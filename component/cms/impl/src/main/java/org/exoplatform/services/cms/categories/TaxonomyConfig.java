package org.exoplatform.services.cms.categories;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyConfig {			
	
  private String repository;
	private List<Taxonomy> taxonomies = new ArrayList<Taxonomy>() ;
	
  public String getRepository() { return repository; }
  public void setRepository(String repository) { this.repository = repository; }
  
	public List<Taxonomy> getTaxonomies() { return this.taxonomies ; }	
	public void setTaxonomies(List<Taxonomy> taxonomies) { this.taxonomies = taxonomies ;}
	
	static public class Taxonomy {
		
		private String path ;
		private String name ;
		private String description ;
		
		public String getPath() { return this.path ; }
		public void setPath(String path) { this.path = path ;}
		
		public String getName() {return this.name ; }
		public void setName(String name) { this.name = name ; }
		
		public String getDescription()  { return this.description ; }
		public void setDescription(String description) { this.description = description ; }
	}
}
