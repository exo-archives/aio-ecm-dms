package org.exoplatform.services.cms.categories;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.categories.TaxonomyConfig.Taxonomy;

public class TaxonomyPlugin extends BaseComponentPlugin{	
	
	private TaxonomyConfig config ;
	private String description;
  private String name;
	
	public TaxonomyPlugin(InitParams params) throws Exception {
		config = (TaxonomyConfig)params.getObjectParamValues(TaxonomyConfig.class).get(0) ;
	
	}
	
	public List<Taxonomy> getTaxonomies() { return config.getTaxonomies() ; }
	
	public String getName() {   return null; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
	
}
