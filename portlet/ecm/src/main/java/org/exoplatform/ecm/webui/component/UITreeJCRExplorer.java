/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UITree;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 10:45:01 AM 
 */
@ComponentConfigs({
//    @ComponentConfig(
//        type = UITree.class, id = "UIJCRTree",
//        template = "system:/groovy/webui/component/UITree.gtmpl" , 
//        events = @EventConfig(listeners = UITreeJCRExplorer.ChangeNodeActionListener.class)
//    ),
    @ComponentConfig(
        template =  "app:/groovy/webui/component/UITreeJCRExplorer.gtmpl",
        events = @EventConfig(listeners = UITreeJCRExplorer.ChangeNodeActionListener.class)
    )
    
})
public class UITreeJCRExplorer extends UIContainer {

  private Node currentNode_ ;
  private Node rootNode_ = null ;
  private boolean isTab_ = false;
  
  public UITreeJCRExplorer() throws Exception {
//    UITree tree = addChild(UITree.class, "UIJCRTree", null) ;
    UITree tree = addChild(UITree.class, null, null) ;
    tree.setBeanLabelField("name") ;
    tree.setBeanIdField("path") ;
    tree.setIcon("Icon LightBlueFolder24x24Icon")  ;    
    tree.setSelectedIcon("Icon LightBlueOpenFolder24x24Icon") ;
  }
  
  public void buildTree() throws Exception {
    CmsConfigurationService cmsService = getApplicationComponent(CmsConfigurationService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    UIJCRBrowser uiJCRBrowser = getParent() ;
    Session session = repositoryService.getRepository().getSystemSession(cmsService.getWorkspace()) ;
    if(uiJCRBrowser.getWorkspace() != null) {
      session = repositoryService.getRepository().getSystemSession(uiJCRBrowser.getWorkspace()) ;
    }
    Iterator sibbling = null ;
    Iterator children = null ;
    if(rootNode_ == null ) {
      rootNode_ = session.getRootNode() ;
      currentNode_ = rootNode_ ;
      children = rootNode_.getNodes() ;
      changeNode(rootNode_) ;
    }
    UITree tree = getChild(UITree.class) ;
    Node nodeSelected = getSelectedNode() ;
    if(!rootNode_.getPath().equals("/")) {
      if(nodeSelected.getPath().equals(rootNode_.getParent().getPath())) nodeSelected = rootNode_ ; 
    } 
    if(nodeSelected.getPath().equals("/")) {
      nodeSelected = session.getRootNode() ;
      children = nodeSelected.getNodes() ;
    }
    tree.setSelected(nodeSelected) ;
    if(nodeSelected.getDepth() > 0) {
      tree.setParentSelected(nodeSelected.getParent()) ;
      sibbling = nodeSelected.getParent().getNodes() ;
      children = nodeSelected.getNodes() ;
    } else {
      tree.setParentSelected(nodeSelected) ;
      sibbling = nodeSelected.getNodes() ;
    }
    List<Node> sibblingList = new ArrayList<Node>() ;
    List<Node> childrenList = new ArrayList<Node>() ;
    while(sibbling.hasNext()) {
      sibblingList.add((Node)sibbling.next()) ;      
    }    
    if(children != null) {
      while(children.hasNext()) {
        childrenList.add((Node)children.next()) ;      
      }
    }
    if(nodeSelected.getPath().equals(rootNode_.getPath())) { tree.setSibbling(childrenList) ; } 
    else { tree.setSibbling(sibblingList) ; }
    tree.setChildren(childrenList) ;
  }
  
  public void renderChildren() throws Exception {
    buildTree() ;
    super.renderChildren() ;
  } 
  
  public void setRootPath(String path) throws Exception {
    CmsConfigurationService cmsService = getApplicationComponent(CmsConfigurationService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    UIJCRBrowser uiJCRBrowser = getParent() ;
    Session session = repositoryService.getRepository().getSystemSession(cmsService.getWorkspace()) ;
    if(uiJCRBrowser.getWorkspace() != null) {
      session = repositoryService.getRepository().getSystemSession(uiJCRBrowser.getWorkspace()) ;
    }
    rootNode_ = (Node) session.getItem(path) ;
    currentNode_ = rootNode_ ;
    changeNode(rootNode_) ;
  }
  
  public void setNodeSelect(String path) throws Exception {
    CmsConfigurationService cmsService = getApplicationComponent(CmsConfigurationService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    UIJCRBrowser uiJCRBrowser = getParent() ;
    Session session = repositoryService.getRepository().getSystemSession(cmsService.getWorkspace()) ;
    if(uiJCRBrowser.getWorkspace() != null) {
      session = repositoryService.getRepository().getSystemSession(uiJCRBrowser.getWorkspace()) ;
    }
    currentNode_ = (Node) session.getItem(path);
    if(!rootNode_.getPath().equals("/")) {
      if(currentNode_.getPath().equals(rootNode_.getParent().getPath())) currentNode_ = rootNode_ ;
    }
    if(currentNode_.getPath().equals("/")){
      currentNode_ = rootNode_ ;
    }
    changeNode(currentNode_) ;
  }
  
  public void setIsTab(boolean isTab) { isTab_ = isTab ; }
  
  public void changeNode(Node nodeSelected) throws Exception {
    List<Node> nodes = new ArrayList<Node>() ;
    NodeIterator nodeIter = nodeSelected.getNodes() ;
    while(nodeIter.hasNext()) {
      nodes.add(nodeIter.nextNode()) ;
    }
    UIContainer uiParent = getParent() ;
    UITreeList uiTreeList = uiParent.getChild(UITreeList.class) ;
    uiTreeList.setNodeList(nodes) ;
    if(isTab_) {
      UIContainer uiRoot = uiParent.getParent() ;
      uiRoot.setRenderedChild(uiParent.getId()) ;
    }
  }
  
  public Node getSelectedNode() { return currentNode_ ; }

  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UITreeJCRExplorer uiTreeJCR = event.getSource().getParent() ;
      String uri = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiTreeJCR.setNodeSelect(uri) ;
      UIJCRBrowser uiJCRBrowser = uiTreeJCR.getParent() ;
      //UIComponent uicomp = uiJCRBrowser.getReturnComponent().getParent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJCRBrowser) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiJCRBrowser.getParent()) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
    }
  }
}
