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
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
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
  private Preference preferences_ = new Preference() ;
  private Set<String> addressPath_ = new HashSet<String>() ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;
  private Node currentNode_ ;
  private String documentInfoTemplate_ ;
  public boolean isHidePopup_ = false ;

  public UIJCRExplorer() throws Exception {
    addChild(UIPopupAction.class, null, null) ;
    addChild(UIControl.class, null, null) ;
    addChild(UIWorkingArea.class, null, null) ;
  }
  
  public Node getCurrentNode() { return currentNode_ ; }
  public void setBackNode(String historyNode) throws Exception {
    currentNode_ = (Node)session_.getItem(historyNode) ;
    refreshExplorer() ;
  }
  
  public LinkedList<String> getNodesHistory() { return nodesHistory_ ; }
  public Set<String> getAddressPath() { return addressPath_ ; }
  
  public Session getSession() { return session_ ; }
  public void setSession(Session session) { this.session_ = session ; }
  
  public String getDocumentInfoTemplate() { return documentInfoTemplate_ ; }
  public void setRenderTemplate(String template) { 
    newJCRTemplateResourceResolver() ;
    documentInfoTemplate_  = template ; 
  }
  
  public JCRResourceResolver getJCRTemplateResourceResolver() { return jcrTemplateResourceResolver_; }
  public void newJCRTemplateResourceResolver() {
    jcrTemplateResourceResolver_ = new JCRResourceResolver(session_, "exo:templateFile") ; 
  }
  
  public Session getSessionByWorkspace(String wsName) throws Exception{
    if(wsName == null ) return getSession() ;
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;
    return repositoryService.getRepository().getSystemSession(wsName) ;    
  }
  public void refreshExplorer() throws Exception { 
    findFirstComponentOfType(UIAddressBar.class).getUIStringInput(UIAddressBar.FIELD_ADDRESS).
                                                 setValue(currentNode_.getPath()) ;
    UIWorkingArea workingArea = getChild(UIWorkingArea.class) ;
    workingArea.getChild(UIDocumentWorkspace.class).setRenderedChild(UIDocumentInfo.class) ;
    if(preferences_.isShowSideBar()) {
      UITreeExplorer uiTree = workingArea.getChild(UISideBar.class).getChild(UITreeExplorer.class) ;
      uiTree.buildTree(currentNode_.getPath()) ;
    }
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
  }
  
  public boolean nodeIsLocked(String path, Session session) throws Exception {
    if(getNodeByPath(path, session).isLocked()) return true;
    return false ;
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
    uiAddressBar.getUIStringInput(UIAddressBar.FIELD_ADDRESS).setValue(currentNode_.getPath()) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBar) ;
    
    UIWorkingArea uiWorkingArea = getChild(UIWorkingArea.class) ;
    UIDocumentWorkspace uiDocWorkspace = uiWorkingArea.getChild(UIDocumentWorkspace.class) ;
    uiDocWorkspace.setRenderedChild(UIDocumentInfo.class) ;
    if(preferences_.isShowSideBar()) {
      UISideBar uiSideBar = uiWorkingArea.getChild(UISideBar.class) ;
      uiSideBar.getChild(UITreeExplorer.class).buildTree(currentNode_.getPath()) ;
    }
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
  }

  public void setSelectNode(String uri, Session session) throws Exception {  
    Node previousNode = null ;
    if(uri == null || uri.length() == 0) uri = "/" ;
    previousNode = currentNode_ ;        
    currentNode_ = (Node) session.getItem(uri);
    if(previousNode != null && !currentNode_.equals(previousNode)) record(previousNode.getPath()) ;
  }
  
  public List<Node> getChildrenList(Node node, boolean isReferences) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    Iterator childrenIterator = node.getNodes() ;
    List<Node> childrenList  = new ArrayList<Node>() ;
    NodeType nodeType = node.getPrimaryNodeType() ;
    if(!preferences_.isJcrEnable() && templateService.isManagedNodeType(nodeType.getName())) {
      return childrenList ;
    } 
    if(isReferenceableNode(getCurrentNode()) && isReferences) {
      String[] workspaces = repositoryService.getRepository().getWorkspaceNames() ;
      for(String workspace:workspaces) {
        Session session = repositoryService.getRepository().getSystemSession(workspace) ;
        Node taxonomyNode = session.getNodeByUUID(getCurrentNode().getUUID()) ;
        PropertyIterator categoriesIter = taxonomyNode.getReferences() ;
        while(categoriesIter.hasNext()) {
          Property exoCategoryProp = categoriesIter.nextProperty();
          Node refNode = exoCategoryProp.getParent() ;
          childrenList.add(refNode) ;            
        }
      }
    }
    if(!preferences_.isShowNonDocumentType()) {
      List documentTypes = templateService.getDocumentTemplates() ;      
      while(childrenIterator.hasNext()){
        Node child = (Node)childrenIterator.next() ;
        NodeType type = child.getPrimaryNodeType() ;
        if(Utils.NT_UNSTRUCTURED.equals(type.getName()) || Utils.NT_FOLDER.equals(type.getName())) {
          childrenList.add(child) ;
        } else if(documentTypes.contains(type.getName())) {
          childrenList.add(child) ;
        }
      }
    } else {
      while(childrenIterator.hasNext()) {
        childrenList.add((Node)childrenIterator.next()) ;
      }
    }
    return childrenList ;
  }
  
  public boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes() ;
    for(NodeType type : nodeTypes) {
      if(type.getName().equals(Utils.MIX_REFERENCEABLE)) return true ;
    }
    return false ;
  }
  
  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return (getCurrentNode().hasNode(node.getName())) ? false : true ;
  }
  
  public Node getNodeByPath(String nodePath, Session session) throws Exception {
    return (Node)session.getItem(nodePath) ;    
  }
  
  public LinkedList<ClipboardCommand> getAllClipBoard() { return clipboards_ ;}
  
  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }
  
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
        DriveData drive = (DriveData)dservice.getDriveByName(driveName) ;
        preferences_.setShowSideBar(drive.getViewSideBar()) ;
        preferences_.setShowNonDocumentType(drive.getViewNonDocument()) ;
        preferences_.setShowPreferenceDocuments(drive.getViewPreferences()) ;
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