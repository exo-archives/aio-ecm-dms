/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.views;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 * 
 */
public class TemplateConfig {  
  private String name ;
  private String warPath ;
  private String type ;   

  public TemplateConfig(){} 
  
  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }

  public String getWarPath() { return this.warPath ; }
  public void setWarPath(String warPath) { this.warPath = warPath ; }

  public String getTemplateType() {return this.type ; }
  public void setTemplateType(String type) { this.type = type ; }

}
