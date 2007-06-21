/*
 * Created on Mar 1, 2005
 */
package org.exoplatform.services.cms.templates.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
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
public class TemplateServiceImpl implements TemplateService, Startable{
  
  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_;
  private SecurityService securityService_;
  private String cmsTemplatesBasePath_ ;  
  private List<TemplatePlugin> plugins_ = new ArrayList<TemplatePlugin>();
  
  public TemplateServiceImpl(RepositoryService jcrService, CmsConfigurationService cmsConfigService,
      SecurityService securityService) throws Exception {
    cmsConfigService_ = cmsConfigService;
    securityService_ = securityService;
    repositoryService_ = jcrService;
    cmsTemplatesBasePath_ = cmsConfigService_.getJcrPath(BasePath.CMS_TEMPLATES_PATH) ;
  }
  
  public void start() {
    try {
      for(TemplatePlugin plugin : plugins_) {
        plugin.init() ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void stop() {}
  
  public void addTemplates(ComponentPlugin plugin) {
   if (plugin instanceof TemplatePlugin)  plugins_.add((TemplatePlugin) plugin);
  }
   
  public void init(String repository) throws Exception {    
    for(TemplatePlugin plugin : plugins_) {
      plugin.init(repository) ;
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
  
  public String addTemplate(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, 
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
    return contentNode.getPath() ;
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
 
}
