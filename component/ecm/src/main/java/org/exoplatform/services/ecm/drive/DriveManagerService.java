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
package org.exoplatform.services.ecm.drive;

import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public interface DriveManagerService {

  final public String DRIVE_REGISTRY_PATH = "exo:services/exo:ecm/exo:drives".intern();     

  public DriveEntry getDrive(String repository, String group,String name, SessionProvider sessionProvider) throws Exception ;  
  public List<DriveEntry> getAllDrives(String repository, SessionProvider sessionProvider) throws Exception ;  
  public List<DriveEntry> getDrivesByGroup(String repository , String group, SessionProvider sessionProvider) throws Exception ;
  public List<DriveEntry> getDrivesByUser(String repository,String userId, SessionProvider sessionProvider) throws Exception ;

  public void addDrive(DriveEntry drive, SessionProvider sessionProvider) throws Exception ;
  public void removeDrive(String repository,String group,String name, SessionProvider sessionProvider)  throws Exception ;

}
