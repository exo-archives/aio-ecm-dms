package org.exoplatform.ecm.webui.component.explorer.sidebar;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Sep 29, 2006
 * 5:37:31 PM 
 */
public class TreeNode {
  
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String NT_FOLDER = "nt:folder" ;
  
  private boolean isExpanded_ ;
  private Node node_ ;
  private List<TreeNode> children_ = new ArrayList<TreeNode>() ; 
  
  public TreeNode(Node node, List<Node> children) {
    node_ = node ;
    isExpanded_ = true;
    setChildren(children) ;
  }
  
  public TreeNode(Node node) {
    node_ = node ;
    isExpanded_ = false ;
  }
  
  public int getLevel() throws RepositoryException { return node_.getDepth(); }
  
  public boolean isExpanded() { return isExpanded_; } 
  public void setExpanded(boolean isExpanded) { isExpanded_ = isExpanded; }
  
  public String getName() throws RepositoryException { 
    String path = node_.getPath() ;
    return path.substring(path.lastIndexOf("/") + 1, path.length()); 
  }
  public String getPath() throws RepositoryException { return node_.getPath(); }
  
  public Node getNode() { return node_ ; }  
  public void setNode(Node node) { node_ = node ; }
  
  public List<TreeNode> getChildren() { return children_ ; }
  public int getChildrenSize() { return children_.size() ; }
  
  public TreeNode getChild(String relPath) throws RepositoryException {
    for(TreeNode child : children_) {
      if(child.getNode().getName().equals(relPath)) return child ;
    }
    return null;
  }
  
  public void setChildren(List<Node> children) {
    setExpanded(true) ;
    for(Node child : children) {
      children_.add(new TreeNode(child)) ;
    } 
  }
}