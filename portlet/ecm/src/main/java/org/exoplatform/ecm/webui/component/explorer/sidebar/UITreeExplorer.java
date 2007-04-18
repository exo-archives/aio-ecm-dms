/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIRightClickPopupMenu;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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

public class UITreeExplorer extends UIComponent {

  private TreeNode treeRoot_ ;
  
  public UITreeExplorer() throws Exception {}
  
  public void setTreeRoot(Node node) throws Exception {
    treeRoot_ = new TreeNode(node) ;
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  public String getRootActionList() throws RepositoryException {
    if(getAncestorOfType(UIJCRExplorer.class).getAllClipBoard().size() > 0) {
      return getContextMenu().getJSOnclickShowPopup(treeRoot_.getPath(), "Paste").toString() ;
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
  public String getNodePath(Node node) throws Exception {
    return node.getPath() ;
  }
  
  public TreeNode getTreeRoot() { return treeRoot_ ; }
  
//  public void buildTree(String path) throws Exception {
//    System.out.println("\n\nBuild Tree is called = !" + path + "\n\n");
//    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
//    treeRoot_.getChildren().clear() ;
//    String[] arr = path.replaceFirst(treeRoot_.getPath(), "").split("/") ;
//    TreeNode temp = treeRoot_ ;
//    for(String nodeName : arr) {
//      if(nodeName.length() == 0) continue ;
//      temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
//      temp = temp.getChild(nodeName) ;
//      if(temp == null) return ;
//    }
//    temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
//  }
  
  public TreeNode buildTree() throws Exception {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    String path = jcrExplorer.getCurrentNode().getPath() ;
    treeRoot_.getChildren().clear() ;
    String[] arr = path.replaceFirst(treeRoot_.getPath(), "").split("/") ;
    TreeNode temp = treeRoot_ ;
    for(String nodeName : arr) {
      if(nodeName.length() == 0) continue ;
      temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
      temp = temp.getChild(nodeName) ;
      if(temp == null) return treeRoot_ ;
    }
    temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
    return treeRoot_ ;
  }
  
  static public class ExpandActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      String wsName = event.getRequestContext().getRequestParameter("workspaceName") ;
      uiExplorer.setSelectNode(path, uiExplorer.getSessionByWorkspace(wsName)) ;
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class CollapseActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      String wsName = event.getRequestContext().getRequestParameter("workspaceName") ;
      path = path.substring(0, path.lastIndexOf("/")) ;
      uiExplorer.setSelectNode(path, uiExplorer.getSessionByWorkspace(wsName)) ;
      uiExplorer.updateAjax(event) ;
    }
  }
}