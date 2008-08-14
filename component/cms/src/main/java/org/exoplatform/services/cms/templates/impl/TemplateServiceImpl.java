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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class TemplateServiceImpl implements TemplateService, Startable {

  private RepositoryService    repositoryService_;

  private ConversationRegistry conversationRegistry_;

  private String               cmsTemplatesBasePath_;

  private List<TemplatePlugin> plugins_ = new ArrayList<TemplatePlugin>();

  public TemplateServiceImpl(RepositoryService jcrService,
      NodeHierarchyCreator nodeHierarchyCreator, ConversationRegistry conversationRegistry) throws Exception {
    conversationRegistry_ = conversationRegistry;
    repositoryService_ = jcrService;
    cmsTemplatesBasePath_ = nodeHierarchyCreator.getJcrPath(BasePath.CMS_TEMPLATES_PATH);
  }

  public void start() {
    try {
      for (TemplatePlugin plugin : plugins_) {
        plugin.init();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
  }

  public void addTemplates(ComponentPlugin plugin) {
    if (plugin instanceof TemplatePlugin)
      plugins_.add((TemplatePlugin) plugin);
  }

  public void init(String repository) throws Exception {
    for (TemplatePlugin plugin : plugins_) {
      plugin.init(repository);
    }
  }

  public Node getTemplatesHome(String repository, SessionProvider provider) throws Exception {
    try {
      Session session = getSession(repository, provider);
      return (Node) session.getItem(cmsTemplatesBasePath_);
    } catch (AccessDeniedException ace) {
      return null;
    }
  }

  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception {
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

  public String getTemplatePath(Node node, boolean isDialog) throws Exception {
    String userId = node.getSession().getUserID();
    String repository = ((ManageableRepository) node.getSession().getRepository())
    .getConfiguration().getName();
    String templateType = null;
    if (node.isNodeType("exo:presentationable")) {
      templateType = node.getProperty("exo:presentationType").getString();
    } else {
      templateType = node.getPrimaryNodeType().getName();
    }
    if (isManagedNodeType(templateType, repository))
      return getTemplatePathByUser(isDialog, templateType, userId, repository);
    return null;
  }

  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName,
      String repository, SessionProvider provider) throws Exception {
    Node nodeTypeHome = getTemplatesHome(repository, provider).getNode(nodeTypeName);
    if (isDialog)
      return nodeTypeHome.getNode(DIALOGS).getNodes();
    return nodeTypeHome.getNode(VIEWS).getNodes();
  }

  public String getDefaultTemplatePath(boolean isDialog, String nodeTypeName) {
    if (isDialog)
      return cmsTemplatesBasePath_ + "/" + nodeTypeName + DEFAULT_DIALOGS_PATH;
    return cmsTemplatesBasePath_ + "/" + nodeTypeName + DEFAULT_VIEWS_PATH;
  }

  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName,
      String repository, SessionProvider provider) throws Exception {
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node nodeTypeNode = getTemplatesHome(repository, provider).getNode(nodeTypeName);
    return nodeTypeNode.getNode(type).getNode(templateName);
  }

  public String getTemplatePathByUser(boolean isDialog, String nodeTypeName, String userName,
      String repository) throws Exception {
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
      if(hasPermission(userName, roles, conversationRegistry_)) {
        String templatePath = node.getPath() ;
        session.logout();
        return templatePath ;
      }
    }
    session.logout();
    return null;
  }

  public String getTemplatePath(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templateNode = getTemplateNode(session, isDialog, nodeTypeName, templateName, repository);
    String path = templateNode.getPath();
    session.logout();
    return path;
  }

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

  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templateNode = getTemplateNode(session, isDialog, nodeTypeName, templateName, repository);
    String template = templateNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
    session.logout();
    return template;
  }

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

  @SuppressWarnings("unused")
  private Node getTemplateNode(Session session, boolean isDialog, String nodeTypeName,
      String templateName, String repository) throws Exception {
    String type = DIALOGS;
    if (!isDialog)
      type = VIEWS;
    Node homeNode = (Node) session.getItem(cmsTemplatesBasePath_);
    Node nodeTypeNode = homeNode.getNode(nodeTypeName);
    return nodeTypeNode.getNode(type).getNode(templateName);
  }

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

  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    Node managedNodeType = templatesHome.getNode(nodeTypeName);
    managedNodeType.remove();
    templatesHome.save();
    session.save();
    session.logout();
  }

  public String addTemplate(boolean isDialog, String nodeTypeName, String label,
      boolean isDocumentTemplate, String templateName, String[] roles, String templateFile,
      String repository) throws Exception {
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
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
    contentNode.setProperty(EXO_ROLES_PROP, roles);
    contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, templateFile);

    templatesHome.save();
    session.save();
    session.logout();
    return contentNode.getPath();
  }

  public List<String> getDocumentTemplates(String repository) throws Exception {
    List<String> templates = new ArrayList<String>();
    Session session = getSession(repository);
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    for (NodeIterator templateIter = templatesHome.getNodes(); templateIter.hasNext();) {
      Node template = templateIter.nextNode();
      if (template.getProperty(DOCUMENT_TEMPLATE_PROP).getBoolean())
        templates.add(template.getName());
    }
    session.logout();
    return templates;
  }
  
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

  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    String systemWorksapce = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return manageableRepository.getSystemSession(systemWorksapce);
  }

  private Session getSession(String repository, SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    String systemWorksapce = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return provider.getSession(systemWorksapce, manageableRepository);
  }

  private boolean hasPermission(String userId,Value[] roles, ConversationRegistry conversationRegistry) throws Exception {        
    if(SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      return true ;
    }
    Identity identity = conversationRegistry.getState(userId).getIdentity() ;
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
  
  private boolean hasPublicTemplate(Value[] roles) throws Exception {
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if("*".equalsIgnoreCase(role)) return true ;
    }
    return false ;
  }
}
