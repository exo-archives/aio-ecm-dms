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
package org.exoplatform.ecm.webui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 10:45:01 AM 
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/UITreeJCRExplorer.gtmpl",
    events = @EventConfig(listeners = UITreeJCRExplorer.ChangeNodeActionListener.class)
)
    
public class UITreeJCRExplorer extends UIContainer {

  private Node currentNode_ ;
  private Node rootNode_ = null ;
  private boolean isTab_ = false;
  private String rootPath_ ;
  
  public UITreeJCRExplorer() throws Exception {
    UITree tree = addChild(UITree.class, null, null) ;
    tree.setBeanLabelField("name") ;
    tree.setBeanIdField("path") ;
    tree.setIcon("nt_unstructured16x16Icon")  ;    
    tree.setSelectedIcon("nt_unstructured16x16Icon") ;
  }
  
  public void setRootNode(Node rootNode) { rootNode_ = rootNode ; }
  
  public void buildTree() throws Exception {
    UIJCRBrowser uiJCRBrowser = getParent() ;
    String workspace = uiJCRBrowser.getWorkspace() ;
    String repositoryName = uiJCRBrowser.getRepository() ; 
    Session session = SessionsUtils.getSystemProvider().getSession(workspace, getRepository(repositoryName)) ;
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
    rootPath_ = path ;
    UIJCRBrowser uiJCRBrowser = getParent() ;        
    String workspace = uiJCRBrowser.getWorkspace() ;    
    String repositoryName = uiJCRBrowser.getRepository() ;
    ManageableRepository repository = getRepository(repositoryName) ;
    if(workspace == null) {
      workspace = repository.getConfiguration().getDefaultWorkspaceName() ;
    }
    Session session = SessionsUtils.getSystemProvider().getSession(workspace, repository) ;
    rootNode_ = (Node) session.getItem(path) ;
    currentNode_ = rootNode_ ;
    changeNode(rootNode_) ;
  }
  
  public String getRootPath() { return rootPath_ ; }
  
  public void setNodeSelect(String path) throws Exception {
    UIJCRBrowser uiJCRBrowser = getParent() ;
    String workspace = uiJCRBrowser.getWorkspace() ;
    String repositoryName = uiJCRBrowser.getRepository() ;    
    Session session = SessionsUtils.getSystemProvider().getSession(workspace, getRepository(repositoryName)) ;
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
  
  public ManageableRepository getRepository(String repositoryName) throws Exception{
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    return repositoryService.getRepository(repositoryName) ;
  } 
  
  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UITreeJCRExplorer uiTreeJCR = event.getSource().getParent() ;
      String uri = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiTreeJCR.setNodeSelect(uri) ;
      UIJCRBrowser uiJCRBrowser = uiTreeJCR.getParent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJCRBrowser) ;
    }
  }
}