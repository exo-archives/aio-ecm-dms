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

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public interface ManageDriveService {

  /**
   * Register a new drive to workspace or update if the drive is existing
   * @param name  drive name
   * @param workspace  the workspace name where will store the drive
   * @param permissions  specify who can access to this drive
   * @param homePath  specify the location of drive
   * @param views  include all views can see in drive
   * @param icon  the drive icon which can see in drive browser
   * @param viewReferences  the boolean to set default for drive can view references node or not
   * @param viewNonDocument the boolean to set default for drive can view non document node or not
   * @param viewSideBar the boolean to set default for drive can view side bar or not
   * @param showHiddenNode  the boolean to set default for drive can see hidden node or not
   * @param repository the string contain repository name
   * @param allowCreateFolder the string to specify which type of folder can add in the drive
   * @throws Exception
   */
  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument,
                        boolean viewSideBar, boolean showHiddenNode, String repository, String allowCreateFolder)throws Exception ;
  /**
   * Return an DriveData Object
   * @param driveName  the string contain the drive name 
   * @param repository  the repository name
   * @see DriveData
   * @return  DriveData with specified drive name and repository
   * @throws Exception
   */
  public DriveData getDriveByName(String driveName, String repository) throws Exception;
  
  /**
   * Return the list of DriveData
   * This method will look up in all workspaces of repository to find DriveData with 
   * specified permission 
   * @param permission  the string contain the permission  
   * @param repository name of repository
   * @return  list of DriveData with specified repository and permission
   * @see DriveData
   * @throws Exception
   */
  public List<DriveData> getAllDriveByPermission(String permission, String repository) throws Exception;
  
  /**
   * Remove drive with specified drive name and repository
   * @param driveName  drive name
   * @param repository repository name
   * @throws Exception
   */
  public void removeDrive(String driveName, String repository) throws Exception;      
  
  /**
   * This method will look up in all workspaces of repository to find DriveData
   * @param repository repository name
   * @return list of DriveData with specified repository
   * @throws Exception
   */
  public List getAllDrives(String repository) throws Exception;
  
  /**
   * This method will check to make sure the view is not in used before remove this view
   * @param viewName view name
   * @param repository repository name
   * @return the status of current view is in used or not
   * @throws Exception
   */
  public boolean isUsedView(String viewName, String repository) throws Exception;
  
  /**
   * Register all drive plugins to repository
   * @param repository the string contain repository name
   * @throws Exception
   */
  public void init(String repository) throws Exception; 
  
}
