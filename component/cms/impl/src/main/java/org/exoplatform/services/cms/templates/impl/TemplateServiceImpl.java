/*
 * Created on Mar 1, 2005
 */
package org.exoplatform.services.cms.templates.impl;

import groovy.lang.GroovyResourceLoader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.security.SecurityService;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class TemplateServiceImpl implements TemplateService, Startable {
  
  private RepositoryService repositoryService_;
  private ConfigurationManager  configManager_;
  private CmsConfigurationService cmsConfigService_;
  private SecurityService securityService_;
  private String cmsTemplatesBasePath_ ;  
  private List<TemplatePlugin> plugins_;     
  
  public TemplateServiceImpl(RepositoryService jcrService, ConfigurationManager configManager,
      CmsConfigurationService cmsConfigService, SecurityService securityService) throws Exception {
    
    cmsConfigService_ = cmsConfigService;
    securityService_ = securityService;
    repositoryService_ = jcrService;
    configManager_ = configManager;
    plugins_ = new ArrayList<TemplatePlugin>();
    cmsTemplatesBasePath_ = cmsConfigService_.getJcrPath(BasePath.CMS_TEMPLATES_PATH) ;
  }
  
  public void start() {
    try {
      initRepository();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void stop() {}
  
  public void addTemplates(ComponentPlugin plugin) {
    if (plugin instanceof TemplatePlugin)
      plugins_.add((TemplatePlugin) plugin);
  }
  
  private void initRepository() throws Exception {    
    Session session = null ;    
    Node root = null ;    
    List nodetypes = null;
    for (int j = 0; j < plugins_.size(); j++) {
      session = repositoryService_.getRepository(plugins_.get(j).getRepository()).
                  getSystemSession(cmsConfigService_.getWorkspace(plugins_.get(j).getRepository()));
      nodetypes = plugins_.get(j).getNodeTypes();
      String location =  plugins_.get(j).getLocation() ;        
      if (nodetypes.isEmpty())
        return;
      TemplateConfig.NodeType nodeType = (TemplateConfig.NodeType) nodetypes.iterator().next();
      String nodeTypeName = nodeType.getNodetypeName();
      try {
        session.getItem(cmsTemplatesBasePath_ + "/" + nodeTypeName);
        return;
      } catch (PathNotFoundException e) {
      }
      root = session.getRootNode();
      Node templatesHome = Utils.makePath(root, cmsTemplatesBasePath_, NT_UNSTRUCTURED);
      String sourcePath = cmsConfigService_.getContentLocation() + "/system" 
      + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
      if (location.equals("jar")) 
        sourcePath = "jar:/conf/system" + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
      
      for (Iterator iter = nodetypes.iterator(); iter.hasNext();) {
        nodeType = (TemplateConfig.NodeType) iter.next();
        nodeTypeName = nodeType.getNodetypeName();
        Node nodeTypeHome = null;
        if (!templatesHome.hasNode(nodeTypeName)){
          nodeTypeHome = Utils.makePath(templatesHome, nodeTypeName,NT_UNSTRUCTURED);
          if(nodeType.getDocumentTemplate())
            nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true) ;
          else
            nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false) ;
        } else{
          nodeTypeHome = templatesHome.getNode(nodeTypeName);
        }
        nodeTypeHome.setProperty(TEMPLATE_LABEL, nodeType.getLabel()) ;
        
        List dialogs = nodeType.getReferencedDialog();
        Node dialogsHome = Utils.makePath(nodeTypeHome, DIALOGS, NT_UNSTRUCTURED);
        addNode(sourcePath, dialogsHome, dialogs);
        
        List views = nodeType.getReferencedView();
        Node viewsHome = Utils.makePath(nodeTypeHome, VIEWS, NT_UNSTRUCTURED);
        addNode(sourcePath, viewsHome, views);
      }
    }
    if(root != null) root.save();
  }
  
  public void init(String repository) throws Exception {    
    Session session = null ;    
    Node root = null ;    
    List nodetypes = null;
    String defaultRepo  = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
    for (int j = 0; j < plugins_.size(); j++) {
      if(plugins_.get(j).getRepository().equals(defaultRepo)) {
        session = repositoryService_.getRepository(repository).
        getSystemSession(cmsConfigService_.getWorkspace(repository));
        nodetypes = plugins_.get(j).getNodeTypes();
        String location =  plugins_.get(j).getLocation() ;        
        if (nodetypes.isEmpty())  return;
        TemplateConfig.NodeType nodeType = (TemplateConfig.NodeType) nodetypes.iterator().next();
        String nodeTypeName = nodeType.getNodetypeName();
        try {
          session.getItem(cmsTemplatesBasePath_ + "/" + nodeTypeName);
          return;
        } catch (PathNotFoundException e) {
        }
        root = session.getRootNode();
        Node templatesHome = Utils.makePath(root, cmsTemplatesBasePath_, NT_UNSTRUCTURED);
        String sourcePath = cmsConfigService_.getContentLocation() + "/system" 
        + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
        if (location.equals("jar")) 
          sourcePath = "jar:/conf/system" + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
        
        for (Iterator iter = nodetypes.iterator(); iter.hasNext();) {
          nodeType = (TemplateConfig.NodeType) iter.next();
          nodeTypeName = nodeType.getNodetypeName();
          Node nodeTypeHome = null;
          if (!templatesHome.hasNode(nodeTypeName)){
            nodeTypeHome = Utils.makePath(templatesHome, nodeTypeName,NT_UNSTRUCTURED);
            if(nodeType.getDocumentTemplate())
              nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true) ;
            else
              nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false) ;
          } else{
            nodeTypeHome = templatesHome.getNode(nodeTypeName);
          }
          nodeTypeHome.setProperty(TEMPLATE_LABEL, nodeType.getLabel()) ;
        
          List dialogs = nodeType.getReferencedDialog();
          Node dialogsHome = Utils.makePath(nodeTypeHome, DIALOGS, NT_UNSTRUCTURED);
          addNode(sourcePath, dialogsHome, dialogs);
          
          List views = nodeType.getReferencedView();
          Node viewsHome = Utils.makePath(nodeTypeHome, VIEWS, NT_UNSTRUCTURED);
          addNode(sourcePath, viewsHome, views);
        }
      }      
    }
    if(root != null) root.save();
  }
  
  private void addNode(String basePath, Node nodeTypeHome, List templates)  throws Exception {
    for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
      TemplateConfig.Template template = (TemplateConfig.Template) iterator.next();
      String templateFileName = template.getTemplateFile();
      String path = basePath + templateFileName;
      InputStream in = configManager_.getInputStream(path);
      String nodeName = 
        templateFileName.substring(templateFileName.lastIndexOf("/") + 1, templateFileName.indexOf("."));
      Node contentNode = nodeTypeHome.addNode(nodeName, EXO_TEMPLATE);
      contentNode.setProperty(EXO_ROLES_PROP, template.getParsedRoles());
      contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, in);
    }
  }
  
  public Node getTemplatesHome(String repository) throws Exception {
    try {
      return (Node) repositoryService_.getRepository(repository)
      .login(cmsConfigService_.getWorkspace(repository)).getItem(cmsTemplatesBasePath_);
    }catch(Exception e) {
      return (Node) repositoryService_.getRepository(repository)
      .getSystemSession(cmsConfigService_.getWorkspace(repository)).getItem(cmsTemplatesBasePath_);
    }
  }  
  
  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception {
    Node systemTemplatesHome = getTemplatesHome(repository) ;
    for(NodeIterator iter = systemTemplatesHome.getNodes();iter.hasNext() ;) {
      Node node = iter.nextNode();
      if (node.getName().equals(nodeTypeName))
        return true;
    }    
    return false;
  }
  
  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName, String repository) throws Exception {
    Node nodeTypeHome = getTemplatesHome(repository).getNode(nodeTypeName);
    if (isDialog)
      return nodeTypeHome.getNode(DIALOGS).getNodes();    
    return nodeTypeHome.getNode(VIEWS).getNodes();
  }
  
  public String getDefaultTemplatePath(boolean isDialog, String nodeTypeName) {
    if (isDialog)
      return cmsTemplatesBasePath_ + "/" + nodeTypeName + DEFAULT_DIALOGS_PATH;    
    return cmsTemplatesBasePath_   + "/" + nodeTypeName + DEFAULT_VIEWS_PATH;
  }
  
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception {
    String type = DIALOGS;
    if (!isDialog) type = VIEWS;
    Node nodeTypeNode = getTemplatesHome(repository).getNode(nodeTypeName);
    return nodeTypeNode.getNode(type).getNode(templateName);
  }
  
  private Node getTemplateNode(String nodeTypeName, String userName, boolean isDialog, String repository) throws Exception {
    String type = DIALOGS;
    if (!isDialog) type = VIEWS;
    Node nodeTypeNode = getTemplatesHome(repository).getNode(nodeTypeName);
    NodeIterator templateIter = nodeTypeNode.getNode(type).getNodes();
    Node selectedTemplateNode = null;
    while (templateIter.hasNext()) {
      Node node = templateIter.nextNode();
      Value[] roles = node.getProperty(EXO_ROLES_PROP).getValues();
      for (int i = 0; i < roles.length; i++) {
        String templateRole = roles[i].getString();
        if ("*".equals(templateRole)) {
          selectedTemplateNode = node;
        }else if(userName != null && userName.equals(templateRole)) {
          return node;	
        }else if (userName != null && securityService_.hasMembershipInGroup(userName, templateRole)){
          return node;
        } 
      }
    }
    return selectedTemplateNode;
  }
  
  public String getTemplatePathByUser(boolean isDialog, String nodeTypeName, String userName, String repository) throws Exception {
    try{
      Node templateNode = getTemplateNode(nodeTypeName, userName, isDialog, repository);
      return templateNode.getPath();
    }catch(Exception e) {
      return null;
    }
  }
  
  public String getTemplatePath(boolean isDialog, String nodeTypeName,
      String templateName, String repository) throws Exception {
    Node templateNode = getTemplateNode(isDialog, nodeTypeName, templateName, repository);
    return templateNode.getPath();
  }
  
  /*public String getTemplate(boolean isDialog, String nodeTypeName) throws Exception {
    Node templateNode = getTemplateNode(nodeTypeName, userName, isDialog );
    return templateNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
  }*/
  
  public String getTemplateLabel(String nodeTypeName, String repository)  throws Exception {
    Node templateHome = getTemplatesHome(repository);
    Node nodeType = templateHome.getNode(nodeTypeName) ;
    if(nodeType.hasProperty("label")) return nodeType.getProperty("label").getString() ;
    return "" ;
  }
  
  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception {
    Node templateNode = getTemplateNode(isDialog, nodeTypeName, templateName, repository);
    return templateNode.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
  }
  
  public String getTemplateRoles(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception {
    Node templateNode = getTemplateNode(isDialog, nodeTypeName, templateName, repository);
    Value[] values = templateNode.getProperty(EXO_ROLES_PROP).getValues() ;
    StringBuffer roles = new StringBuffer() ;
    for(int i = 0 ; i < values.length ; i ++ ){
      if(roles.length() > 0 )roles.append("; ") ;
      roles.append(values[i].getString()) ;
    }
    return roles.toString();
  }
  
  public void removeTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception {
    Node nodeTypeHome = getTemplatesHome(repository).getNode(nodeTypeName);
    Node specifiedTemplatesHome = null;
    if (isDialog) {
      specifiedTemplatesHome = nodeTypeHome.getNode(DIALOGS);
    } else {
      specifiedTemplatesHome = nodeTypeHome.getNode(VIEWS);
    }
    Node contentNode = specifiedTemplatesHome.getNode(templateName);
    contentNode.remove() ;
    nodeTypeHome.save() ;
//    removeFromCache(path) ;
  }
  
  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception {
    Node templatesHome = getTemplatesHome(repository) ;
    Node managedNodeType = templatesHome.getNode(nodeTypeName);
    managedNodeType.remove() ;
    templatesHome.save() ;
  }
  
  public void addTemplate(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, 
      String templateName, String[] roles, String templateFile, String repository) throws Exception {    
    Node templatesHome = getTemplatesHome(repository) ;
    
    Node nodeTypeHome = null;
    if (!templatesHome.hasNode(nodeTypeName)){
      nodeTypeHome = Utils.makePath(templatesHome, nodeTypeName, NT_UNSTRUCTURED);
      if(isDocumentTemplate){
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true) ;        
      }
      else 
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false) ;
      nodeTypeHome.setProperty(TEMPLATE_LABEL, label) ;
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
//    removeFromCache(contentNode.getPath()) ;
  }
  
  public List<String> getDocumentTemplates(String repository) throws Exception {
    List<String> templates = new ArrayList<String>() ;                
    Node templatesHome = getTemplatesHome(repository) ;    
    for(NodeIterator templateIter = templatesHome.getNodes(); templateIter.hasNext() ; ) {
      Node template = templateIter.nextNode() ;      
      if(template.getProperty(DOCUMENT_TEMPLATE_PROP).getBoolean())        
        templates.add(template.getName()) ;
    }        
    return templates ;
  }      
  
  public List<Node> getNodeDocumentTemplates(String repository) throws Exception {
    List<Node> templates = new ArrayList<Node>() ;                
    Node templatesHome = getTemplatesHome(repository) ;    
    for(NodeIterator templateIter = templatesHome.getNodes(); templateIter.hasNext() ; ) {
      Node template = templateIter.nextNode() ;         
      if(template.getProperty(DOCUMENT_TEMPLATE_PROP).getBoolean())        
        templates.add(template) ;
    }    
    return templates ;
  }      
  
 /* private Node getSystemTemplatesHome() throws Exception {    
    Session session = repositoryService_.getRepository().getSystemSession(cmsConfigService_.getWorkspace());        
    Node templatesHome = (Node) session.getItem(cmsTemplatesBasePath_);
    return templatesHome ;
  }*/
  
//  protected void removeFromCache(String templateName) {
//    try{
//      ExoCache jcrcache_ = cacheService_.getCacheInstance(GroovyResourceLoader.class.getName()) ;
////      String portalName = containerInfo_.getContainerName() ;
////      String key = portalName + "jcr:" +templateName ; 
////      System.out.println("\n\nkey===>" + key + "\n\n");
//      Object cachedobject = jcrcache_.get(templateName);
//      if (cachedobject != null) {
//        jcrcache_.remove(templateName);      
//      }
//    } catch(Exception e) {
//      e.printStackTrace() ;
//    }
//  }  
}
