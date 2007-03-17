/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Oct 9, 2006
 * 2:36:06 PM 
 */
public class TemplateBean {    
  private String name ;
  private String path ;
  private String baseVersion =  "";

  public TemplateBean(String n, String p, String baVer) {
    name = n ;
    path = p ;
    baseVersion = baVer ;
  }

  public String getBaseVersion() { return baseVersion; }
  public void setBaseVersion(String baVer) { baseVersion = baVer; }

  public String getName() { return name ; }
  public void setName(String n) { name = n ; }

  public String getPath() { return path ; }
  public void setPath(String p) { path = p ; }
}
