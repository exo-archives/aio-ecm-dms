/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
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
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          phamtuanchip@gmail.com
 * Jan 15, 2007  
 */

@ComponentConfig(
    events ={ 
        @EventConfig(listeners =  UIDocumentDetail.ChangeLanguageActionListener.class),
        @EventConfig(listeners =  UIDocumentDetail.ChangeNodeActionListener.class),
        @EventConfig(listeners =  UIDocumentDetail.DownloadActionListener.class)
    }
)

public class UIDocumentDetail extends UIComponent implements ECMViewComponent, UIPopupComponent{
  protected Node node_ ;
  private String language_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;

  public UIDocumentDetail() {} 

  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    if(isValidNode()) {
      super.processRender(arg0);
    } else {
      if(uiContainer.usecase_.equals(Utils.CB_USE_DOCUMENT)) {
        uiContainer.setRendered(false) ;
      }
      if(uiContainer.isShowDocumentByTag_ && uiContainer.isShowDocumentDetail_) {
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
      } else {
        uiContainer.isShowDocumentByTag_ = false ;
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
        if(uiContainer.usecase_.equals(Utils.CB_USE_FROM_PATH) && uiContainer.history_ != null) {
          uiContainer.setCurrentNode(uiContainer.history_.get(UIBrowseContainer.KEY_CURRENT)) ;
          uiContainer.setSelectedTab(uiContainer.history_.get(UIBrowseContainer.KEY_SELECTED)) ;
          uiContainer.history_.clear() ;
        }
      }

    }
    arg0.addUIComponentToUpdateByAjax(uiContainer) ;
  }
  private boolean isValidNode() throws Exception  {
    if(node_ == null) return false ;
    if(getAncestorOfType(UIBrowseContainer.class).getNodeByPath(node_.getPath()) == null) return false ;
    return true ;
  }
  public String getTemplatePath(){
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    try{
      return templateService.getTemplatePathByUser(false, getNodeType(), userName, repository) ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }    
    return null ;
  }

  public String getIcons(Node node, String type) throws Exception {
    return Utils.getNodeTypeIcon(node, type) ; 
  }

  public String getTemplate(){ return getTemplatePath() ;}

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ;
    return jcrTemplateResourceResolver_ ;
  }

  public void newJCRTemplateResourceResolver() {
    try{
      String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;            
      ManageableRepository mRepository = 
        getApplicationComponent(RepositoryService.class).getRepository(repository) ;
      String systemWorkspace = mRepository.getConfiguration().getSystemWorkspaceName() ;
      Session session = SessionsUtils.getSystemProvider().getSession(systemWorkspace,mRepository) ;
      jcrTemplateResourceResolver_ = new JCRResourceResolver(session, Utils.EXO_TEMPLATEFILE) ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }     
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

  public Node getNode() throws Exception { 
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

  public Node getOriginalNode() throws Exception {return node_ ;}

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }

  public void setLanguage(String language) { language_ = language ; }
  public String getLanguage() { return language_ ; }

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

  public String getWebDAVServerPrefix() throws Exception {    
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  public void setNode(Node docNode) {node_ = docNode ;}

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

  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
      return templateService.isManagedNodeType(getNodeType(), repository);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
      return templateService.isManagedNodeType(nodeTypeName, repository);
    } catch (Exception e) {
      return false;
    }
  }

  public Node getNodeByUUID(String uuid) throws Exception{ 
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    String[] workspaces = manageRepo.getWorkspaceNames() ;
    SessionProvider provider = SessionsUtils.getSessionProvider() ;
    for(String ws : workspaces) {
      try{
        return provider.getSession(ws,manageRepo).getNodeByUUID(uuid) ;
      }catch(Exception e) { }      
    }
    return null;
  }

  public boolean hasPropertyContent(Node node, String property){
    try {
      return node.hasProperty(property) ;
    } catch (Exception e) {
      return false ;
    }

  }

  public String getNodeType() throws Exception {
    return node_.getPrimaryNodeType().getName() ;
  }

  public String getRssLink() {return null ;}

  public boolean isRssLink() {return false ;}

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, getLanguage()) ;
  }

  public List<String> getSupportedLocalise() throws Exception {
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

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, repository) ;
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}

  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ; 
  }

  public String getRepository() throws Exception {
    return ((ManageableRepository)node_.getSession().getRepository()).getConfiguration().getName() ;
  }

  public String getWorkspaceName() throws Exception {
    return node_.getSession().getWorkspace().getName();
  }

  public String encodeHTML(String text) throws Exception {
    return Utils.encodeHTML(text) ;
  }

  static public class ChangeLanguageActionListener extends EventListener<UIDocumentDetail>{
    public void execute(Event<UIDocumentDetail> event) throws Exception {
      UIDocumentDetail uiDocument = event.getSource() ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiDocument.setLanguage(selectedLanguage) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocument) ;
    }
  }

  static public class ChangeNodeActionListener extends EventListener<UIDocumentDetail>{
    public void execute(Event<UIDocumentDetail> event) throws Exception {
      UIDocumentDetail uiDocument = event.getSource() ;
      UIBrowseContentPortlet cbPortlet = uiDocument.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
      uiPopupAction.deActivate() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Node node = null ;
      UIBrowseContainer uiContainer = cbPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;     
      if(wsName != null) {
        String repository = uiDocument.getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;        
        ManageableRepository manageableRepository = 
          uiDocument.getApplicationComponent(RepositoryService.class).getRepository(repository) ;        
        Session session = null ;
        if(path.indexOf("/jcr:system")>0) {
          session = SessionsUtils.getSystemProvider().getSession(wsName,manageableRepository) ;
        }else {
          if(SessionsUtils.isAnonim()) {
            session = SessionsUtils.getAnonimProvider().getSession(wsName,manageableRepository) ;
          }else {
            session = SessionsUtils.getSessionProvider().getSession(wsName,manageableRepository) ;
          }
        } 
        node = (Node)session.getItem(path) ;
      }else {
        node = uiContainer.getNodeByPath(path) ;
      }
      UIDocumentDetail uiDocumentView =  uiPopupAction.activate(UIDocumentDetail.class, 600) ;
      uiDocumentView.setNode(node) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static  public class DownloadActionListener extends EventListener<UIDocumentDetail> {
    public void execute(Event<UIDocumentDetail> event) throws Exception {
      UIDocumentDetail uiComp = event.getSource() ;
      String downloadLink = uiComp.getDownloadLink(uiComp.getOriginalNode()) ;
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }
}