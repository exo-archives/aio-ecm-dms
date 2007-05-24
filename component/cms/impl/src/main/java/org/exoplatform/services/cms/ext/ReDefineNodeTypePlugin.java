/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.ext;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * May 9, 2007  
 */
public class ReDefineNodeTypePlugin implements ComponentPlugin {
  private String name ;
  private String description ;
  private SuperTypeConfig superTypeConfig ;
  
  public ReDefineNodeTypePlugin(InitParams params) {
    superTypeConfig = (SuperTypeConfig)params.getObjectParamValues(SuperTypeConfig.class).get(0) ;
  }
  
  public SuperTypeConfig getAddSuperTypeConfig() {
    return superTypeConfig ;
  }
  
  public String getName() { return name; }  
  public void setName(String name) { this.name = name ; }

  public String getDescription() { return description; }
  public void setDescription(String desc) { this.description = desc ; }
}
