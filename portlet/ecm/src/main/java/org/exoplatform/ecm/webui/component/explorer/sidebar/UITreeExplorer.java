/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Aug 2, 2006
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UITreeExplorer.gtmpl",
    events = {
        @EventConfig(listeners = UITreeExplorer.ExpandActionListener.class),
        @EventConfig(listeners = UITreeExplorer.CollapseActionListener.class)
    }    
)
  //TODO Maybe can reuse UITree from portal

public class UITreeExplorer extends UIContainer {
  private TreeNode treeRoot_ ;
  public UITreeExplorer() throws Exception { 
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  public TreeNode getRootTreeNode() { return treeRoot_ ; }
  
  public String getRootActionList() throws RepositoryException {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(jcrExplorer.getAllClipBoard().size() > 0) {
      return getContextMenu().getJSOnclickShowPopup(jcrExplorer.getRootNode().getPath(), "Paste").toString() ;
    }
    return "" ;
  }
  
  public String getActionsList(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getActionsList(node) ;
  }
  
  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }
  
  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return getAncestorOfType(UIWorkingArea.class).isPreferenceNode(node) ;
  }
  
  public List<TreeNode> getRenderedChildren(TreeNode treeNode) throws Exception {    
    if(isPaginated(treeNode)) {      
      UITreeNodePageIterator pageIterator = findComponentById(treeNode.getPath());      
      return pageIterator.getCurrentPageData();
    }
    return treeNode.getChildren();
  }
  
  public boolean isSystemWorkspace() throws Exception {
    String wsName = getAncestorOfType(UIJCRExplorer.class).getCurrentWorkspace() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String systemWS = repositoryService.getRepository(getRepository()).getConfiguration().getSystemWorkspaceName() ;
    if(wsName.equals(systemWS)) return true ;
    return false ;
  }
  
  public UITreeNodePageIterator getUIPageIterator(String id) throws Exception {    
    return findComponentById(id);
  }
  
  public boolean isPaginated(TreeNode treeNode) {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    int nodePerPages = jcrExplorer.getPreference().getNodesPerPage();
    return (treeNode.getChildrenSize()>nodePerPages) ;   
  }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  public String getServerPath() {
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
                          portletRequestContext.getRequest().getServerName() + ":" +
                          String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }
  
  public String getRepository() { 
    return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
  }    
  
  private void addTreeNodePageIteratorAsChild(String id,ObjectPageList pageList, String selectedPath,String currentPath) throws Exception {
    if(findComponentById(id)== null) {
      UITreeNodePageIterator nodePageIterator = addChild(UITreeNodePageIterator.class,null,id);
      nodePageIterator.setPageList(pageList);
      nodePageIterator.setSelectedPath(selectedPath);
    }else {
      UITreeNodePageIterator existedComponent = findComponentById(id);      
      int currentPage = existedComponent.getCurrentPage();
      existedComponent.setPageList(pageList);
      if(!selectedPath.equalsIgnoreCase(currentPath)) {
        if(currentPage <= existedComponent.getAvailablePage()) {
          existedComponent.setCurrentPage(currentPage);
        } 
      }      
    }
  }
  
  public void buildTree() throws Exception {    
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    int nodePerPages = jcrExplorer.getPreference().getNodesPerPage();
    TreeNode treeRoot = new TreeNode(jcrExplorer.getRootNode()) ;
    String path = jcrExplorer.getCurrentNode().getPath() ;     
    String[] arr = path.replaceFirst(treeRoot.getPath(), "").split("/") ;
    TreeNode temp = treeRoot ;
    String subPath = null ;
    String prefix = "/" ;
    if(!treeRoot.getNode().getPath().equals("/")) prefix = treeRoot.getNode().getPath() + prefix;
    for(String nodeName : arr) {
      if(nodeName.length() == 0) continue ;
      if(subPath == null) subPath = prefix + nodeName;
      else subPath = subPath + "/" + nodeName ;      
      temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
      if(temp.getChildrenSize()> nodePerPages) {                
        ObjectPageList list = new ObjectPageList(temp.getChildren(),nodePerPages);
        addTreeNodePageIteratorAsChild(temp.getPath(),list,temp.getPath(),path);
      }
      temp = temp.getChild(subPath) ;            
      if(temp == null)  {
        treeRoot_ = treeRoot;
        return ;
      }
    }
    temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;        
    if(temp.getChildrenSize()> nodePerPages) {             
      ObjectPageList list = new ObjectPageList(temp.getChildren(),nodePerPages);
      addTreeNodePageIteratorAsChild(temp.getPath(),list,temp.getPath(),path);
    }    
    treeRoot_ = treeRoot ;    
  }
  
  static public class ExpandActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer treeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = treeExplorer.getAncestorOfType(UIJCRExplorer.class) ;      
      Node selectedNode = null ;
      try {
        selectedNode = (Node) uiExplorer.getSession().getItem(path) ;
      } catch(AccessDeniedException ace) {
        selectedNode = uiExplorer.getSession().getRootNode() ;
      }
      uiExplorer.setSelectNode(selectedNode) ; 
      uiExplorer.updateAjax(event) ;      
    }
  }
  
  static public class CollapseActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer treeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = treeExplorer.getAncestorOfType(UIJCRExplorer.class) ;
      path = path.substring(0, path.lastIndexOf("/")) ;
      uiExplorer.setSelectNode(path, uiExplorer.getSession()) ;
      uiExplorer.updateAjax(event) ;      
    }
  }
}