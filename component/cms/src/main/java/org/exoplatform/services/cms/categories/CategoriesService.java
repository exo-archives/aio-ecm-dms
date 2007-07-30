package org.exoplatform.services.cms.categories;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface CategoriesService {
	
	public void addTaxonomyPlugin(ComponentPlugin plugin) ;
	public Node getTaxonomyHomeNode(String repository,SessionProvider provider) throws Exception ;		
	public void addTaxonomy(String parentPath,String childName, String repository) throws Exception  ;
	public void removeTaxonomyNode(String realPath, String repository) throws Exception ;		
	public void moveTaxonomyNode(String srcPath, String destPath, String type, String repository) throws Exception ;		
	
	public boolean hasCategories(Node node) throws Exception;
	public List<Node> getCategories(Node node, String repository) throws Exception;
	public void removeCategory(Node node, String categoryPath, String repository) throws Exception;
	
	public void addCategory(Node node, String categoryPath, String repository) throws Exception;   
	public void addCategory(Node node, String categoryPath, boolean replaceAll, String repository) throws Exception;
  
  public void init(String repository) throws Exception ;
  
}
