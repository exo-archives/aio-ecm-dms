package org.exoplatform.services.cms.scripts;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.impl.ResourceConfig;
//import org.exoplatform.services.cms.scripts.ScriptConfig.Script;

public class ScriptPlugin extends BaseComponentPlugin{	
	
//	private ScriptConfig config ;
	private ResourceConfig config ;
  private String name;
	private String description;
	
	public ScriptPlugin(InitParams params) throws Exception {
//		config = (ScriptConfig)params.getObjectParamValues(ScriptConfig.class).get(0) ;
		config = (ResourceConfig)params.getObjectParamValues(ResourceConfig.class).get(0) ;	
	}
	
	public ResourceConfig getScripts() { return config ; }
	
	public String getName() {   return name; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
}
