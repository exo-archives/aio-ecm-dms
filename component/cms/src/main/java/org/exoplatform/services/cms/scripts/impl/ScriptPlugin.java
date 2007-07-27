package org.exoplatform.services.cms.scripts.impl;

import java.util.Iterator;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;


public class ScriptPlugin extends BaseComponentPlugin{	
  
  private String name;
  private String description;
  private InitParams params_ ;    

  public ScriptPlugin(InitParams params) throws Exception {
    params_ = params; 
  }

  public Iterator<ObjectParameter> getScriptIterator() { return params_.getObjectParamIterator()  ; }

  public boolean getAutoCreateInNewRepository() {
    ValueParam param = params_.getValueParam("autoInitInNewRepository") ;
    if(param == null) return true ;
    return Boolean.parseBoolean(param.getValue()) ;    
  }

  public String getInitRepository() {
    ValueParam param = params_.getValueParam("reposisoty") ;
    if(param == null) return null ;
    return param.getValue() ;
  }

  public String getName() {   return name; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
}
