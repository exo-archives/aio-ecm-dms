package org.exoplatform.services.cms.folksonomy.impl;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.folksonomy.impl.TagStyleConfig.HtmlTagStyle;

public class TagStylePlugin extends BaseComponentPlugin{		  
  
	private TagStyleConfig config ;
	private String description;
  private String name;	    
  
	public TagStylePlugin(InitParams params) throws Exception {
		config = (TagStyleConfig)params.getObjectParamValues(TagStyleConfig.class).get(0) ;       
	}	 
  
	public List<HtmlTagStyle> getTagStyleList() { return config.getTagStyleList() ; }
	
	public String getName() {   return null; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
	
}
