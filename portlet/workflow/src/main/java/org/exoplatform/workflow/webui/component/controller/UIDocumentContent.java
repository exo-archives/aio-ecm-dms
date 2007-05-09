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
import javax.jcr.Value;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
        @EventConfig(listeners = UIDocumentContent.ChangeLanguageActionListener.class)
    }
)
public class UIDocumentContent extends UIContainer implements ECMViewComponent {
  private Node node_ ;
  public static final String DEFAULT_LANGUAGE = "default".intern() ;
  private String language_ = DEFAULT_LANGUAGE ;
  public UIDocumentContent() throws Exception {}
  
  public void setNode(Node node) { this.node_ = node; }
  
  public Node getNode() throws Exception { 
    if(node_.hasProperty("exo:language")) {
      String defaultLang = node_.getProperty("exo:language").getString() ;
      if(!language_.equals(DEFAULT_LANGUAGE) && !language_.equals(defaultLang)) {
        Node curNode = node_.getNode("languages/" + language_) ;
        language_ = defaultLang ;
        return curNode ;
      } 
    }    
    return node_;
  }

  public String getNodeType() throws Exception { return node_.getPrimaryNodeType().getName() ; }
  
  public String getTemplate() {
    UITask task = getAncestorOfType(UITaskManager.class).getChild(UITask.class) ;
    String temp = "" ;
    try {
      if(task.isView()) {
        if(isNodeTypeSupported()) temp = getTemplatePath() ;
        else temp = super.getTemplate() ;
      } else if(task.isCreatedOrUpdated()) temp = task.getDialogPath() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return temp ;
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    UITask task = getAncestorOfType(UITaskManager.class).getChild(UITask.class) ;
    try {
      if((task.isView() && isNodeTypeSupported()) || task.isCreatedOrUpdated()) {
        return new JCRResourceResolver(node_.getSession(), "exo:templateFile") ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String nodeTypeName = node_.getPrimaryNodeType().getName();
      return templateService.isManagedNodeType(nodeTypeName);
    } catch (Exception e) {
      return false;
    }
  }

  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>();
    try {
      Value[] vals = node_.getProperty("exo:relation").getValues();
      for (Value val : vals) {
        String uuid = val.getString();
        Node currentNode = node_.getSession().getNodeByUUID(uuid);
        relations.add(currentNode);
      }
    } catch (Exception e) {}
    return relations;
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
        if ("nt:file".equals(nodeType)) attachments.add(childNode);
      } catch (Exception e) {}
    }
    return attachments;
  }  

  public String getRssLink() { return null ; }
  public boolean isRssLink() { return false ; }

  public List getSupportedLocalise() throws Exception {
    List<String> local = new ArrayList<String>() ;
    if(node_.hasNode("languages")){
      Node languages = node_.getNode("languages") ;
      NodeIterator iter = languages.getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(node_.getProperty("exo:language").getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception { 
    String nodeTypeName = node_.getPrimaryNodeType().getName();
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    return templateService.getTemplatePathByUser(false, nodeTypeName, userName);
  }

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName) ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, "default") ;
  }
  
  public String getIcons(Node node, String appended) throws Exception {
    String nodeType = node.getPrimaryNodeType().getName().replaceAll(":", "_") + appended ;
    StringBuilder str = new StringBuilder(nodeType) ;
    if(node.isNodeType("nt:file")) {
      Node jcrContentNode = node.getNode("jcr:content") ;
      str.append(" ").append(jcrContentNode.getProperty("jcr:mimeType").getString().replaceFirst("/", "_")).append(appended);
    }
    return str.toString() ;
  }
  
  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode("exo:image") ;
    InputStream input = imageNode.getProperty("jcr:data").getStream() ;
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
    if(!node.getPrimaryNodeType().getName().equals("nt:file")) return null; 
    Node jcrContentNode = node.getNode("jcr:content") ;
    InputStream input = jcrContentNode.getProperty("jcr:data").getStream() ;
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


}