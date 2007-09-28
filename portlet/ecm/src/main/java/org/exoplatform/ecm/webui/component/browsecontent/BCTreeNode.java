/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Jan 3, 2007 3:24:37 PM 
 */
public class BCTreeNode {
  //TODO can use TreeNode instead of this class
  private boolean isExpanded_ ;
  private Node node_ ;
  private List<BCTreeNode> children_ = new ArrayList<BCTreeNode>() ; 
  
  public BCTreeNode(Node node, List<Node> children) {
    node_ = node ;
    isExpanded_ = true;
    setChildren(children) ;
  }
  
  public BCTreeNode(Node node) {
    node_ = node ;
    isExpanded_ = false ;
  }
  
  public int getLevel() throws RepositoryException { return node_.getDepth(); }
  
  public boolean isExpanded() { return isExpanded_; } 
  public void setExpanded(boolean isExpanded) { isExpanded_ = isExpanded; }
  
  public String getName() throws RepositoryException { return node_.getName(); }
  public String getPath() throws RepositoryException { return node_.getPath(); }
  
  public Node getNode() { return node_ ; }  
  public void setNode(Node node) { node_ = node ; }
  
  public List<BCTreeNode> getChildren() { return children_ ; }
  public int getChildrenSize() { return children_.size() ; }
  
  public BCTreeNode getChild(String relPath) throws RepositoryException {
    for(BCTreeNode child : children_) {
      String path = child.getPath() ;
      String nodeName = path.substring(path.lastIndexOf("/") + 1) ;
      if(nodeName.equals(relPath)) return child ;
    }
    return null;
  }
  
  public void setChildren(List<Node> children) {
    setExpanded(true) ;
    for(Node child : children) {
      children_.add(new BCTreeNode(child)) ;
    } 
  }
}