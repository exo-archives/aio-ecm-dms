/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 18-07-2007  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/browse/UICategoryTree.gtmpl",
    events = {
      @EventConfig(listeners = UICategoryTree.SelectActionListener.class)
    }
)
public class UICategoryTree extends UIComponent {
  private BCTreeNode treeRoot_;
  public UICategoryTree() { }
  
  public BCTreeNode getTreeRoot() { return treeRoot_ ;}
  public void setTreeRoot(Node node) throws Exception { treeRoot_ = new BCTreeNode(node) ;}
  public Node getRootNode() throws Exception {return getAncestorOfType(UIBrowseContainer.class).getRootNode() ;}

  public String getIcons(Node node, String type) throws Exception {
    return Utils.getNodeTypeIcon(node, type) ; 
  }
  
  protected boolean isCategories(NodeType nodeType) {
    for(String type : Utils.CATEGORY_NODE_TYPES) {
      if(nodeType.getName().equals(type)) return true ;
    }
    return false ;
  }
  public List<Node> getCategoryList(Node node) throws Exception{
    List<Node> nodes = new ArrayList<Node>() ;
    NodeIterator item = node.getNodes() ;
    while(item.hasNext()) {
      Node child = item.nextNode() ;
      if(isCategories(child.getPrimaryNodeType())) nodes.add(child) ; 
    }
    return nodes ;
  }
  public void buildTree(String path) throws Exception {
    treeRoot_.getChildren().clear() ;
    String[] arr = path.replaceFirst(treeRoot_.getPath(), "").split("/") ;
    BCTreeNode temp = treeRoot_ ;
    for(String nodeName : arr) {
      if(nodeName.length() == 0) continue ;
      temp.setChildren(getCategoryList(temp.getNode())) ;
      temp = temp.getChild(nodeName) ;
      if(temp == null) return ;
    }
    temp.setChildren(getCategoryList(temp.getNode())) ;
  }
  static public class SelectActionListener extends EventListener<UICategoryTree> {
    public void execute(Event<UICategoryTree> event) throws Exception {
      UICategoryTree cateTree = event.getSource() ;
      UIBrowseContainer uiContainer = cateTree.getAncestorOfType(UIBrowseContainer.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = uiContainer.getNodeByPath(path) ;
      if(node == null) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UICategoryTree.msg.invalid-node", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      uiContainer.isShowDocumentDetail_ = false ;
      uiContainer.isShowDocumentByTag_ = false ;
      uiContainer.isShowAllDocument_ = false ;
      uiContainer.setSelectedTab(null) ;
      uiContainer.setCurrentNode(node) ;
      cateTree.buildTree(node.getPath()) ;
      uiContainer.setPageIterator(uiContainer.getSubDocumentList(uiContainer.getCurrentNode())) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
