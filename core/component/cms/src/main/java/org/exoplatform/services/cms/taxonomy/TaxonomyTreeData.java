/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.services.cms.taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Hoang Van Hung hunghvit@gmail.com
 * Apr 3, 2009
 */
public class TaxonomyTreeData implements Comparable<TaxonomyTreeData> {

  private String  name;

  private String  repository;

  private String  workspace;

  private String  permissions;

  private String  homePath;

  public TaxonomyTreeData() {
    
  }

  /**
   * @return the name of drive
   */
  public String getName() {
    return name;
  }

  /**
   * Register drive name
   * 
   * @param name the name of DriveData
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name of repository
   */
  public String getRepository() {
    return repository;
  }

  /**
   * Register repository to drive
   * 
   * @param rp repository name
   */
  public void setRepository(String rp) {
    repository = rp;
  }

  /**
   * @return the name of workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * Register workspace to drive
   * 
   * @param ws the workspace name
   */
  public void setWorkspace(String ws) {
    workspace = ws;
  }

  /**
   * @return the permissions of drive
   */
  public String getPermissions() {
    return this.permissions;
  }

  /**
   * Register permission to drive
   * 
   * @param permissions
   */
  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }

  /**
   * @return the home path of drive
   */
  public String getHomePath() {
    return homePath;
  }

  /**
   * Register home path to drive
   * 
   * @param path the home path of drive
   */
  public void setHomePath(String path) {
    homePath = path;
  }

  /**
   * @return the array of permission
   */
  public String[] getAllPermissions() {
    return permissions.split(",");
  }

  /**
   * Check the state of permission is existing or not
   * @param allPermissions the string array permission of drive
   * @param permission permission name
   * @return the state of permission is existing or not.
   */
  public boolean hasPermission(String[] allPermissions, String permission) {
    List<String> permissionList = new ArrayList<String>();
    for (String per : allPermissions) {
      permissionList.add(per.trim());
    }
    if (permission == null)
      return false;
    if (permission.indexOf(":/") > -1) {
      String[] array = permission.split(":/");
      if (array == null || array.length < 2)
        return false;
      if (permissionList.contains("*:/" + array[1]))
        return true;
    }
    return permissionList.contains(permission);
  }

  public int compareTo(TaxonomyTreeData arg) {
    return name.compareToIgnoreCase(arg.getName());
  }
}