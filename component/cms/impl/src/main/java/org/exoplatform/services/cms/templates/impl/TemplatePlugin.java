package org.exoplatform.services.cms.templates.impl;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

public class TemplatePlugin extends BaseComponentPlugin {

  private TemplateConfig config_;

  public TemplatePlugin(InitParams params) throws Exception {
    config_ = (TemplateConfig) params
        .getObjectParamValues(TemplateConfig.class).get(0);
  }
  
  public String getRepository() {
    return config_.getRepository();
  }
  
  public List getNodeTypes() {
    return config_.getNodeTypes();
  }
  
  public List getViewTemplates() {
    return config_.getTemplates();
  }
  
  public String getLocation() {
	  return config_.getLocation() ;
  }
}
