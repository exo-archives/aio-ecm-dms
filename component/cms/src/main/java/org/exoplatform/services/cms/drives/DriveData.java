/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.cms.drives;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006 
 */
public class DriveData implements Comparable<DriveData> {

  private String name ;
  private String repository ;
  private String workspace ;
  private String permissions ;
  private String homePath ;
  private String icon ;
  private String views ;
  private boolean viewPreferences ;
  private boolean viewNonDocument ;
  private boolean viewSideBar ;
  private boolean showHiddenNode ;
  private String allowCreateFolder ;
  
  public  DriveData(){}

  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }  

  
  public String getRepository() { return repository ; }
  public void setRepository(String rp) { repository = rp ; }
  
  public String getWorkspace() { return workspace ; }
  public void setWorkspace(String ws) { workspace = ws ; }
  
  public String getPermissions() { return this.permissions ; }
  public void setPermissions(String permissions) { this.permissions = permissions ; }

  public String getHomePath() { return homePath ; }
  public void setHomePath(String path) { homePath = path ; }
  
  public String getIcon() { return icon ; }
  public void setIcon(String ico) { icon = ico ; }
  
  public String getAllowCreateFolder() { return allowCreateFolder ; }
  public void setAllowCreateFolder(String allowCreateFolder) { this.allowCreateFolder = allowCreateFolder ; }

  public String getViews() { return views ; }
  public void setViews(String v) { views = v ; }
  
  public boolean getViewPreferences() { return viewPreferences ; }
  public void setViewPreferences(boolean b) { viewPreferences = b ; }
  
  public boolean getViewNonDocument() { return viewNonDocument ; }
  public void setViewNonDocument(boolean b) { viewNonDocument = b ; }
  
  public boolean getViewSideBar() { return viewSideBar ; }
  public void setViewSideBar(boolean b) { viewSideBar = b ; }
  
  public boolean getShowHiddenNode() { return showHiddenNode ; }
  public void setShowHiddenNode(boolean b) { showHiddenNode = b ; }
  
  public String[] getAllPermissions() {    
    return permissions.split(",") ;
  }

  public boolean hasPermission(String[] allPermissions, String permission) {
    List<String> permissionList = new ArrayList<String>() ;
    for(String per : allPermissions){
      permissionList.add(per.trim()) ;
    }
    if(permission == null) return false ;
    if(permission.indexOf(":/") > -1){
      String[] array = permission.split(":/") ;
      if(array == null || array.length < 2) return false ;
      if(permissionList.contains("*:/"+array[1])) return true ;
    }    
    return permissionList.contains(permission) ;
  }

  public int compareTo(DriveData arg) {
    return name.compareToIgnoreCase(arg.getName()) ;
  }
}
