package org.exoplatform.services.cms.scripts;

import java.util.Iterator;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;


public class ScriptPlugin extends BaseComponentPlugin{	
	
	//private Iterator<ObjectParameter> configs_  ;
  private String name;
	private String description;
  private InitParams params_ ;
	
	public ScriptPlugin(InitParams params) throws Exception {
    params_ = params; 
	}
	
	public Iterator<ObjectParameter> getScriptIterator() { return params_.getObjectParamIterator()  ; }
	
	public String getName() {   return name; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
}
