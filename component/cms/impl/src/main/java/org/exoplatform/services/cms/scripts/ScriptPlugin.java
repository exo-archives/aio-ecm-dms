package org.exoplatform.services.cms.scripts;

import java.util.Iterator;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;


public class ScriptPlugin extends BaseComponentPlugin{	
	
	private Iterator<ObjectParameter> configs_  ;
  private String name;
	private String description;
	
	public ScriptPlugin(InitParams params) throws Exception {
    configs_ = params.getObjectParamIterator() ; 
	}
	
	public Iterator<ObjectParameter> getScriptIterator() { return configs_ ; }
	
	public String getName() {   return name; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
}
