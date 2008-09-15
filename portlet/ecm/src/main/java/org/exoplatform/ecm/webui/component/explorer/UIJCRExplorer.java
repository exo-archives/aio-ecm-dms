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
package org.exoplatform.ecm.webui.component.explorer ;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.TypeNodeComparator;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.comparator.DateTimeComparator;
import org.exoplatform.ecm.webui.comparator.NodeNameComparator;
import org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
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

  private PortletPreferences pref_ ;
  private Preference preferences_;
  private Set<String> addressPath_ = new HashSet<String>() ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;  
  
  private String rootPath_ ;
  private String currentPath_ ;
  private String currentStatePath_ ;
  private String currentWorkspaceName_ ;
  private String currentRepositoryName_ ;
  private String documentInfoTemplate_ ;
  private String language_ ;
  private String tagPath_ ;
  private String referenceWorkspace_ ;
  
  private boolean isViewTag_ = false ;
  private boolean isHidePopup_ = false ;
  private boolean isReferenceNode_ = false ;
  private DriveData driveData_ ;
  
  public UIJCRExplorer() throws Exception {
    addChild(UIControl.class, null, null) ;
    addChild(UIWorkingArea.class, null, null) ;
    addChild(UIPopupContainer.class, null, null);
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    pref_ = pcontext.getRequest().getPreferences() ;
  }

  private String filterPath(String currentPath) throws Exception {
    if(getRootNode().getDepth() == 0) return currentPath ;
    if(rootPath_.equals(currentPath_)) return "/" ;
    return currentPath.replaceFirst(rootPath_, "") ;
  }
  
  public void setRootPath(String rootPath) {
    rootPath_ = rootPath ;
    currentPath_ = rootPath ;
  }
  public Node getRootNode() throws Exception { return getNodeByPath(rootPath_, getSession()) ; }

  public String getRootPath() { return rootPath_; }
  
  public Node getCurrentNode() throws Exception { return getNodeByPath(currentPath_, getSession()) ; }
  
  public String getCurrentPath() { return currentPath_ ; }
  public void setCurrentPath(String currentPath) { currentPath_ = currentPath ; }
  
  public boolean isReferenceNode() { return isReferenceNode_ ; }
  public void setIsReferenceNode(boolean isReferenceNode) { isReferenceNode_ = isReferenceNode ; }
  
  public void setReferenceWorkspace(String referenceWorkspace) { referenceWorkspace_ = referenceWorkspace ; }
  public String getReferenceWorkspace() { return referenceWorkspace_ ; }
   
  public void setBackNodePath(String historyPath) throws Exception {
    currentPath_ = historyPath ;    
    refreshExplorer() ;
  }
  
  public void setDriveData(DriveData driveData) { driveData_ = driveData ; }
  public DriveData getDriveData() { return driveData_ ; }

  public void setLanguage(String language) { language_ = language ; }
  public String getLanguage() { return language_ ; }

  public LinkedList<String> getNodesHistory() { return nodesHistory_ ; }
  public void setNodesHistory(LinkedList<String> h) {nodesHistory_ = h;}

  public Set<String> getAddressPath() { return addressPath_ ; }
  public void setAddressPath(Set<String> s) {addressPath_ = s;} ;

  public SessionProvider getSessionProvider() { return SessionProviderFactory.createSessionProvider(); }  

  public SessionProvider getSystemProvider() { return SessionProviderFactory.createSystemProvider(); }  

  public Session getSession() throws Exception { 
    if(isReferenceNode_) return getSessionProvider().getSession(referenceWorkspace_, getRepository()) ;
    return getSessionProvider().getSession(currentWorkspaceName_, getRepository()) ; 
  }
  
  public Session getSystemSession() throws Exception {
    if(isReferenceNode_) return getSystemProvider().getSession(referenceWorkspace_, getRepository()) ;
    return getSystemProvider().getSession(currentWorkspaceName_, getRepository()) ;    
  }
  
  public String getDocumentInfoTemplate() { return documentInfoTemplate_ ; }
  public void setRenderTemplate(String template) { 
    newJCRTemplateResourceResolver() ;
    documentInfoTemplate_  = template ; 
  }
  
  public void setCurrentStatePath(String currentStatePath) { currentStatePath_ =  currentStatePath ; }
  public Node getCurrentStateNode() throws Exception { 
    return getNodeByPath(currentStatePath_, getSession()) ; 
  }

  public JCRResourceResolver getJCRTemplateResourceResolver() { return jcrTemplateResourceResolver_; }
  public void newJCRTemplateResourceResolver() {    
    try{                        
      String workspace =  driveData_.getWorkspace() ;
      jcrTemplateResourceResolver_ = new JCRResourceResolver(currentRepositoryName_, workspace, "exo:templateFile") ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }         
  }

  public void setRepositoryName(String repositoryName) { currentRepositoryName_ = repositoryName ; }
  public String getRepositoryName() { return currentRepositoryName_ ; }
  
  public void setWorkspaceName(String workspaceName) { currentWorkspaceName_ = workspaceName ; }
  public String getCurrentWorkspace() { return currentWorkspaceName_ ; }

  public ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getRepository(currentRepositoryName_);
  }

  public Session getSessionByWorkspace(String wsName) throws Exception{    
    if(wsName == null ) return getSession() ;                      
    return getSessionProvider().getSession(wsName,getRepository()) ;
  }
  
  public boolean isSystemWorkspace() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String systemWS = 
      repositoryService.getRepository(getRepositoryName()).getConfiguration().getSystemWorkspaceName() ;
    if(getCurrentWorkspace().equals(systemWS)) return true ;
    return false ;
  }

  public void refreshExplorer() throws Exception { 
    try {
      getSession().getItem(currentPath_) ;
    } catch(PathNotFoundException path) {
      currentPath_ = getRootNode().getPath() ;
    }
    findFirstComponentOfType(UIAddressBar.class).getUIStringInput(UIAddressBar.FIELD_ADDRESS).
        setValue(filterPath(currentPath_)) ;
    UIDocumentContainer uiDocumentContainer = findFirstComponentOfType(UIDocumentContainer.class) ;
    UIDocumentInfo uiDocumentInfo = uiDocumentContainer.getChild(UIDocumentInfo.class) ;
    uiDocumentInfo.updatePageListData();
    if(isShowViewFile()) uiDocumentInfo.setRendered(false) ;
    else uiDocumentInfo.setRendered(true) ;
    if(preferences_.isShowSideBar()) {
      UITreeExplorer treeExplorer = findFirstComponentOfType(UITreeExplorer.class);
      treeExplorer.buildTree();
    }
    UIPopupContainer popupAction = getChild(UIPopupContainer.class) ;
    popupAction.deActivate() ;
  }

  public boolean nodeIsLocked(String path, Session session) throws Exception {
    Node node = getNodeByPath(path, session) ;
    return nodeIsLocked(node);
  }

  public boolean nodeIsLocked(Node node) throws Exception {
    if(node.isLocked()) {
      return !Utils.isLockTokenHolder(node) ; 
    }
    return false ;
  }

  public boolean hasAddPermission() {
    try {
      ((ExtendedNode)getCurrentNode()).checkPermission(PermissionType.ADD_NODE) ;      
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasEditPermission() {
    try {
      ((ExtendedNode)getCurrentNode()).checkPermission(PermissionType.SET_PROPERTY) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasRemovePermission() {
    try {
      ((ExtendedNode)getCurrentNode()).checkPermission(PermissionType.REMOVE) ;
    } catch(Exception e) {
      return false ;
    }
    return true ;
  }

  public boolean hasReadPermission() {
    try {
      ((ExtendedNode)getCurrentNode()).checkPermission(PermissionType.READ) ;
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
    } catch(Exception e) { 
      e.printStackTrace() ;
    }
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
    uiAddressBar.getUIStringInput(UIAddressBar.FIELD_ADDRESS).setValue(filterPath(currentPath_)) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBar) ;
    UIWorkingArea uiWorkingArea = getChild(UIWorkingArea.class) ;
    UIDocumentWorkspace uiDocWorkspace = uiWorkingArea.getChild(UIDocumentWorkspace.class) ;
    UIDocumentContainer uiDocumentContainer = findFirstComponentOfType(UIDocumentContainer.class) ;
    UIDocumentInfo uiDocumentInfo = uiDocumentContainer.getChild(UIDocumentInfo.class) ;
    if(isShowViewFile()) {
      uiDocumentInfo.updatePageListData();
      uiDocumentInfo.setRendered(false) ;
    } else {
      uiDocumentInfo.setRendered(true) ;
    }
    if(preferences_.isShowSideBar()) {
      findFirstComponentOfType(UITreeExplorer.class).buildTree();
    }
    uiDocWorkspace.setRenderedChild(UIDocumentContainer.class) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;    
    if(!isHidePopup_) {
      UIPopupContainer popupAction = getChild(UIPopupContainer.class) ;
      if(popupAction.isRendered()) {
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }    
    isHidePopup_ = false ;
  }
  
  public boolean isShowViewFile() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    NodeType nodeType = getCurrentNode().getPrimaryNodeType() ;
    NodeType[] superTypes = nodeType.getSupertypes() ;
    boolean isFolder = false ;
    for(NodeType superType : superTypes) {
      if(superType.getName().equals(Utils.NT_FOLDER) || superType.getName().equals(Utils.NT_UNSTRUCTURED)) {
        isFolder = true ;
      }
    }
    if(isFolder && templateService.getDocumentTemplates(getRepositoryName()).contains(nodeType.getName())) {
      return true ;
    }
    return false;
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupContainer popupAction = getChild(UIPopupContainer.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }

  public void record(String str) {
    nodesHistory_.add(str) ;
    addressPath_.add(str) ;
  }

  public String rewind() { return nodesHistory_.removeLast() ; }

  public void setSelectNode(Node node) throws Exception {
    currentPath_ = node.getPath() ;
    Node currentNode = getCurrentNode() ;
    if(currentNode != null && !node.getPath().equals(currentPath_)) record(currentPath_) ;
    if(currentNode.hasProperty(Utils.EXO_LANGUAGE)) {
      setLanguage(currentNode.getProperty(Utils.EXO_LANGUAGE).getValue().getString()) ;
    }    
  }

  public void setSelectNode(String uri, Session session) throws Exception {  
    Node previousNode = null ;
    Node currentNode = getCurrentNode() ;
    if(uri == null || uri.length() == 0) uri = "/" ;
    previousNode = currentNode ;   
    try {
      currentPath_ = uri ;
      currentNode = (Node) session.getItem(uri);
    } catch (Exception e) {
      currentPath_ = currentNode.getParent().getPath() ;
      currentNode = currentNode.getParent() ;
    }    
    if(currentNode.hasProperty(Utils.EXO_LANGUAGE)) {
      setLanguage(currentNode.getProperty(Utils.EXO_LANGUAGE).getValue().getString()) ;
    }
    if(previousNode != null && !currentNode.equals(previousNode)) record(previousNode.getPath()) ;    
  }

  public List<Node> getChildrenList(Node node, boolean isReferences) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    Iterator childrenIterator = node.getNodes() ;
    List<Node> childrenList  = new ArrayList<Node>() ;
    NodeType nodeType = node.getPrimaryNodeType() ;
    NodeType[] superTypes = nodeType.getSupertypes() ;
    boolean isFolder = false ;
    for(NodeType superType : superTypes) {
      if(superType.getName().equals(Utils.NT_FOLDER) || superType.getName().equals(Utils.NT_UNSTRUCTURED)) {
        isFolder = true ;
      }
    }
    if(!preferences_.isJcrEnable() && templateService.isManagedNodeType(nodeType.getName(), currentRepositoryName_) && !isFolder) {
      return childrenList ;
    } 
    if(isReferenceableNode(getCurrentNode()) && isReferences) {
      ManageableRepository manageableRepository = repositoryService.getRepository(currentRepositoryName_) ;
      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
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
      List documentTypes = templateService.getDocumentTemplates(currentRepositoryName_) ;      
      while(childrenIterator.hasNext()){
        Node child = (Node)childrenIterator.next() ;
        if(PermissionUtil.canRead(child)) {
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
        if(PermissionUtil.canRead(child))  childrenList.add(child) ;
      }
    }
    List<Node> childList = new ArrayList<Node>() ;
    if(!preferences_.isShowHiddenNode()) {
      for(Node child : childrenList) {
        if(PermissionUtil.canRead(child) && !child.isNodeType("exo:hiddenable")) {
          childList.add(child) ;
        }
      }
    } else {
      childList = childrenList ;
    }
    sort(childList);
    return childList ;
  }
  
  private void sort(List<Node> childrenList) {
    if(Preference.SORT_BY_NODENAME.equals(preferences_.getSortType())) {
      Collections.sort(childrenList,new NodeNameComparator(preferences_.getOrder())) ;
    }else if(Preference.SORT_BY_NODETYPE.equals(preferences_.getSortType())) {
      Collections.sort(childrenList,new TypeNodeComparator(preferences_.getOrder())) ;
    }else if(Preference.SORT_BY_CREATED_DATE.equals(preferences_.getSortType()))  {
      Collections.sort(childrenList,new DateTimeComparator("exo:dateCreated",preferences_.getOrder()));
    }else if(Preference.SORT_BY_MODIFIED_DATE.equals(preferences_.getSortType())) {
      Collections.sort(childrenList,new DateTimeComparator("exo:dateModified",preferences_.getOrder()));
    }  
  }
  
  public boolean isReferenceableNode(Node node) throws Exception {
    return node.isNodeType(Utils.MIX_REFERENCEABLE) ;    
  }

  public boolean isPreferenceNode(Node node) {
    try {
      return (getCurrentNode().hasNode(node.getName())) ? false : true ;
    } catch(Exception e) {
      return false ;
    }
  }

  public Node getNodeByPath(String nodePath, Session session) throws Exception {    
    try {
      return (Node)session.getItem(nodePath) ;
    } catch(PathNotFoundException e) {
      refreshExplorer() ;
      return (Node)session.getItem(rootPath_) ;
    } catch(Exception e) {
      e.printStackTrace() ;
      refreshExplorer() ;
      return (Node)session.getItem(rootPath_) ;
    }
  }
  
  public void setTagPath(String tagPath) { tagPath_ = tagPath ; }
  
  public String getTagPath() { return tagPath_ ; }
  
  public List<Node> getDocumentByTag()throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<String> documentsType = templateService.getDocumentTemplates(getRepositoryName()) ;
    List<Node> documentsOnTag = new ArrayList<Node>() ;
    for(Node node : folksonomyService.getDocumentsOnTag(tagPath_, getRepositoryName())) {
      if(documentsType.contains(node.getPrimaryNodeType().getName())) {
        documentsOnTag.add(node) ;
      }
    }
    return documentsOnTag ;
  }
  
  public void setIsViewTag(boolean isViewTag) { isViewTag_ = isViewTag ; }
  
  public boolean isViewTag() { return isViewTag_ ; }

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
    
  public Preference getPreference() { return preferences_; }  
  public void setPreferences(Preference preference) {this.preferences_ = preference; } 
  
  public String getPreferencesPath() {
//    PortletPreferences prefs_ = getPortletPreferences() ;
//    String prefPath = prefs_.getValue(Utils.JCR_PATH, "") ;
    String prefPath = driveData_.getHomePath() ;
    if (prefPath == null || prefPath.length() == 0 || prefPath == "/") return "" ;
    return prefPath ;
  }

  public String getPreferencesWorkspace() {       
//    PortletPreferences prefs_ = getPortletPreferences() ;
//    String workspaceName = prefs_.getValue(Utils.WORKSPACE_NAME, "") ;
    String workspaceName = driveData_.getWorkspace() ;
    if(workspaceName == null || workspaceName.length() == 0) return "" ;
    return workspaceName ;
  }
}