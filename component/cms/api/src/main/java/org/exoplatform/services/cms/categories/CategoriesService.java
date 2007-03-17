package org.exoplatform.services.cms.categories;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;

public interface CategoriesService {
	
	public void addTaxonomyPlugin(ComponentPlugin plugin) ;
	public Node getTaxonomyHomeNode() throws Exception ;
	public Node getTaxonomyNode(String taxonomyName) throws Exception ;		
	public void addTaxonomy(String parentPath,String childName) throws Exception  ;
	public void removeTaxonomyNode(String realPath) throws Exception ;		
	public void moveTaxonomyNode(String srcPath, String destPath, String type) throws Exception ;		
	
	public boolean hasCategories(Node node) throws Exception;
	public List<Node> getCategories(Node node) throws Exception;
	public void removeCategory(Node node, String categoryPath) throws Exception;
	
	public void addCategory(Node node, String categoryPath) throws Exception;   
	public void addCategory(Node node, String categoryPath, boolean replaceAll) throws Exception;
  
}
