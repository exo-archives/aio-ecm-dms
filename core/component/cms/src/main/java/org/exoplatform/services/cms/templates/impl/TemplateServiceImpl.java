/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.templates.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.templates.ContentTypeFilterPlugin;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.templates.ContentTypeFilterPlugin.FolderFilterConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class TemplateServiceImpl implements TemplateService, Startable {
  private RepositoryService    repositoryService_;
  private IdentityRegistry identityRegistry_;
  private LocaleConfigService localeConfigService_;
  private String               cmsTemplatesBasePath_;
  private List<TemplatePlugin> plugins_ = new ArrayList<TemplatePlugin>();

  private Map<String,HashMap<String,List<String>>> foldersFilterMap = new HashMap<String,HashMap<String,List<String>>> ();  
  private Map<String,List<String>> managedDocumentTypesMap = new HashMap<String,List<String>>();
  
  /** Immutable and therefore thread safe. */
  private static final Pattern LT = Pattern.compile("/\\*\\s*orientation=lt\\s*\\*/");

  /** Immutable and therefore thread safe. */
  private static final Pattern RT = Pattern.compile("/\\*\\s*orientation=rt\\s*\\*/");

  private ExoCache templatesCache_ ;
  private ExoCache rtlTemplateCache_;
  
  /**
   * DMS configuration which used to store informations
   */   
  private DMSConfiguration dmsConfiguration_;
  
  public TemplateServiceImpl(RepositoryService jcrService,
      NodeHierarchyCreator nodeHierarchyCreator, IdentityRegistry identityRegistry,
      LocaleConfigService localeConfigService, CacheService caService, 
      DMSConfiguration dmsConfiguration) throws Exception {
    identityRegistry_ = identityRegistry;
    repositoryService_ = jcrService;
    localeConfigService_ = localeConfigService;
    cmsTemplatesBasePath_ = nodeHierarchyCreator.getJcrPath(BasePath.CMS_TEMPLATES_PATH);
    templatesCache_ = caService.getCacheInstance(org.exoplatform.groovyscript.text.TemplateService.class.getName()) ;
    rtlTemplateCache_ = caService.getCacheInstance("RTLTemplateCache");
    dmsConfiguration_ = dmsConfiguration;
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    try {
      for (TemplatePlugin plugin : plugins_) {
        plugin.init();
      }
      //Cached all nodetypes that is document type in the map
      for(RepositoryEntry repositoryEntry:repositoryService_.getConfig().getRepositoryConfigurations()) {
        String repositoryName = repositoryEntry.getName();
        List<String> managedContentTypes = getAllDocumentNodeTypes(repositoryEntry.getName());
        if(managedContentTypes.size() != 0) {
          managedDocumentTypesMap.put(repositoryName,managedContentTypes);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void stop() {
  }
  
  /**
   * {@inheritDoc}
   */
  public void addContentTypeFilterPlugin(ContentTypeFilterPlugin filterPlugin) {
    String repository = filterPlugin.getRepository();
    HashMap<String,List<String>> folderFilterMap = foldersFilterMap.get(repository); 
    if(folderFilterMap == null) {
      folderFilterMap = new HashMap<String,List<String>>();
    }    
    for(FolderFilterConfig filterConfig: filterPlugin.getFolderFilterConfigList()) {      
      String folderType = filterConfig.getFolderType();
      List<String> contentTypes = filterConfig.getContentTypes();
      List<String> value = folderFilterMap.get(folderType);
      if(value == null) {
        folderFilterMap.put(folderType,contentTypes);
      }else {
        value.addAll(contentTypes);
        folderFilterMap.put(folderType,value);
      }
    }
    foldersFilterMap.put(repository,folderFilterMap);
  }

  /**
   * {@inheritDoc}
   */
  public void addTemplates(ComponentPlugin plugin) {
    if (plugin instanceof TemplatePlugin)
      plugins_.add((TemplatePlugin) plugin);
  }
  
  /**
   * {@inheritDoc}
   */
  public void init(String repository) throws Exception {
    for (TemplatePlugin plugin : plugins_) {
      plugin.init(repository);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getTemplatesHome(String repository, SessionProvider provider) throws Exception {
    try {
      Session session = getSession(repository, provider);
      return (Node) session.getItem(cmsTemplatesBasePath_);
    } catch (AccessDeniedException ace) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getCreationableContentTypes(Node node) throws Exception {
    String folderType = node.getPrimaryNodeType().getName();    
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    List<String> testContentTypes = null;    
    HashMap<String,List<String>> folderFilterMap = foldersFilterMap.get(repository);
    if(folderFilterMap != null) {
      List<String> list = folderFilterMap.get(folderType);
      if(list != null && list.size() != 0) {
        testContentTypes = list;
      }
    }
    if(testContentTypes == null) {
      testContentTypes = getDocumentTemplates(repository);
    }    
    List<String> result = new ArrayList<String>();
    for(String contentType: testContentTypes) {
      if(isChildNodePrimaryTypeAllowed(node,contentType)) {
        if (!folderType.equals(contentType))                  //When content type is not parent node's content type
          result.add(contentType);
      }
    }            
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isChildNodePrimaryTypeAllowed(Node parent, String childNodeTypeName) throws Exception{
    NodeType childNodeType = parent.getSession().getWorkspace().getNodeTypeManager().getNodeType(childNodeTypeName);
    //In some cases, the child node is mixins type of a nt:file example
    if(childNodeType.isMixin()) return true;    
    List<ExtendedNodeType> allNodeTypes = new ArrayList<ExtendedNodeType>();
    allNodeTypes.add((ExtendedNodeType)parent.getPrimaryNodeType());
    for(NodeType mixin: parent.getMixinNodeTypes()) {
      allNodeTypes.add((ExtendedNodeType)mixin);
    }
    for (ExtendedNodeType nodetype:allNodeTypes) {      
      if (nodetype.isChildNodePrimaryTypeAllowed(childNodeTypeName)) {
        return true;
      } 
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception {
    //check if the node type is document type first
    List<String> managedDocumentTypes = managedDocumentTypesMap.get(repository);
    if(managedDocumentTypes != null && managedDocumentTypes.contains(nodeTypeName)) 
      return true;
    SessionProvider provider = SessionProvider.createSystemProvider();
    Session session = getSession(repository, provider);
    Node systemTemplatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    boolean b = false;
    if (systemTemplatesHome.hasNode(nodeTypeName)) {
      b = true;
    }
    provider.close();
    return b;
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplatePath(Node node, boolean isDialog) throws Exception {
    String userId = node.getSession().getUserID();
    String repository = ((ManageableRepository) node.getSession().getRepository())
    .getConfiguration().getName();
    String templateType = null;
    if (node.isNodeType("exo:presentationable") && node.hasProperty("exo:presentationType")) {
      templateType = node.getProperty("exo:presentationType").getString();
    } else {
      templateType = node.getPrimaryNodeType().getName();
    }
    if (isManagedNodeType(templateType, repository))
      return getTemplatePathByUser(isDialog, templateType, userId, repository);
    throw new Exception("The content type: " + templateType + " doesn't be supported by any template");
  }
  
  /**
   * {@inheritDoc}
   */
  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName,
      String repository, SessionProvider provider) throws Exception {
    Node nodeTypeHome = getTemplatesHome(repository, provider).getNode(nodeTypeName);
    if (isDialog)
      return nodeTypeHome.getNode(DIALOGS).getNodes();
    return nodeTypeHome.getNode(VIEWS).getNodes();
  }

  /**
   * {@inheritDoc}
   */
  public String getDefaultTemplatePath(boolean isDialog, String nodeTypeName) {
    if (isDialog)
      return cmsTemplatesBasePath_ + "/" + nodeTypeName + DEFAULT_DIALOGS_PATH;
    return cmsTemplatesBasePath_ + "/" + nodeTypeName + DEFAULT_VIEWS_PATH;
  }

  /**
   * {@inheritDoc}
   */
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName,
      String repository, SessionProvider provider) throws Exception {
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node nodeTypeNode = getTemplatesHome(repository, provider).getNode(nodeTypeName);
    return nodeTypeNode.getNode(type).getNode(templateName);
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplatePathByUser(boolean isDialog, String nodeTypeName, String userName,
      String repository) throws Exception {
    if(SystemIdentity.ANONIM.equals(userName) || userName == null) {
      return getTemplatePathByAnonymous(isDialog, nodeTypeName, repository);
    }
    Session session = getSession(repository);
    Node templateHomeNode = (Node) session.getItem(cmsTemplatesBasePath_);
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node nodeTypeNode = templateHomeNode.getNode(nodeTypeName);
    NodeIterator templateIter = nodeTypeNode.getNode(type).getNodes();
    while (templateIter.hasNext()) {
      Node node = templateIter.nextNode();
      Value[] roles = node.getProperty(EXO_ROLES_PROP).getValues();
      if(hasPermission(userName, roles, identityRegistry_)) {
        String templatePath = node.getPath() ;
        session.logout();
        return templatePath ;
      }
    }
    session.logout();
    throw new AccessControlException("You don't have permission to access any template");
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplatePath(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templateNode = getTemplateNode(session, isDialog, nodeTypeName, templateName, repository);
    String path = templateNode.getPath();
    session.logout();
    return path;
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplateLabel(String nodeTypeName, String repository) throws Exception {
    SessionProvider provider = SessionProvider.createSystemProvider();
    Node templateHome = getTemplatesHome(repository, provider);
    Node nodeType = templateHome.getNode(nodeTypeName);
    String label = "";
    if (nodeType.hasProperty("label")) {
      label = nodeType.getProperty("label").getString();
    }
    provider.close();
    return label;
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templateNode = getTemplateNode(session, isDialog, nodeTypeName, templateName, repository);
    String template = templateNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
    session.logout();
    return template;
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplateRoles(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templateNode = getTemplateNode(session, isDialog, nodeTypeName, templateName, repository);
    Value[] values = templateNode.getProperty(EXO_ROLES_PROP).getValues();
    StringBuffer roles = new StringBuffer();
    for (int i = 0; i < values.length; i++) {
      if (roles.length() > 0)
        roles.append("; ");
      roles.append(values[i].getString());
    }
    session.logout();
    return roles.toString();
  }

  /**
   * {@inheritDoc}
   */
  public void removeTemplate(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    Node nodeTypeHome = templatesHome.getNode(nodeTypeName);
    Node specifiedTemplatesHome = null;
    if (isDialog) {
      specifiedTemplatesHome = nodeTypeHome.getNode(DIALOGS);
    } else {
      specifiedTemplatesHome = nodeTypeHome.getNode(VIEWS);
    }
    Node contentNode = specifiedTemplatesHome.getNode(templateName);
    contentNode.remove();
    nodeTypeHome.save();
    session.save();
    session.logout();
  }

  /**
   * {@inheritDoc}
   */
  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    Node managedNodeType = templatesHome.getNode(nodeTypeName);
    managedNodeType.remove();    
    session.save();
    session.logout();
    //Update managedDocumentTypeMap
    List<String> managedDocumentTypes = managedDocumentTypesMap.get(repository);
    managedDocumentTypes.remove(nodeTypeName);
  }

  /**
   * {@inheritDoc}
   */
  public String addTemplate(boolean isDialog, String nodeTypeName, String label,
      boolean isDocumentTemplate, String templateName, String[] roles, String templateFile,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    Node contentNode = getContentNode(isDialog, templatesHome, nodeTypeName, label, 
        isDocumentTemplate, templateName);
    contentNode.setProperty(EXO_ROLES_PROP, roles);
    contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, templateFile);
    templatesHome.save();
    session.save();
    session.logout();
    //Update managedDocumentTypesMap
    updateDocumentsTemplate(isDocumentTemplate, repository, nodeTypeName);
    return contentNode.getPath();
  }
  
  /**
   * {@inheritDoc}
   */
  public String addTemplateWithLocale(boolean isDialog, String nodeTypeName, String label,
      boolean isDocumentTemplate, String templateName, String[] roles, String templateFile,
      String repository, String locale) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    Node contentNode = getContentNode(isDialog, templatesHome, nodeTypeName, label, 
        isDocumentTemplate, templateName);
    contentNode.setProperty(EXO_ROLES_PROP, roles);
    contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, templateFile);
    rtlTemplateCache_.clearCache();
    setTemplateData(locale, contentNode, nodeTypeName, RT, LTR);
    setTemplateData(locale, contentNode, nodeTypeName, LT, RTL);
    templatesHome.save();
    session.save();
    session.logout();
    //Update managedDocumentTypesMap
    updateDocumentsTemplate(isDocumentTemplate, repository, nodeTypeName);
    return contentNode.getPath();
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getDocumentTemplates(String repository) throws Exception {    
    List<String> templates = managedDocumentTypesMap.get(repository);
    if(templates != null) 
      return templates;
    templates = getAllDocumentNodeTypes(repository);
    managedDocumentTypesMap.put(repository,templates);
    return templates;    
  }

  /**
   * {@inheritDoc}
   */
  public String getTemplatePathByAnonymous(boolean isDialog, String nodeTypeName, String repository) throws Exception {
    Session session = getSession(repository);
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node homeNode = (Node) session.getItem(cmsTemplatesBasePath_);
    Node nodeTypeNode = homeNode.getNode(nodeTypeName);
    NodeIterator templateIter = nodeTypeNode.getNode(type).getNodes();
    while (templateIter.hasNext()) {
      Node node = templateIter.nextNode();
      Value[] roles = node.getProperty(EXO_ROLES_PROP).getValues();
      if(hasPublicTemplate(roles)) {
        String templatePath = node.getPath() ;
        session.logout();
        return templatePath ;
      }
    }
    session.logout();
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  public String getTemplateData(Node templateNode, String locale, String propertyName, 
      String repository) throws Exception {
    Orientation orientation = getOrientation(locale);
    Node nodeType = templateNode.getParent().getParent();
    List<String> documentTemplates = managedDocumentTypesMap.get(repository);
    Node parentNode = templateNode.getParent();
    if(documentTemplates.contains(nodeType.getName()) && parentNode.getName().equals(VIEWS)) {
      if(rtlTemplateCache_.get(nodeType.getName() + LTR) == null || 
          rtlTemplateCache_.get(nodeType.getName() + RTL) == null) {
        setTemplateData(locale, templateNode, nodeType.getName(), RT, LTR);
        setTemplateData(locale, templateNode, nodeType.getName(), LT, RTL);
      }
      if(orientation.isLT() && 
          rtlTemplateCache_.get(nodeType.getName() + LTR) != null) {
        return rtlTemplateCache_.get(nodeType.getName() + LTR).toString();
      } else if(orientation.isRT() && 
          rtlTemplateCache_.get(nodeType.getName() + RTL) != null) {
        return rtlTemplateCache_.get(nodeType.getName() + RTL).toString();
      }
    }
    return templateNode.getProperty(propertyName).getString();
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeCacheTemplate(String resourceId) throws Exception {
    templatesCache_.remove(resourceId);
  }
  
  /**
   * Get template with the following specified params 
   * @param session         Session         
   * @param isDialog        boolean
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param templateName    String
   *                        The name of template
   * @param repository      String
   *                        The name of repository    
   * @return
   * @throws Exception
   */
  private Node getTemplateNode(Session session, boolean isDialog, String nodeTypeName,
      String templateName, String repository) throws Exception {
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node homeNode = (Node) session.getItem(cmsTemplatesBasePath_);
    Node nodeTypeNode = homeNode.getNode(nodeTypeName);
    return nodeTypeNode.getNode(type).getNode(templateName);
  }
  
  /**
   * Get content of the specified node 
   * @param isDialog              boolean
   * @param templatesHome         Node
   * @param nodeTypeName          String
   *                              The name of NodeType
   * @param label                 String
   *                              The label of template
   * @param isDocumentTemplate    boolean
   * @param templateName          String
   *                              The name of template
   * @see                         Node                              
   * @return
   * @throws Exception
   */
  private Node getContentNode(boolean isDialog, Node templatesHome, String nodeTypeName, 
      String label, boolean isDocumentTemplate, String templateName) throws Exception {
    Node nodeTypeHome = null;
    if (!templatesHome.hasNode(nodeTypeName)) {
      nodeTypeHome = Utils.makePath(templatesHome, nodeTypeName, NT_UNSTRUCTURED);
      if (isDocumentTemplate) {
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true);
      } else
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false);
      nodeTypeHome.setProperty(TEMPLATE_LABEL, label);
    } else {
      nodeTypeHome = templatesHome.getNode(nodeTypeName);
    }
    Node specifiedTemplatesHome = null;
    if (isDialog) {
      if (!nodeTypeHome.hasNode(DIALOGS)) {
        specifiedTemplatesHome = Utils.makePath(nodeTypeHome, DIALOGS, NT_UNSTRUCTURED);
      } else {
        specifiedTemplatesHome = nodeTypeHome.getNode(DIALOGS);
      }
    } else {
      if (!nodeTypeHome.hasNode(VIEWS)) {
        specifiedTemplatesHome = Utils.makePath(nodeTypeHome, VIEWS, NT_UNSTRUCTURED);
      } else {
        specifiedTemplatesHome = nodeTypeHome.getNode(VIEWS);
      }
    }

    Node contentNode = null;
    if (specifiedTemplatesHome.hasNode(templateName)) {
      contentNode = specifiedTemplatesHome.getNode(templateName);
    } else {
      contentNode = specifiedTemplatesHome.addNode(templateName, EXO_TEMPLATE);
    }
    return contentNode;
  }
  
  /**
   * Update document template
   * @param isDocumentTemplate    boolean
   * @param repository            String
   *                              The name of repository
   * @param nodeTypeName          String
   *                              The name of NodeType
   * @see                         Node
   * @see                         NodeType                              
   */
  private void updateDocumentsTemplate(boolean isDocumentTemplate, String repository, 
      String nodeTypeName) {
    if(isDocumentTemplate) {
      List<String> documentList = managedDocumentTypesMap.get(repository);
      if(documentList == null) {
        documentList = new ArrayList<String>();
        documentList.add(nodeTypeName);
        managedDocumentTypesMap.put(repository,documentList);
      } else {
        if(!documentList.contains(nodeTypeName)) {
          documentList.add(nodeTypeName);
          managedDocumentTypesMap.put(repository,documentList);
        } 
      }
    }    
  }
  
  /**
   * Set data for template
   * @param locale          String                        
   * @param templateNode    Node
   *                        the specified template node 
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param pattern         Pattern
   * @param type            String
   * @see                   Node
   * @see                   NodeType
   * @see                   Pattern
   * @throws Exception
   */
  private void setTemplateData(String locale, Node templateNode, String nodeTypeName, 
      Pattern pattern, String type) throws Exception {
    if(locale != null) {
      String str = "";
      String templateData = templateNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
      BufferedReader reader = new BufferedReader(new StringReader(templateData));
      Node homeNode = (Node) templateNode.getSession().getItem(cmsTemplatesBasePath_);
      Node includedTemplateNode = null;
      StringBuilder fullData = new StringBuilder();
      try {
        while ((str = reader.readLine()) != null) {
          if (str.length() > 0) {
            if(str.indexOf("_ctx.include") > -1) {
              String[] arrTemplate = str.split("\"");
              includedTemplateNode = homeNode.getNode(arrTemplate[1]);
              Node specifiedTemplatesHome = includedTemplateNode.getNode(VIEWS);
              Node viewNode = specifiedTemplatesHome.getNode(arrTemplate[3]);
              String templateIncludedData = viewNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
              BufferedReader viewReader = new BufferedReader(new StringReader(templateIncludedData));
              String includedStr = "";
              while ((includedStr = viewReader.readLine()) != null) {
                if (includedStr.length() > 0) {
                  fullData.append(includedStr).append('\n');
                }
              }
            } else {
              fullData.append(str).append('\n');
            }
          }
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
      String realStr = "";
      StringBuilder ltsb = new StringBuilder();
      BufferedReader realReader = new BufferedReader(new StringReader(fullData.toString()));
      try {
        while ((realStr = realReader.readLine()) != null) {
          if (realStr.length() > 0) {
            Matcher matcher = pattern.matcher(realStr);
            if(matcher.find()) {
              continue;
            }
            ltsb.append(realStr).append('\n');
          }
        }
        rtlTemplateCache_.put(nodeTypeName + type, ltsb.toString());
      } catch(IOException e) {
        e.printStackTrace();
      } 
    }
  }
  
  /**
   * Return session of the specified repository
   * @param repository      String
   *                        The name of repository
   * @return
   * @see                   ManageableRepository
   * @see                   DMSRepositoryConfiguration
   * @throws Exception
   */
  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    return manageableRepository.getSystemSession(dmsRepoConfig.getSystemWorkspace());
  }

  /**
   * get All Document NodeTypes of the specified repository
   * @param repository      String
   *                        The name of repository
   * @return
   * @throws Exception
   */
  private List<String> getAllDocumentNodeTypes(String repository) throws Exception {
    List<String> contentTypes = new ArrayList<String>();
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    for (NodeIterator templateIter = templatesHome.getNodes(); templateIter.hasNext();) {
      Node template = templateIter.nextNode();
      if (template.getProperty(DOCUMENT_TEMPLATE_PROP).getBoolean())
        contentTypes.add(template.getName());
    }
    session.logout();
    return contentTypes;
  }

  /**
   * Return session of the specified repository
   * @param repository      String
   *                        The name of repository
   * @param provider        SessionProvider                    
   * @return
   * @see                   SessionProvider
   * @see                   ManageableRepository
   * @see                   DMSRepositoryConfiguration
   * @throws Exception
   */
  private Session getSession(String repository, SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    return provider.getSession(dmsRepoConfig.getSystemWorkspace(), manageableRepository);
  }

  /**
   * Check permission of the user with roles
   * @param userId              String
   *                            The specified user
   * @param roles               Value[]
   * @param identityRegistry    IdentityRegistry
   * @see                       MembershipEntry
   * @see                       IdentityRegistry
   * @return
   * @throws Exception
   */
  private boolean hasPermission(String userId,Value[] roles, IdentityRegistry identityRegistry) throws Exception {        
    if(SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      return true ;
    } 
    Identity identity = identityRegistry.getIdentity(userId) ;
    if(identity == null) {
      return false ; 
    }        
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if("*".equalsIgnoreCase(role)) return true ;
      MembershipEntry membershipEntry = MembershipEntry.parse(role) ;
      if(identity.isMemberOf(membershipEntry)) {
        return true ;
      }
    }
    return false ;
  }
  
  /**
   * Check public template with the roles
   * @param roles         Value[]
   * @return
   * @throws Exception
   */
  private boolean hasPublicTemplate(Value[] roles) throws Exception {
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if("*".equalsIgnoreCase(role)) return true ;
    }
    return false ;
  }
  
  /**
   * Get orientaion of the locale
   * @param locale        String
   * @see                 LocaleConfigService
   * @return
   * @throws Exception
   */
  private Orientation getOrientation(String locale) throws Exception {
    return localeConfigService_.getLocaleConfig(locale).getOrientation();
  }
}
