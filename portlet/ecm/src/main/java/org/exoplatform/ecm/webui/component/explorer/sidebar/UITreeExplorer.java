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
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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

public class UITreeExplorer extends UIContainer {
  private TreeNode treeRoot_ ;
  public UITreeExplorer() throws Exception { 
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  public TreeNode getRootTreeNode() { return treeRoot_ ; }
  
  public String getRootActionList() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(uiExplorer.getAllClipBoard().size() > 0) {
      return getContextMenu().getJSOnclickShowPopup(uiExplorer.getRootNode().getPath(), "Paste").toString() ;
    }
    return "" ;
  }
  
  public String getActionsList(Node node) throws Exception {
    if(node == null) return "" ;
    return getAncestorOfType(UIWorkingArea.class).getActionsList(node) ;
  }
  
  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }
  
  @SuppressWarnings("unused")
  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return getAncestorOfType(UIWorkingArea.class).isPreferenceNode(node) ;
  }
  
  
  @SuppressWarnings("unchecked")
  public List<TreeNode> getRenderedChildren(TreeNode treeNode) throws Exception {    
    if(isPaginated(treeNode)) {      
      UITreeNodePageIterator pageIterator = findComponentById(treeNode.getPath());      
      return pageIterator.getCurrentPageData();
    }
    return treeNode.getChildren();
  }
  
  public boolean isSystemWorkspace() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).isSystemWorkspace() ;
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
  
  private Node getRootNode() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Session session = 
      uiExplorer.getSessionProvider().getSession(uiExplorer.getCurrentWorkspace(), uiExplorer.getRepository());
    return uiExplorer.getNodeByPath(uiExplorer.getRootPath(), session);
  }  
  
  public void buildTree() throws Exception {    
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    int nodePerPages = jcrExplorer.getPreference().getNodesPerPage();
    TreeNode treeRoot = new TreeNode(getRootNode()) ;
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
      UITreeExplorer uiTreeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiTreeExplorer.getAncestorOfType(UIJCRExplorer.class) ;      
      UIApplication uiApp = uiTreeExplorer.getAncestorOfType(UIApplication.class) ;
      Node selectedNode = null ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName");
      if(workspaceName != null && workspaceName.length() > 0) {
        if(!workspaceName.equals(uiExplorer.getCurrentWorkspace())) {              
          uiExplorer.setIsReferenceNode(true) ;
          uiExplorer.setReferenceWorkspace(workspaceName) ;
        } else {              
          uiExplorer.setIsReferenceNode(false) ;
        }
      }
      try {
        selectedNode = (Node) uiExplorer.getSession().getItem(path) ;
      } catch(PathNotFoundException pa) {
        uiApp.addMessage(new ApplicationMessage("UITreeExplorer.msg.path-not-found", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(AccessDeniedException ace) {
        selectedNode = uiExplorer.getSession().getRootNode() ;
      }
      uiExplorer.setSelectNode(selectedNode.getPath(), uiExplorer.getSession()) ; 
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