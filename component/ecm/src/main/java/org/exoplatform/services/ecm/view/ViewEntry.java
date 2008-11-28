/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.ecm.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public class ViewEntry {

  private String name ;
  private String templatePath ;
  private ArrayList<String> accessPermissions ;  
  private List<Tab> tabList = new ArrayList<Tab>(5) ;

  public  ViewEntry() { }

  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }  

  public ArrayList<String> getAccessPermissions() { return this.accessPermissions ; }
  public void setPermissions(ArrayList<String> permission) { this.accessPermissions = permission ; }

  public String getTemplatePath() { return this.templatePath ; }
  public void setTemplatePath(String templ) { this.templatePath = templ ; }

  public List<Tab> getTabList() { return this.tabList ; }
  public void setTabList(List<Tab> tabs) { this.tabList = tabs ; }   

  public boolean hasPermission(String permission) {
    if(accessPermissions == null)  return true;
    for(String s:accessPermissions) {
      if("*".equals(s) || s.equalsIgnoreCase(permission)) {
        return true;
      }
    }
    return false;
  }

  public static class Tab{
    private String tabName ;
    private String buttons ;

    public Tab() {}

    public String getTabName(){ return this.tabName ; }
    public void setTabName( String name) { this.tabName = name ; }

    public String getButtons() { return this.buttons ; }
    public void setButtons( String buttons) { this.buttons = buttons ; }

  }
}