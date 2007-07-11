package org.exoplatform.services.cms.impl;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;

public class AddPathPlugin implements ComponentPlugin {

  private CmsConfig paths;
  private String description;
  private String name;

  public AddPathPlugin(InitParams params) {
    paths = (CmsConfig) params.getObjectParamValues(CmsConfig.class).get(0);
  }
  
  public CmsConfig getPaths() {
    return paths;
  }

  public String getName() {   return null; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }
  
}
