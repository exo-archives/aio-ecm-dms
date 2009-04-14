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

  private String taxoTreeName;

  private String repository;

  private String taxoTreeWorkspace;

  private String taxoTreePermissions;

  private String taxoTreeHomePath;

  public TaxonomyTreeData() {
    
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
   * @return the permissions of drive
   */
  public String getPermissions() {
    return this.taxoTreePermissions;
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
    return taxoTreeName.compareToIgnoreCase(arg.getTaxoTreeName());
  }


  /**
   * Get taxonomy tree home path
   * @return taxoTreeHomePath
   */
  public String getTaxoTreeHomePath() {
    return taxoTreeHomePath;
  }

  /**
   * Register home path to taxonomy Tree
   * @param path the home path of drive
   */
  public void setTaxoTreeHomePath(String taxoTreeHomePath) {
    this.taxoTreeHomePath = taxoTreeHomePath;
  }
  
  /**
   * get taxonomy tree name
   */
  public String getTaxoTreeName() {
    return taxoTreeName;
  }
  
  /**
   * Register taxonomy tree name
   * @param name the name of taxonomy tree
   */
  public void setTaxoTreeName(String taxoTreeName) {
    this.taxoTreeName = taxoTreeName;
  }

  public String getTaxoTreePermissions() {
    return taxoTreePermissions;
  }

  /**
   * Register permission to taxonomy tree
   * @param taxoTreePermissions
   */
  public void setTaxoTreePermissions(String taxoTreePermissions) {
    this.taxoTreePermissions = taxoTreePermissions;
  }

  /**
   * @return the name of workspace
   */
  public String getTaxoTreeWorkspace() {
    return taxoTreeWorkspace;
  }

  /**
   * Register workspace to tree
   * @param ws the workspace name
   */
  public void setTaxoTreeWorkspace(String taxoTreeWorkspace) {
    this.taxoTreeWorkspace = taxoTreeWorkspace;
  }
}