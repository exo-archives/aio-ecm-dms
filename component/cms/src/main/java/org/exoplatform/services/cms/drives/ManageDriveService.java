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

  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument,
                        boolean viewSideBar, boolean showHiddenNode, String repository, String allowCreateFolder)throws Exception ;
  public DriveData getDriveByName(String driveName, String repository) throws Exception;
  public List<DriveData> getAllDriveByPermission(String permission, String repository) throws Exception;
  
  public void removeDrive(String driveName, String repository) throws Exception;      
  public List getAllDrives(String repository) throws Exception;
  public boolean isUsedView(String viewName, String repository) throws Exception;
  public void init(String repository) throws Exception; 
  
}
