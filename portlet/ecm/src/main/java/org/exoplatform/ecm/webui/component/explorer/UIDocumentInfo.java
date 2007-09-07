/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.AlphaNodeComparator;
import org.exoplatform.ecm.jcr.DateTimeComparator;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.TypeNodeComparator;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *          phamtuanchip@gmail.com
 * Nov 10, 2006         
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UIDocumentInfo.ChangeNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ViewNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.SortActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.VoteActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ChangeLanguageActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.DownloadActionListener.class)
    }
)
public class UIDocumentInfo extends UIContainer implements ECMViewComponent {
  
  private String typeSort_ = Preference.SORT_BY_NODETYPE;
  private String typeSortOrder_ = Preference.ASCENDING_ORDER;
  private String nameSortOrder_ = Preference.ASCENDING_ORDER;
  private Node currentNode_ ;
  
  private UIPageIterator pageIterator_ ;  
  
  public UIDocumentInfo() throws Exception {
    pageIterator_ = addChild(UIPageIterator.class, null, null) ;        
  }
  
  public UIPageIterator getContentPageIterator() {return pageIterator_ ; }
  
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    String repository = uiExplorer.getRepositoryName();
    try {
      String nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType().getName() ;
      if(uiExplorer.getPreference().isJcrEnable()) {
        return uiExplorer.getDocumentInfoTemplate(); 
      } else if(isNodeTypeSupported(nodeType)) {
        return templateService.getTemplatePathByUser(false, nodeType, userName, repository) ; 
      }
    } catch(AccessDeniedException ace) {
      try {
        uiExplorer.setSelectNode(uiExplorer.getRootNode()) ;
        Object[] args = { uiExplorer.getCurrentNode().getName() } ;
        throw new MessageException(new ApplicationMessage("UIDocumentInfo.msg.access-denied", args, ApplicationMessage.WARNING)) ;
      } catch(Exception exc) {
        exc.printStackTrace() ;
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return uiExplorer.getDocumentInfoTemplate(); 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
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

  public List<String> getMultiValues(Node node, String name) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getMultiValues(node, name) ;
  }
  
  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;    
    if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return null; 
    Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
    InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
    String mimeType = jcrContentNode.getProperty(Utils.JCR_MIMETYPE).getString() ;
    InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, mimeType) ;
    MimeTypeResolver mimeTypeResolver = new MimeTypeResolver() ;
    String ext = mimeTypeResolver.getExtension(mimeType) ;
    String fileName = node.getName() ;    
    if(fileName.lastIndexOf("."+ext)<0){
      fileName = fileName + "." +ext ;
    } 
    dresource.setDownloadName(fileName) ;
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
  
