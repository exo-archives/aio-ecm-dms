package org.exoplatform.ecm.webui.component.admin.taxonomy;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Sep 29, 2006
 * 5:37:31 PM 
 */
public class TaxonomyNode {
  
	private boolean isExpanded = false;
	private Node node_ ;
	private int level_ ;
  private List<TaxonomyNode> children_ = new ArrayList<TaxonomyNode>();
	
	public TaxonomyNode(Node node, int level) throws RepositoryException {
		node_ = node ;
    level_ = level + 1;
		try {
			NodeIterator iter = node_.getNodes() ;
			if(iter.getSize() == 0) isExpanded = false;
			while(iter.hasNext()) {
				Node child = iter.nextNode() ;				
				children_.add(new TaxonomyNode(child, level_)) ;
			}						     
		} catch (PathNotFoundException e) {
		}		
	}	
	
  public int getLevel() { return level_; }
  
	public boolean isExpanded() { return isExpanded; }	
	public void setExpanded(boolean isExpanded) { this.isExpanded = isExpanded; }
  
	public Node getNode() { return node_ ; }	
	public void setNode(Node node) { this.node_ = node ; }
	
	public String getName() throws RepositoryException { return node_.getName(); }
	
	public String getPath() throws RepositoryException { return node_.getPath(); }
	
  public int getChildrenSize() { return children_.size(); }
  public List<TaxonomyNode> getChildren() { return children_; }
  public void setChildren(List<TaxonomyNode> list) { this.children_ = list ; }
	
	public TaxonomyNode findTaxonomyNode(String path) throws RepositoryException {
		if(node_.getPath().equals(path)) return this;
		TaxonomyNode tnode = null;
		for(TaxonomyNode child : children_) {
			tnode = child.findTaxonomyNode(path);
			if(tnode != null) return tnode;
		}
		return null;
	}
		
	public void update(String path,Boolean expand) throws Exception {
	  TaxonomyNode taxonomyNode = findTaxonomyNode(path) ;	  
	  if(taxonomyNode == null) return ;	  
	  int level = taxonomyNode.getLevel() ;
	  Node selectedNode = taxonomyNode.getNode() ;	  
	  List<TaxonomyNode> newChildren = new ArrayList<TaxonomyNode>() ;
	  for(NodeIterator iterator = selectedNode.getNodes();iterator.hasNext();) {
	    Node node = iterator.nextNode();
	    TaxonomyNode child = taxonomyNode.findTaxonomyNode(node.getPath());
	    if(child != null) {
	      newChildren.add(child) ;
	    }else {
	      newChildren.add(new TaxonomyNode(node,level)) ;
	    }
	  }
	  if(expand != null) taxonomyNode.setExpanded(expand) ;
	  taxonomyNode.setChildren(newChildren) ;
	}
}