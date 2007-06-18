/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006 
 */
public class DriveData implements Comparable<DriveData> {

  private String name ;
  private boolean autoCreate ;
  private String repository ;
  private String workspace ;
  private String permissions ;
  private String homePath ;
  private String icon ;
  private String views ;
  private boolean viewPreferences ;
  private boolean viewNonDocument ;
  private boolean viewSideBar ;
  private String allowCreateFolder ;
  
  public  DriveData(){}

  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }  

  public void setAutoCreate(boolean isAuto) { autoCreate = isAuto ; }
  public boolean getAutoCreate() { return autoCreate ; }
  
  public String getRepository() { return this.repository ; }
  public void setRepository(String rp) { this.repository = rp ; }
  
  public String getWorkspace() { return this.workspace ; }
  public void setWorkspace(String ws) { this.workspace = ws ; }
  
  public String getPermissions() { return this.permissions ; }
  public void setPermissions(String permission) { this.permissions = permission ; }

  public String getHomePath() { return this.homePath ; }
  public void setHomePath(String path) { this.homePath = path ; }
  
  public String getIcon() { return this.icon ; }
  public void setIcon(String ico) { this.icon = ico ; }
  
  public String getAllowCreateFolder() { return this.allowCreateFolder ; }
  public void setAllowCreateFolder(String allowCreateFolder) { this.allowCreateFolder = allowCreateFolder ; }

  public String getViews() { return this.views ; }
  public void setViews(String v) { this.views = v ; }
  
  public boolean getViewPreferences() { return this.viewPreferences ; }
  public void setViewPreferences(boolean b) { this.viewPreferences = b ; }
  
  public boolean getViewNonDocument() { return this.viewNonDocument ; }
  public void setViewNonDocument(boolean b) { this.viewNonDocument = b ; }
  
  public boolean getViewSideBar() { return this.viewSideBar ; }
  public void setViewSideBar(boolean b) { this.viewSideBar = b ; }
  
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
      if( permissionList.indexOf("*:/"+array[1]) > -1) return true ;
    }    
    return permissionList.contains(permission) ;
  }

  public int compareTo(DriveData arg) {
    return name.compareToIgnoreCase(arg.getName()) ;
  }
  
}
