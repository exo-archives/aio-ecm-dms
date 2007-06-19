/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.queries;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * mar 02, 2007 
 */
public class QueryData{

  private String name ;
  private boolean autoCreatedInNewRepository ;
  private String repository ;
  private String language ;
  private String statement ;
  private String permissions ;
  private boolean cachedResult ;
  
  public  QueryData(){}

  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }  
  
  public boolean getAutoCreatedInNewRepository() { return this.autoCreatedInNewRepository ; }
  public void setAutoCreatedInNewRepository(boolean isAuto) { this.autoCreatedInNewRepository = isAuto ; }
  
  public String getRepository() { return this.repository ; }
  public void setRepository(String rp) { this.repository = rp ; }

  public String getLanguage() { return this.language ; }
  public void setLanguage(String l) { this.language = l ; }
  
  public String getPermissions() { return this.permissions ; }
  public void setPermissions(String permission) { this.permissions = permission ; }

  public String getStatement() { return this.statement ; }
  public void setStatement(String s) { this.statement = s ; }
  
  public boolean getCacheResult() { return this.cachedResult ; }
  public void setCacheResult(boolean r) { this.cachedResult = r ; }
}
