/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.views.impl;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 * 
 */
public class TemplateDataImpl{
  private String repository ;
  private String name ;
  private String warPath ;
  private String type ;   

  public TemplateDataImpl(){} 

  public String getRepository() { return this.repository ; }
  public void setRepository(String repo) { this.repository = repo ; }  
  
  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }

  public String getWarPath() { return this.warPath ; }
  public void setWarPath(String warPath) { this.warPath = warPath ; }

  public String getTemplateType() {return this.type ; }
  public void setTemplateType(String type) { this.type = type ; }

}
