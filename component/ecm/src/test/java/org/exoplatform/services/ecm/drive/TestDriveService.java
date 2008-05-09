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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Workspace;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.ecm.drive.impl.DriveManagerServiceImpl;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 8, 2008  
 */
public class TestDriveService extends BaseECMTestCase {
  
  private DriveManagerService driveManagerService_ ;
  private DriveEntry driveEntry ;
  
  public void setUp() throws Exception{
    super.setUp() ;
    driveManagerService_ = (DriveManagerService)container.getComponentInstanceOfType(DriveManagerService.class) ;
  }
  
  public void testAddDrive() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    DriveEntry driveEntry = new DriveEntry() ;
    driveEntry.setName("film") ;
    driveEntry.setGroup("person") ;
    driveEntry.setHomePath("dzung/film") ;
    driveEntry.setIcon("icon/iconFilm") ;
    driveEntry.setAllowCreateFolder("allow") ;
    driveEntry.setWorkspace("ws") ;
    driveEntry.setViewPreferences(false) ;
    driveEntry.setShowHiddenNode(false) ;
    driveEntry.setViewNonDocument(false) ;
    driveEntry.setViewSideBar(true) ;
    ArrayList<String> permissionList = new ArrayList<String>() ;
    ArrayList<String> viewList = new ArrayList<String>() ;
    for (int i = 0; i < 4; i++) {
      permissionList.add("film" + i) ;
    }
    for (int i = 0; i < 4; i++) {
      viewList.add("view" + i) ;
    }
    
    driveEntry.setAcessPermissions(permissionList) ;
    driveEntry.setViews(viewList) ;
    
    driveManagerService_.addDrive(driveEntry, sessionProvider);
    
    DriveEntry savedDrive = driveManagerService_.getDrive("repository", "person", "film", sessionProvider) ;
    
    assertNotNull(savedDrive) ;
    assertEquals("dzung/film", savedDrive.getHomePath()) ;
    assertEquals("icon/iconFilm", savedDrive.getIcon()) ;
    assertEquals(permissionList , savedDrive.getAccessPermissions()) ;
    
    driveManagerService_.removeDrive("repository", "person", "film", sessionProvider) ;
    sessionProvider.close() ;
  }
  
  public void testGetAllDrives() throws Exception{
    // create data to test
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    DriveEntry driveEntry = new DriveEntry() ;
    driveEntry.setName("film") ;
    driveEntry.setGroup("person") ;
    driveEntry.setHomePath("dzung/film") ;
    driveEntry.setIcon("icon/iconFilm") ;
    driveEntry.setAllowCreateFolder("allow") ;
    driveEntry.setWorkspace("ws") ;
    driveEntry.setViewPreferences(false) ;
    driveEntry.setShowHiddenNode(false) ;
    driveEntry.setViewNonDocument(false) ;
    driveEntry.setViewSideBar(true) ;
    ArrayList<String> permissionList = new ArrayList<String>() ;
    ArrayList<String> viewList = new ArrayList<String>() ;
    for (int i = 0; i < 4; i++) {
      permissionList.add("film" + i) ;
    }
    for (int i = 0; i < 4; i++) {
      viewList.add("viewFilm" + i) ;
    }
    
    driveEntry.setAcessPermissions(permissionList) ;
    driveEntry.setViews(viewList) ;
    
    driveManagerService_.addDrive(driveEntry, sessionProvider) ;
    
    driveEntry.setName("Music") ;
    driveEntry.setGroup("person") ;
    driveEntry.setHomePath("dzung/music") ;
    driveEntry.setIcon("icon/iconMusic") ;
    driveEntry.setAllowCreateFolder("allow") ;
    driveEntry.setWorkspace("ws1") ;
    driveEntry.setViewPreferences(true) ;
    driveEntry.setViewPreferences(false) ;
    driveEntry.setViewNonDocument(true) ;
    driveEntry.setViewSideBar(true) ;
    for (int i = 0; i < 4; i++) {
      permissionList.add("music" + i) ;
    }
    for (int i = 0; i < 4; i++) {
      viewList.add("viewMusic" + i) ;
    }
    
    driveEntry.setAcessPermissions(permissionList) ;
    driveEntry.setViews(viewList) ;
    
    driveManagerService_.addDrive(driveEntry, sessionProvider) ;
    
    // test
    List<DriveEntry> driveEntryList = driveManagerService_.getAllDrives("repository", sessionProvider) ;
    
    assertNotNull(driveEntryList) ;
    assertEquals(2, driveEntryList.size()) ;
  }
}
