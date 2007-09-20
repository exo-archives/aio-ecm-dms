/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
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

public class UITreeExplorer extends UIComponent {
  public UITreeExplorer() throws Exception {}
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  public String getRootActionList() throws RepositoryException {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(jcrExplorer.getAllClipBoard().size() > 0) {
      return getContextMenu().getJSOnclickShowPopup(jcrExplorer.getRootNode().getPath(), "Paste").toString() ;
    }
    return "" ;
  }
  
  public List<Node> getChildrenNode(NodeIterator iter) {
    List<Node> listNodes = new ArrayList<Node>() ;
    while(iter.hasNext()) {
      Node node = iter.nextNode() ;
      listNodes.add(node) ;
    }
    return listNodes ;
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
  
  public String getNodePath(Node node) throws Exception {
    return node.getPath() ;
  }
  
  public TreeNode buildTree() throws Exception {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
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
      temp = temp.getChild(subPath) ;
      if(temp == null) return treeRoot ;
    }
    temp.setChildren(jcrExplorer.getChildrenList(temp.getNode(), false)) ;
    return treeRoot ;
  }
  
  static public class ExpandActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
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
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      path = path.substring(0, path.lastIndexOf("/")) ;
      uiExplorer.setSelectNode(path, uiExplorer.getSession()) ;
      uiExplorer.updateAjax(event) ;
    }
  }
}