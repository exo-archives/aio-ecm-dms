/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives;

import java.util.List;
import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public interface ManageDriveService {

  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument,
                        boolean viewSideBar, String folderDisplay)throws Exception ;
  public Object getDriveByName(String driveName) throws Exception;
  public List<DriveData> getAllDriveByPermission(String permission) throws Exception;
  
  public void removeDrive(String driveName) throws Exception;  
  public Node getDriveHome() throws Exception ;  
  public List getAllDrives() throws Exception;
}
