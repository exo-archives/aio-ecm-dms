/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer ;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig( lifecycle = UIContainerLifecycle.class )
public class UIJCRExplorer extends UIContainer {

  private LinkedList<ClipboardCommand> clipboards_ = new LinkedList<ClipboardCommand>() ;
  private LinkedList<String> nodesHistory_ = new LinkedList<String>() ;

  private Session session_ = null ;
  private PortletPreferences pref_ ;
  private Preference preferences_ = new Preference() ;
  private Set<String> addressPath_ = new HashSet<String>() ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;  
  private Node rootNode_ ;
  private Node currentNode_ ;

  private String documentInfoTemplate_ ;
  public boolean isHidePopup_ = false ;
  private String language_ ;

  public UIJCRExplorer() throws Exception {
    addChild(UIControl.class, null, null) ;
    addChild(UIWorkingArea.class, null, null) ;
    addChild(UIPopupAction.class, null, null);
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    pref_ = pcontext.getRequest().getPreferences() ;
  }

  private String filterPath(String currentPath) throws RepositoryException {
    if(rootNode_.getDepth() == 0) return currentPath ;
    if(rootNode_.equals(currentNode_)) return "/" ;
    return currentPath.replaceFirst(rootNode_.getPath(), "") ;
  }

  public void setRootNode(Node node) {
    rootNode_ = node ;
    currentNode_ = node ;
  }
  public Node getRootNode() { return rootNode_ ; }

  public Node getCurrentNode() { return currentNode_ ; }
  public void setBackNode(String historyNode) throws Exception {
    currentNode_ = (Node)session_.getItem(historyNode) ;
    refreshExplorer() ;
  }

  public void setLanguage(String language) { language_ = language ; }
  public String getLanguage() { return language_ ; }

  public LinkedList<String> getNodesHistory() { return nodesHistory_ ; }
  public void setNodesHistory(LinkedList<String> h) {nodesHistory_ = h;}

  public Set<String> getAddressPath() { return addressPath_ ; }
  public void setAddressPath(Set<String> s) {addressPath_ = s;} ;

  public SessionProvider getSessionProvider() { return SessionsUtils.getSessionProvider() ; }  
  
  public SessionProvider getSystemProvider() { return SessionsUtils.getSystemProvider() ; }  

  public Session getSession() { return session_ ; }
  public void setSession(Session session) { this.session_ = session ; }  

  public String getDocumentInfoTemplate() { return documentInfoTemplate_ ; }
  public void setRenderTemplate(String template) { 
    newJCRTemplateResourceResolver() ;
    documentInfoTemplate_  = template ; 

  }

  public JCRResourceResolver getJCRTemplateResourceResolver() { return jcrTemplateResourceResolver_; }
  public void newJCRTemplateResourceResolver() {    
    try{                        
      String workspace =  getPortletPreferences().getValue(Utils.WORKSPACE_NAME,"") ; // repository.getConfiguration().get .getDefaultWorkspaceName() ;
      Session session = getSystemProvider().getSession(workspace,getRepository());        
      jcrTemplateResourceResolver_ = new JCRResourceResolver(session, "exo:templateFile") ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }         
  }

  public String getRepositoryName() { return pref_.getValue(Utils.REPOSITORY,"") ; }

