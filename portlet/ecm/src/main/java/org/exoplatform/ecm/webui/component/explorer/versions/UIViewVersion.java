/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : lxchiati  
 *          lebienthuy@gmail.com
 * Oct 19, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
  type     = UIViewVersion.class,
  template = "system:groovy/webui/core/UITabPane.gtmpl",
  events = {@EventConfig(listeners = UIViewVersion.ChangeLanguageActionListener.class)}
)

public class UIViewVersion extends UIContainer implements ECMViewComponent {
  private Node node_ ;
  protected Node originalNode_ ;
  private String language_ ;
  
  public UIViewVersion() throws Exception {    
    addChild(UINodeInfo.class, null, null) ;
    addChild(UINodeProperty.class, null, null).setRendered(false) ;
  } 
 
  public String getTemplate() {
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      String nodeType = node.getPrimaryNodeType().getName();
      if(isNodeTypeSupported(node)) return templateService.getTemplatePathByUser(false, nodeType, userName, getRepository()) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplate() ;
  }
   
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    try {
      if(isNodeTypeSupported(node)) return new JCRResourceResolver(node.getSession(), "exo:templateFile") ;
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }

  public boolean isNodeTypeSupported(Node node) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String nodeTypeName = node.getPrimaryNodeType().getName();
      return templateService.isManagedNodeType(nodeTypeName, getRepository());
    } catch (Exception e) {
      return false;
    }
  }
  
  public Node getNode() throws RepositoryException {
    if(node_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = node_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(language_ == null) language_ = defaultLang ;
      if(!language_.equals(defaultLang)) {
        Node curNode = node_.getNode(Utils.LANGUAGES + Utils.SLASH + language_) ;
        return curNode ;
      } 
    }    
    return node_;
  }
  
  public Node getOriginalNode() throws Exception {return  originalNode_ ;}
  
  public void setNode(Node node) {node_ = node ;}
  
  public Node getNodeByUUID(String uuid) throws Exception{
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    String[] workspaces = manageRepo.getWorkspaceNames() ;
    for(String ws : workspaces) {
      try{
        return manageRepo.getSystemSession(ws).getNodeByUUID(uuid) ;
      }catch(Exception e) {
        
      }      
    }
    return null;
  }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (node_.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = node_.getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = node_.getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if (Utils.NT_FILE.equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }
  
  public String getIcons(Node node, String type) throws Exception {
    return Utils.getNodeTypeIcon(node, type) ; 
  }
  public boolean hasPropertyContent(Node node, String property){
    try {
      String value = node.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    } catch (Exception e) {
      e.printStackTrace() ;      
    }
    return false ;
  }
  
  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }
  
  public void update() throws Exception {    
    getChild(UINodeInfo.class).update();
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, getLanguage()) ;
  }

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

  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public void setLanguage(String language) { language_ = language ; }
  public String getLanguage() { return language_ ; }

  public String getNodeType() throws Exception {
    return node_.getPrimaryNodeType().getName() ;
  }

  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ; 
  }

  public List getSupportedLocalise() throws Exception {
    List<String> local = new ArrayList<String>() ;
    if(node_.hasNode(Utils.LANGUAGES)){
      NodeIterator iter = node_.getNode(Utils.LANGUAGES).getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(node_.getProperty(Utils.EXO_LANGUAGE).getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, getRepository()) ;
  }

  public String getWebDAVServerPrefix() throws Exception {
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  public String getWorkspaceName() throws Exception {
    return node_.getSession().getWorkspace().getName();
  }

  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      return templateService.isManagedNodeType(getNodeType(), getRepository());
    } catch (Exception e) {
      return false;
    }
  }

  public String getRepository() throws Exception{
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }
  
  static public class ChangeLanguageActionListener extends EventListener<UIViewVersion>{
    public void execute(Event<UIViewVersion> event) throws Exception {
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIViewVersion uiViewVersion = event.getSource() ;
      UIApplication uiApp = uiViewVersion.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIViewVersion.msg.not-supported", null)) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      uiViewVersion.setLanguage(selectedLanguage) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiViewVersion) ;
    }
    
  }

  public String encodeHTML(String text) throws Exception {
    return Utils.encodeHTML(text) ;
  }
}