  public Node getViewNode(String nodeType) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode().getNode(nodeType) ;
  }
  
  public String getActionsList(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getActionsList(node) ;
  }
  
  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }
  
  public boolean isPreferenceNode(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).isPreferenceNode(node) ;
  }
  
  public boolean isReadAuthorized(ExtendedNode node) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).isReadAuthorized(node) ;
  }
  
  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(className);
      service = getApplicationComponent(clazz);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }
  
  public String getNodeOwner(Node node) throws RepositoryException {
    if(node.hasProperty("exo:owner")) {
      return node.getProperty("exo:owner").getString();
    }
    return SystemIdentity.ANONIM ;
  }
  
  public Date getDateCreated(Node node) throws Exception{
    if(node.hasProperty("exo:dateCreated")) {
      return node.getProperty("exo:dateCreated").getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }
  
  public Date getDateModified(Node node) throws Exception {
    if(node.hasProperty("exo:dateModified")) {
      return node.getProperty("exo:dateModified").getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (currentNode_.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = currentNode_.getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }
  
  public List<Node> getAttachments() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = currentNode_.getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      List<String> listCanCreateNodeType = 
        Utils.getListAllowedFileType(currentNode_, getRepository(), templateService) ; 
      if(listCanCreateNodeType.contains(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
      return templateService.isManagedNodeType(nodeTypeName, repository);
    } catch (Exception e) {
      return false;
    }
  }
  
  public String getNodeType() throws Exception { return null; }

  public List<String> getSupportedLocalise() throws Exception {
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    return multiLanguageService.getSupportedLanguages(currentNode_) ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }
  
  public String getVersionName(Node node) throws Exception {
    return node.getBaseVersion().getName() ;
  }
  
  public void setNode(Node node) { currentNode_ = node ; }
  
  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  public String getRepository() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
  }
  
  public String getWorkspaceName() throws Exception {
    return currentNode_.getSession().getWorkspace().getName();
  }

  public Node getNode() throws Exception { 
    currentNode_ = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    if(!Utils.isReadAuthorized(currentNode_)) {
      System.out.println("\n\nGo here\n\n");
    }
    if(currentNode_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = currentNode_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(getLanguage() == null) setLanguage(defaultLang) ;
      if(!getLanguage().equals(defaultLang)) {
        Node curNode = currentNode_.getNode(Utils.LANGUAGES + Utils.SLASH + getLanguage()) ;
        return curNode ;
      }
    }    
    return currentNode_; 
  }
  
  public Node getOriginalNode() throws Exception {return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;}
  
  public String getIcons(Node node, String size) throws Exception {
    return Utils.getNodeTypeIcon(node, size) ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(currentNode_, getLanguage()) ;
  }

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, repository) ;
  }
  
  public String getLanguage() {
    return getAncestorOfType(UIJCRExplorer.class).getLanguage() ;
  }

  public void setLanguage(String language) { 
    getAncestorOfType(UIJCRExplorer.class).setLanguage(language) ;
  }
  
  public void updatePageListData() throws Exception {    
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    Node currentNode = uiExplorer.getCurrentNode();
    List<Node> childrenList = uiExplorer.getChildrenList(currentNode,pref.isShowPreferenceDocuments());
    int nodesPerPage = pref.getNodesPerPage();
    if(Preference.SORT_BY_NODENAME.equals(pref.getSortType())) {
      Collections.sort(childrenList,new AlphaNodeComparator(pref.getOrder())) ;
    }else if(Preference.SORT_BY_NODETYPE.equals(pref.getSortType())) {
      Collections.sort(childrenList,new TypeNodeComparator(pref.getOrder())) ;
    }else if(Preference.SORT_BY_CREATED_DATE.equals(pref.getSortType()))  {
      Collections.sort(childrenList,new DateTimeComparator("exo:dateCreated",pref.getOrder()));
    }else if(Preference.SORT_BY_MODIFIED_DATE.equals(pref.getSortType())) {
      Collections.sort(childrenList,new DateTimeComparator("exo:dateModified",pref.getOrder()));
    }
    PageList pageList = new ObjectPageList(childrenList,nodesPerPage) ;
    pageIterator_.setPageList(pageList) ;
  }
  
  public List<Node> getChildrenList() throws Exception {
    List<Node> list = pageIterator_.getCurrentPageData();    
    return list;    
  }
  
  public String getTypeSort() { return typeSort_ ; }
  public String getTypeSortOrder() { return typeSortOrder_ ; }
  public String getNameSortOrder() { return nameSortOrder_ ; }
  
  public String encodeHTML(String text) { return Utils.encodeHTML(text) ; }
  
  static  public class ViewNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {      
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);      
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;      
      Session session ;
      if(workspaceName == null ) {
        session = uiExplorer.getSession() ;
      } else {
        String repository = uicomp.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
        RepositoryService repositoryService  = uicomp.getApplicationComponent(RepositoryService.class) ;
        ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
        SessionProvider provider = SessionsUtils.getSessionProvider() ;
        session = provider.getSession(workspaceName,manageableRepository) ;
      }
      uiExplorer.setSelectNode(uri, session) ;
      uiExplorer.updateAjax(event) ;           
      event.broadcast();      
    }
  }

  static  public class ChangeNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {      
      UIDocumentInfo uicomp =  event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ; 
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session = uiExplorer.getSessionByWorkspace(workspaceName) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      String prefPath = uiExplorer.getPreferencesPath() ;
      String prefWorkspace = uiExplorer.getPreferencesWorkspace() ;
      if((prefPath.length() > 0) && (uiExplorer.getCurrentWorkspace().equals(prefWorkspace))) {
        try {
          if ((".." + prefPath).equals(uri)) {
            if (prefPath.equals(uiExplorer.getCurrentNode().getPath())) {
              uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
              uiExplorer.updateAjax(event) ;
            }
          } else {
            uiExplorer.setSelectNode(uri, session);
            uiExplorer.updateAjax(event) ;
          }
        } catch(Exception e) {
          e.printStackTrace() ;
          JCRExceptionManager.process(uiApp, e);
        }
      } else {
        try {
          if ("../".equals(uri)) {
            if (!"/".equals(uiExplorer.getCurrentNode().getPath())) {
              uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
              uiExplorer.updateAjax(event) ;
            }
          } else {
            uiExplorer.setSelectNode(uri, session);
            uiExplorer.updateAjax(event) ;
          }
        } catch(Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      }      
    }        
  }

  static  public class SortActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String sortParam = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String[] array = sortParam.split(";") ;
      Preference pref = uiExplorer.getPreference() ;
      if(array[0].trim().equals(Preference.SORT_BY_NODETYPE)) {
        if(array[1].trim().equals(Preference.ASCENDING_ORDER)) {
          uicomp.typeSortOrder_ = Preference.ASCENDING_ORDER ;
        } else if(array[1].trim().equals(Preference.DESCENDING_ORDER)) {
          uicomp.typeSortOrder_ = Preference.DESCENDING_ORDER ;
        }
        uicomp.typeSort_ = Preference.SORT_BY_NODETYPE ;
      } else if(array[0].trim().equals(Preference.SORT_BY_NODENAME)) {
        if(array[1].trim().equals(Preference.ASCENDING_ORDER)) {
          uicomp.nameSortOrder_ = Preference.ASCENDING_ORDER ;
        } else if(array[1].trim().equals(Preference.DESCENDING_ORDER)) {
          uicomp.nameSortOrder_ = Preference.DESCENDING_ORDER ;
        }
        uicomp.typeSort_ = Preference.SORT_BY_NODENAME ;
      }
      if(array.length == 2) {
        pref.setSortType(array[0].trim()) ;
        pref.setOrder(array[1].trim()) ; 
      } else if(array.length == 3) {
        pref.setSortType(array[0].trim()) ;
        //pref.setProperty(array[1].trim()) ;
        pref.setOrder(array[2].trim()) ;
      } else {
        return ;
      }       
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class ChangeLanguageActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiDocumentInfo = event.getSource() ;
      UIJCRExplorer uiExplorer = uiDocumentInfo.getAncestorOfType(UIJCRExplorer.class) ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiExplorer.setLanguage(selectedLanguage) ;
      uiExplorer.updateAjax(event) ;
    }   
  }
  
  static  public class VoteActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiComp = event.getSource() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      double objId = Double.parseDouble(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiComp.getApplicationComponent(VotingService.class) ;
      votingService.vote(uiComp.currentNode_, objId, userName, uiComp.getLanguage()) ;
    }
  }
  
  static  public class DownloadActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiComp = event.getSource() ;
      String downloadLink = uiComp.getDownloadLink(uiComp.getOriginalNode()) ;
      event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript("ajaxRedirect('" + downloadLink + "');");
    }
  }
}