  private ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getRepository(getRepositoryName());
  }

  public Session getSessionByWorkspace(String wsName) throws Exception{    
    if(wsName == null ) return getSession() ;                      
    return getSessionProvider().getSession(wsName,getRepository()) ;
  }

  public void refreshExplorer() throws Exception { 
    findFirstComponentOfType(UIAddressBar.class).getUIStringInput(UIAddressBar.FIELD_ADDRESS).
    setValue(filterPath(currentNode_.getPath())) ;
    UIWorkingArea workingArea = getChild(UIWorkingArea.class) ;
    workingArea.getChild(UIDocumentWorkspace.class).setRenderedChild(UIDocumentInfo.class) ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
  }

  public boolean nodeIsLocked(String path, Session session) throws Exception {
    Node node = getNodeByPath(path, session) ;
    if(node.isLocked()) {
      return !Utils.isLockTokenHolder(node) ; 
    }
    return false ;
  }
  
  public boolean nodeIsLocked(Node node) throws Exception {
    if(node.isLocked()) {
      return !Utils.isLockTokenHolder(node) ; 
    }
    return false ;
  }
  
  public boolean hasAddPermission() {
    try {
      session_.checkPermission(currentNode_.getPath(), PermissionType.ADD_NODE) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasEditPermission() {
    try {
      session_.checkPermission(currentNode_.getPath(), PermissionType.SET_PROPERTY) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasRemovePermission() {
    try {
      session_.checkPermission(currentNode_.getPath(), PermissionType.REMOVE) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasReadPermission() {
    try {
      session_.checkPermission(currentNode_.getPath(), PermissionType.READ) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public Node getViewNode(String nodeType) throws Exception { 
    try {
      Item primaryItem = getCurrentNode().getPrimaryItem() ;
      if(primaryItem == null || !primaryItem.isNode()) return getCurrentNode() ;
      if(primaryItem != null && primaryItem.isNode()) {
        Node primaryNode = (Node) primaryItem ;
        if(primaryNode.isNodeType(nodeType)) return primaryNode ;
      }
    } catch(Exception e) { }
    return getCurrentNode() ;
  }

  public List<String> getMultiValues(Node node, String name) throws Exception {
    List<String> list = new ArrayList<String>();
    if(!node.hasProperty(name)) return list;
    if (!node.getProperty(name).getDefinition().isMultiple()) {
      try {
        list.add(node.getProperty(name).getString());
      } catch(Exception e) {
        list.add("") ;
      }
      return list;
    }
    Value[] values = node.getProperty(name).getValues();
    for (Value value : values) {
      list.add(value.getString());
    }
    return list;
  }

  public void setIsHidePopup(boolean isHidePopup) { isHidePopup_ = isHidePopup ; }

  public void updateAjax(Event event) throws Exception { 
    UIAddressBar uiAddressBar = findFirstComponentOfType(UIAddressBar.class) ;
    uiAddressBar.getUIStringInput(UIAddressBar.FIELD_ADDRESS).setValue(filterPath(currentNode_.getPath())) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBar) ;
    UIWorkingArea uiWorkingArea = getChild(UIWorkingArea.class) ;
    UIDocumentWorkspace uiDocWorkspace = uiWorkingArea.getChild(UIDocumentWorkspace.class) ;
    uiDocWorkspace.setRenderedChild(UIDocumentInfo.class) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    if(!isHidePopup_) {
      UIPopupAction popupAction = getChild(UIPopupAction.class) ;
      if(popupAction.isRendered()) {
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
    isHidePopup_ = false ;
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }

  public String getCurrentWorkspace() { return session_.getWorkspace().getName() ; }

  public void record(String str) {
    nodesHistory_.add(str) ;
    addressPath_.add(str) ;
  }

  public String rewind() { return nodesHistory_.removeLast() ; }

  public void setSelectNode(Node node) throws Exception {
    if(currentNode_ != null && !node.equals(currentNode_)) record(currentNode_.getPath()) ;
    currentNode_ = node ;
    if(currentNode_.hasProperty(Utils.EXO_LANGUAGE)) {
      setLanguage(currentNode_.getProperty(Utils.EXO_LANGUAGE).getValue().getString()) ;
    }
  }

  public void setSelectNode(String uri, Session session) throws Exception {  
    Node previousNode = null ;
    if(uri == null || uri.length() == 0) uri = "/" ;
    previousNode = currentNode_ ;        
    currentNode_ = (Node) session.getItem(uri);
    if(currentNode_.hasProperty(Utils.EXO_LANGUAGE)) {
      setLanguage(currentNode_.getProperty(Utils.EXO_LANGUAGE).getValue().getString()) ;
    }
    if(previousNode != null && !currentNode_.equals(previousNode)) record(previousNode.getPath()) ;
  }

  public List<Node> getChildrenList(Node node, boolean isReferences) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    Iterator childrenIterator = node.getNodes() ;
    List<Node> childrenList  = new ArrayList<Node>() ;
    NodeType nodeType = node.getPrimaryNodeType() ;
    if(!preferences_.isJcrEnable() && templateService.isManagedNodeType(nodeType.getName(), repository)) {
      return childrenList ;
    } 
    if(isReferenceableNode(getCurrentNode()) && isReferences) {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
      //TODO use normal SessionProvider
      SessionProvider sessionProvider = SessionsUtils.getSystemProvider();
      for(String workspace:manageableRepository.getWorkspaceNames()) {
        Session session = sessionProvider.getSession(workspace,manageableRepository) ;
        try {
          Node taxonomyNode = session.getNodeByUUID(getCurrentNode().getUUID()) ;
          PropertyIterator categoriesIter = taxonomyNode.getReferences() ;
          while(categoriesIter.hasNext()) {
            Property exoCategoryProp = categoriesIter.nextProperty();
            Node refNode = exoCategoryProp.getParent() ;
            childrenList.add(refNode) ;            
          }
        } catch(Exception e) {
          continue ;
        }
      }
    }
    if(!preferences_.isShowNonDocumentType()) {
      List documentTypes = templateService.getDocumentTemplates(repository) ;      
      while(childrenIterator.hasNext()){
        Node child = (Node)childrenIterator.next() ;
        if(Utils.isReadAuthorized(child)) {
          NodeType type = child.getPrimaryNodeType() ;
          if(Utils.NT_UNSTRUCTURED.equals(type.getName()) || Utils.NT_FOLDER.equals(type.getName())) {
            childrenList.add(child) ;
          } else if(documentTypes.contains(type.getName())) {
            childrenList.add(child) ;
          }
        }
      }
    } else {
      while(childrenIterator.hasNext()) {
        Node child = (Node)childrenIterator.next() ;
        if(Utils.isReadAuthorized(child)) {
          childrenList.add(child) ;
        }
      }
    }
    return childrenList ;
  }

  public boolean isReferenceableNode(Node node) throws Exception {
    return node.isNodeType(Utils.MIX_REFERENCEABLE) ;    
  }

  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return (getCurrentNode().hasNode(node.getName())) ? false : true ;
  }

  public Node getNodeByPath(String nodePath, Session session) throws Exception {
    return (Node)session.getItem(nodePath) ;    
  }

  public LinkedList<ClipboardCommand> getAllClipBoard() { return clipboards_ ;}

  public PortletPreferences getPortletPreferences() { return pref_ ; }

  public boolean isReadAuthorized(ExtendedNode node) throws RepositoryException {
    try {
      node.checkPermission(PermissionType.READ);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }  

  public Preference getPreference() throws Exception { 
    if(preferences_.isEmpty()){
      ManageDriveService dservice = getApplicationComponent(ManageDriveService.class) ;
      PortletPreferences portletPref = getPortletPreferences() ; 
      String driveName = portletPref.getValue(Utils.DRIVE, "") ;
      try{
        String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
        DriveData drive = (DriveData)dservice.getDriveByName(driveName, repository) ;
        preferences_.setShowSideBar(drive.getViewSideBar()) ;
        preferences_.setShowNonDocumentType(drive.getViewNonDocument()) ;
        preferences_.setShowPreferenceDocuments(drive.getViewPreferences()) ;
        preferences_.setAllowCreateFoder(drive.getAllowCreateFolder()) ;
        preferences_.setEmpty(false) ;        
      } catch(Exception e) {
        preferences_.setEmpty(true) ;
        return preferences_ ;
      }
    }
    return preferences_; 
  }

  public String getPreferencesPath() {
    PortletPreferences prefs_ = getPortletPreferences() ;
    String prefPath = prefs_.getValue(Utils.JCR_PATH, "") ;
    if (prefPath == null || prefPath.length() == 0 || prefPath == "/") return "" ;
    return prefPath ;
  }

  public String getPreferencesWorkspace() {       
    PortletPreferences prefs_ = getPortletPreferences() ;
    String workspaceName = prefs_.getValue(Utils.WORKSPACE_NAME, "") ;
    if(workspaceName == null || workspaceName.length() == 0) return "" ;
    return workspaceName ;
  }
}