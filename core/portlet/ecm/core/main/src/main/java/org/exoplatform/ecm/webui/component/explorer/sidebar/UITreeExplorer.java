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
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
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
        @EventConfig(listeners = UITreeExplorer.CollapseActionListener.class),
        @EventConfig(listeners = UITreeExplorer.ExpandTreeActionListener.class)
    }    
)

public class UITreeExplorer extends UIContainer {
	
  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("dms.UIJCRExplorer");	
  private TreeNode treeRoot_ ;
  public UITreeExplorer() throws Exception { 
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  UIWorkingArea getWorkingArea() {
    return getAncestorOfType(UIWorkingArea.class);
  }
  
  UIComponent getCustomAction() throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomAction();
  }
  
  public TreeNode getRootTreeNode() { return treeRoot_ ; }
  
  public String getRootActionList() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(uiExplorer.getAllClipBoard().size() > 0) {
      return getContextMenu().getJSOnclickShowPopup(uiExplorer.getCurrentDriveWorkspace() + ":" + uiExplorer.getRootPath(), "Paste").toString() ;
    }
    return "" ;
  }
  
  public String getDriveName() {
    return getAncestorOfType(UIJCRExplorer.class).getDriveData().getName() ;
  }  
  
  public String getActionsList(Node node) throws Exception {
    if(node == null) return "" ;
    return getAncestorOfType(UIWorkingArea.class).getActionsExtensionList(node) ;
  }
  
  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }
  
  public boolean isPreferenceNode(Node node) {
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
  
  public boolean isSymLink(Node node) throws RepositoryException {
    LinkManager linkManager = getApplicationComponent(LinkManager.class);
    return linkManager.isLink(node);
  }
  
  public boolean isPaginated(TreeNode treeNode) {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    int nodePerPages = jcrExplorer.getPreference().getNodesPerPage();
    return (treeNode.getChildrenSize()>nodePerPages) ;   
  }
  
  public String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PortalContainerInfo containerInfo = (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class);      
    return containerInfo.getContainerName(); 
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
    return uiExplorer.getRootNode();
  }  
  
  private void buildTree(String path) throws Exception {
    UIJCRExplorer jcrExplorer = getAncestorOfType(UIJCRExplorer.class);
    int nodePerPages = jcrExplorer.getPreference().getNodesPerPage();
    TreeNode treeRoot = new TreeNode(getRootNode());
    if (path == null)
      path = jcrExplorer.getCurrentPath();
    String[] arr = path.replaceFirst(treeRoot.getPath(), "").split("/");
    TreeNode temp = treeRoot;
    String subPath = null;
    String rootPath = treeRoot.getPath();
    String prefix = rootPath;
    if (!rootPath.equals("/")) {
      prefix += "/";      
    }
    temp.setChildren(jcrExplorer.getChildrenList(rootPath, false));
    if (temp.getChildrenSize() > nodePerPages) {
      ObjectPageList list = new ObjectPageList(temp.getChildren(), nodePerPages);
      addTreeNodePageIteratorAsChild(temp.getPath(), list, rootPath, path);
    }
    for (String nodeName : arr) {
      if (nodeName.length() == 0)
        continue;
      temp = temp.getChildByName(nodeName);
      if (temp == null) {
        LOG.error("The node '" + nodeName + "' cannot be found");
        treeRoot_ = treeRoot;
        return;
      }
      if (subPath == null)
        subPath = prefix + nodeName;
      else
        subPath = subPath + "/" + nodeName;
      temp.setChildren(jcrExplorer.getChildrenList(subPath, false));
      if (temp.getChildrenSize() > nodePerPages) {
        ObjectPageList list = new ObjectPageList(temp.getChildren(), nodePerPages);
        addTreeNodePageIteratorAsChild(temp.getPath(), list, subPath, path);
      }
    }
    treeRoot_ = treeRoot;
  }
  
  public void buildTree() throws Exception {    
    buildTree(null);
  }
  
  static public class ExpandActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer uiTreeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiTreeExplorer.getAncestorOfType(UIJCRExplorer.class) ;      
      UIApplication uiApp = uiTreeExplorer.getAncestorOfType(UIApplication.class) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName");
      Session session = uiExplorer.getSessionByWorkspace(workspaceName);
      try {
    	// Check if the path exists
        NodeFinder nodeFinder = uiTreeExplorer.getApplicationComponent(NodeFinder.class);
        nodeFinder.getItem(session, path);
      } catch(PathNotFoundException pa) {
        uiApp.addMessage(new ApplicationMessage("UITreeExplorer.msg.path-not-found", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(ItemNotFoundException inf) {
          uiApp.addMessage(new ApplicationMessage("UITreeExplorer.msg.path-not-found", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.access-denied", null, 
                  ApplicationMessage.WARNING)) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
	      return ;    	  
      }
      uiExplorer.setSelectNode(workspaceName, path) ; 
      uiExplorer.updateAjax(event) ;      
    }
  }
  
  static public class ExpandTreeActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer uiTreeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      UIJCRExplorer uiExplorer = uiTreeExplorer.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiTreeExplorer.getAncestorOfType(UIApplication.class);
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName");
      Session session = uiExplorer.getSessionByWorkspace(workspaceName);
      try {
        // Check if the path exists
        NodeFinder nodeFinder = uiTreeExplorer.getApplicationComponent(NodeFinder.class);
        nodeFinder.getItem(session, path);
      } catch (PathNotFoundException pa) {
        uiApp.addMessage(new ApplicationMessage("UITreeExplorer.msg.path-not-found",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (ItemNotFoundException inf) {
        uiApp.addMessage(new ApplicationMessage("UITreeExplorer.msg.path-not-found",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.access-denied",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (uiExplorer.getPreference().isShowSideBar()) {
        uiTreeExplorer.buildTree(path);
      }
    }
  }
  
  static public class CollapseActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UITreeExplorer treeExplorer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = treeExplorer.getAncestorOfType(UIJCRExplorer.class) ;
      path = LinkUtils.getParentPath(path) ;
      uiExplorer.setSelectNode(path) ;
      uiExplorer.updateAjax(event) ;      
    }
  }
}