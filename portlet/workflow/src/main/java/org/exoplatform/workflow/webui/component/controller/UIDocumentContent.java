/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller ;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.workflow.utils.SessionsUtils;
import org.exoplatform.workflow.utils.Utils;
import org.exoplatform.workflow.webui.component.ECMViewComponent;
import org.exoplatform.workflow.webui.component.JCRResourceResolver;

/**
 * Created by The eXo Platform SARL
 * Author : tran the trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIDocumentContent.gtmpl",
    events = {
        @EventConfig(listeners = UIDocumentContent.ChangeLanguageActionListener.class),
        @EventConfig(listeners = UIDocumentContent.DownloadActionListener.class),
        @EventConfig(listeners = UIDocumentContent.ChangeNodeActionListener.class)
    }
)
public class UIDocumentContent extends UIContainer implements ECMViewComponent {
  private Node node_ ;
  public static final String DEFAULT_LANGUAGE = "default".intern() ;
  private String language_ = DEFAULT_LANGUAGE ;
  public UIDocumentContent() throws Exception {}
  
  public void setNode(Node node) { this.node_ = node; }
  
  public Node getNode() throws Exception { 
    if(node_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = node_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(!language_.equals(DEFAULT_LANGUAGE) && !language_.equals(defaultLang)) {
        Node curNode = node_.getNode(Utils.LANGUAGES + "/" + language_) ;
        language_ = defaultLang ;
        return curNode ;
      } 
    }    
    return node_;
  }
  public Node getOriginalNode() throws Exception {return node_;}
  
  public String getNodeType() throws Exception { return node_.getPrimaryNodeType().getName() ; }
  
  public String getTemplate() {
    try {
      if(isNodeTypeSupported()) return getTemplatePath() ;
      return super.getTemplate() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null ;
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    try {
      ManageableRepository repository = repositoryService.getRepository(getRepository()) ;
      Session session = SessionsUtils.getSystemProvider().getSession(getWorkspaceName(), repository) ;
      return new JCRResourceResolver(session, Utils.EXO_TEMPLATEFILE) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String nodeTypeName = node_.getPrimaryNodeType().getName();
      
      return templateService.isManagedNodeType(nodeTypeName, getRepository());
    } catch (Exception e) {
      return false;
    }
  }
  
  public String getImage(Node node, String nodeTypeName) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(nodeTypeName) ;    
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  public Node getNodeByPath(String nodePath, String workspace) throws Exception {
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(getRepository()) ;
    Session session = manageRepo.getSystemSession(workspace) ;
    return (Node) session.getItem(nodePath) ;
  }
  
  public String getCapacityOfFile(Node file) throws Exception {
    Node contentNode = file.getNode(Utils.JCR_CONTENT) ;
    InputStream in = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
    float capacity = in.available()/1024 ;
    String strCapacity = Float.toString(capacity) ;
    if(strCapacity.indexOf(".") > -1) return strCapacity.split(".")[0] ;
    return strCapacity ;
  }

  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>();
    String repository = 
      ((ManageableRepository)node_.getSession().getRepository()).getConfiguration().getName() ;
    try {
      Value[] vals = node_.getProperty(Utils.EXO_RELATION).getValues();
      for (Value val : vals) {
        String uuid = val.getString();
        Node relationNode = getNodeByUUID(uuid, repository);
        relations.add(relationNode);
      }
    } catch (Exception e) {}
    return relations;
  }
  
  private Node getNodeByUUID(String uuid, String repository) throws Exception{ 
    ManageableRepository manageRepo = 
      getApplicationComponent(RepositoryService.class).getRepository(repository) ;    
    SessionProvider sessionProvider = Utils.getSessionProvider();
    for(String ws : manageRepo.getWorkspaceNames()) {
      try{
        return sessionProvider.getSession(ws,manageRepo).getNodeByUUID(uuid) ;
      }catch(Exception e) {        
      }      
    }
    return null;
  }
  
  private List<String> getListAllowedFileType(Node currentNode, String repository) throws Exception {
    List<String> nodeTypes = new ArrayList<String>() ;
    NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = currentNode.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates(repository) ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ; 
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
        NodeType[] superTypes = nodeType.getSupertypes() ;
        boolean isCanCreateDocument = false ;
        for(NodeDefinition childDef : childDefs){
          NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes() ;
          for(NodeType requiredChild : requiredChilds) {          
            if(nodeTypeName.equals(requiredChild.getName())){            
              isCanCreateDocument = true ;
              break ;
            }            
          }
          if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
            if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName) ;
            isCanCreateDocument = true ;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName) ;
                  isCanCreateDocument = true ;
                  break;
                }
              }
              if(isCanCreateDocument) break ;
            }
            if(isCanCreateDocument) break ;
          }
        }            
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return nodeTypes ;
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>();
    String nodeType = "";
    NodeIterator childrenIterator;
    childrenIterator = node_.getNodes();
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      try {
        nodeType = childNode.getPrimaryNodeType().getName();
        List<String> listCanCreateNodeType = getListAllowedFileType(node_, getRepository()) ;
        if(listCanCreateNodeType.contains(nodeType)) attachments.add(childNode);
      } catch (Exception e) {}
    }
    return attachments;
  }  

  public String getRssLink() { return null ; }
  public boolean isRssLink() { return false ; }

  public List getSupportedLocalise() throws Exception {
    List<String> local = new ArrayList<String>() ;
    if(node_.hasNode(Utils.LANGUAGES)){
      Node languages = node_.getNode(Utils.LANGUAGES) ;
      NodeIterator iter = languages.getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(node_.getProperty(Utils.EXO_LANGUAGE).getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception { 
    String nodeTypeName = node_.getPrimaryNodeType().getName();
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    return templateService.getTemplatePathByUser(false, nodeTypeName, userName, getRepository());
  }

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName,getRepository()) ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, "default") ;
  }
  
  public String getIcons(Node node, String appended) throws Exception {
    String nodeType = node.getPrimaryNodeType().getName().replaceAll(":", "_") + appended ;
    StringBuilder str = new StringBuilder(nodeType) ;
    if(node.isNodeType(Utils.NT_FILE)) {
      Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
      str.append(" ").append(jcrContentNode.getProperty(Utils.JCR_MIMETY).getString().replaceFirst("/", "_")).append(appended);
    }
    return str.toString() ;
  }
  
  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getWebDAVServerPrefix() throws Exception {    
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  public String getLanguage() { return language_ ; }
  public void setLanguage(String language) { language_ = language ; }
  
  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class object = loader.loadClass(className);
      service = getApplicationComponent(object);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }
  
  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return null; 
    Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
    InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ; 
  }
  
  public String getWorkspaceName() throws Exception {
    return node_.getSession().getWorkspace().getName();
  }
  
  public String getRepository() throws Exception {
    ManageableRepository manaRepo = (ManageableRepository)node_.getSession().getRepository() ;
    return manaRepo.getConfiguration().getName() ;
  }
  
  static public class ChangeLanguageActionListener extends EventListener<UIDocumentContent> {
    public void execute(Event<UIDocumentContent> event) throws Exception {
      UIDocumentContent uiDocContent = event.getSource() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocContent.getAncestorOfType(UITaskManager.class)) ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiDocContent.setRenderSibbling(UIDocumentContent.class) ;
      uiDocContent.setLanguage(selectedLanguage) ;
    }   
  }
  
  public String encodeHTML(String text) throws Exception {
    return text.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
  }
  
  static  public class DownloadActionListener extends EventListener<UIDocumentContent> {
    public void execute(Event<UIDocumentContent> event) throws Exception {
      UIDocumentContent uiComp = event.getSource() ;
      String downloadLink = uiComp.getDownloadLink(uiComp.getOriginalNode()) ;
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }

  static  public class ChangeNodeActionListener extends EventListener<UIDocumentContent> {
    public void execute(Event<UIDocumentContent> event) throws Exception {
      UIDocumentContent uiComp = event.getSource() ;
      RepositoryService repositoryService  = uiComp.getApplicationComponent(RepositoryService.class) ;
      ManageableRepository repository = repositoryService.getRepository(uiComp.getRepository()) ;
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session = SessionsUtils.getSessionProvider().getSession(workspaceName, repository) ;
      Node selectedNode = (Node) session.getItem(uri) ;
      uiComp.setNode(selectedNode) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComp.getParent()) ;
    }
  }
}