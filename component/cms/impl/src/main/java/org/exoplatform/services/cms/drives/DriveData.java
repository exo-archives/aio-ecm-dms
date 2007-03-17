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
public class DriveData{

  private String name ;
  private String workspace ;
  private String permissions ;
  private String homePath ;
  private String icon ;
  private String views ;
  private boolean viewPreferences ;
  private boolean viewNonDocument ;
  private boolean viewExplorer ;
  private boolean viewClipboard ;
  
  public  DriveData(){}

  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }  

  public String getWorkspace() { return this.workspace ; }
  public void setWorkspace(String ws) { this.workspace = ws ; }
  
  public String getPermissions() { return this.permissions ; }
  public void setPermissions(String permission) { this.permissions = permission ; }

  public String getHomePath() { return this.homePath ; }
  public void setHomePath(String path) { this.homePath = path ; }
  
  public String getIcon() { return this.icon ; }
  public void setIcon(String ico) { this.icon = ico ; }

  public String getViews() { return this.views ; }
  public void setViews(String v) { this.views = v ; }
  
  public boolean getViewPreferences() { return this.viewPreferences ; }
  public void setViewPreferences(boolean b) { this.viewPreferences = b ; }
  
  public boolean getViewNonDocument() { return this.viewNonDocument ; }
  public void setViewNonDocument(boolean b) { this.viewNonDocument = b ; }
  
  public boolean getViewExplorer() { return this.viewExplorer ; }
  public void setViewExplorer(boolean b) { this.viewExplorer = b ; }
  
  public boolean getViewClipboard() { return this.viewClipboard ; }
  public void setViewClipboard(boolean b) { this.viewClipboard = b ; }
  
  public List getAllPermissions() {
    String[] allPermissions = StringUtils.split(permissions, ",");
    List permissionList = new ArrayList() ;
    for(int i = 0 ; i < allPermissions.length ; i ++ ){
      permissionList.add(allPermissions[i].trim()) ;
    }
    return permissionList ;
  }

  public boolean hasPermission(String permission) {
    List permissions = getAllPermissions() ;
    if(permission == null) return false ;
    if(permission.indexOf(":/") > -1){
	  String[] array = StringUtils.split(permission , ":/") ;
      if(array == null || array.length < 2) return false ;
      if( permissions.indexOf("*:/"+array[1]) > -1) return true ;
    }    
    return permissions.contains(permission) ;
  }
  
}
