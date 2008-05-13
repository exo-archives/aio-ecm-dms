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
import java.util.Iterator;
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

  private ArrayList<String> permissionList1, permissionList2, viewList1, viewList2 ;

  public void setUp() throws Exception{
    super.setUp() ;
    driveManagerService_ = (DriveManagerService)container.getComponentInstanceOfType(DriveManagerService.class) ;
  }

  public void testAddDrive() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    DriveEntry driveEntry = new DriveEntry() ;
    driveEntry = createDriveEntryToTest("film", "person", "dzung/film", "icon/iconFilm", "ws", "allow", false, false, false, false, permissionList1, viewList1) ;
    assertNotNull(driveEntry) ;
    driveManagerService_.addDrive(driveEntry, sessionProvider);

    DriveEntry savedDrive = driveManagerService_.getDrive("repository", "person", "film", sessionProvider) ;

    assertNotNull(savedDrive) ;
    assertEquals("dzung/film", savedDrive.getHomePath()) ;
    assertEquals("icon/iconFilm", savedDrive.getIconPath()) ;
    assertEquals(driveEntry.getAccessPermissions() , savedDrive.getAccessPermissions()) ;

    driveManagerService_.removeDrive("repository", "person", "film", sessionProvider) ;
    sessionProvider.close() ;
  }

  public void testGetAllDrives() throws Exception{
    // create data to test
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    DriveEntry driveEntry1 = new DriveEntry() ;
    DriveEntry driveEntry2 = new DriveEntry() ;

    driveEntry1 = createDriveEntryToTest("film", "person", "dzung/film", "icon/iconFilm", "ws", "allow", false, false, false, false, permissionList1, viewList1) ;
    driveEntry2 = createDriveEntryToTest("music", "person", "dzung/music", "icon/iconMusic", "ws1", "allow", false, false, false, false, permissionList2, viewList2) ;

    driveManagerService_.addDrive(driveEntry1, sessionProvider) ;
    driveManagerService_.addDrive(driveEntry2, sessionProvider) ;

    // test
    List<DriveEntry> driveEntryList = driveManagerService_.getAllDrives("repository", sessionProvider) ;

    assertNotNull(driveEntryList) ;
    assertEquals(4, driveEntryList.size()) ;

    Iterator<DriveEntry> iteratorDrive = driveEntryList.iterator() ;
    while (iteratorDrive.hasNext()) {
      DriveEntry drive = iteratorDrive.next() ;
      assertNotNull(drive);
      if (drive.getHomePath().equalsIgnoreCase("dzung/film")) {
        assertGetDrives(driveEntry1, drive) ;
      }

      if (drive.getHomePath().equalsIgnoreCase("dzung/music")) {
        assertGetDrives(driveEntry2, drive) ;
      }
    }

    driveManagerService_.removeDrive("repository", "person", "film", sessionProvider) ;
    driveManagerService_.removeDrive("repository", "person", "music", sessionProvider) ;
    sessionProvider.close() ;
  }

  public void testGetDriveByGroup() throws Exception{
//  create data to test
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    DriveEntry driveEntry1 = new DriveEntry() ;
    DriveEntry driveEntry2 = new DriveEntry() ;

    driveEntry1 = createDriveEntryToTest("film", "person", "dzung/film", "icon/iconFilm", "ws", "allow", false, false, false, false, permissionList1, viewList1) ;
    driveEntry2 = createDriveEntryToTest("music", "public", "dzung/music", "icon/iconMusic", "ws1", "allow", false, false, false, false, permissionList2, viewList2) ;

    driveManagerService_.addDrive(driveEntry1, sessionProvider) ;
    driveManagerService_.addDrive(driveEntry2, sessionProvider) ;

    List<DriveEntry> driveEntryList = driveManagerService_.getDrivesByGroup("repository", "person", sessionProvider) ;

    assertNotNull(driveEntryList) ;

    Iterator<DriveEntry> iteratorDrive = driveEntryList.iterator() ;
    while (iteratorDrive.hasNext()) {
      DriveEntry drive = iteratorDrive.next() ;
      assertNotNull(drive);
      assertGetDrives(driveEntry1, drive) ;
    }

    List<DriveEntry> driveEntryList1 = driveManagerService_.getDrivesByGroup("repository", "Mygroup2", sessionProvider) ;
    assertNotNull(driveEntryList);
    assertEquals(1, driveEntryList.size()) ;
    for (DriveEntry drive: driveEntryList1) {
      assertEquals("Mydrive2" , drive.getName()) ;
      assertEquals("Mygroup2" , drive.getGroup()) ;
      assertEquals("*:/platform/administrators" , drive.getAccessPermissions().get(0)) ;
      assertEquals("Repository2", drive.getRepository()) ;
      assertEquals("system2", drive.getWorkspace()) ;
      assertEquals("HomePath2", drive.getHomePath()) ;
      assertEquals("IconPath2", drive.getIconPath()) ;
      assertEquals("system-view2", drive.getViews().get(0)) ;
      assertEquals(false, drive.getShowPreferences()) ;
      assertEquals(true, drive.getShowNonDocument()) ;
      assertEquals(true, drive.getShowSideBar()) ;
      assertEquals(true, drive.getShowHiddenNode()) ;
      assertEquals("both", drive.getAllowCreateFolder()) ;
    }
    
    driveManagerService_.removeDrive("repository", "person", "film", sessionProvider) ;
    driveManagerService_.removeDrive("repository", "public", "music", sessionProvider) ;
    sessionProvider.close() ;
  }
  
  public void testGetDriveByUser() throws Exception {
    
  }

  private DriveEntry createDriveEntryToTest(String name, String group, String homePath, String Icon, 
    String workspace, String allowCreateFolder, Boolean viewPreferences,
    Boolean viewNonDocument, Boolean viewSideBar, Boolean showHiddenNode, 
    ArrayList<String> permissionList, ArrayList<String> viewList) {
    DriveEntry drive = new DriveEntry() ;

    drive.setName(name) ;
    drive.setGroup(group) ;
    drive.setHomePath(homePath) ;
    drive.setIconPath(Icon) ;
    drive.setAllowCreateFolder(allowCreateFolder) ;
    drive.setWorkspace(workspace) ;
    drive.setShowPreferences(viewPreferences) ;
    drive.setShowHiddenNode(showHiddenNode) ;
    drive.setShowNonDocument(viewNonDocument) ;
    drive.setShowSideBar(viewSideBar) ;
    permissionList = new ArrayList<String>() ;
    viewList = new ArrayList<String>() ;
    for (int i = 0; i < 4; i++) {
      permissionList.add(name + i) ;
    }
    for (int i = 0; i < 4; i++) {
      viewList.add("view"+ name +  i) ;
    }

    drive.setAcessPermissions(permissionList) ;
    drive.setViews(viewList) ;

    return drive ;
  }

  private void assertGetDrives(DriveEntry orginalEntry, DriveEntry savedEntry) {
    assertEquals(orginalEntry.getIconPath(), savedEntry.getIconPath()) ;
    assertEquals(orginalEntry.getAllowCreateFolder(), savedEntry.getAllowCreateFolder()) ;
    assertEquals(orginalEntry.getAccessPermissions(), savedEntry.getAccessPermissions()) ;
    assertEquals(orginalEntry.getViews(), savedEntry.getViews()) ;
    assertEquals(orginalEntry.getShowHiddenNode(), savedEntry.getShowHiddenNode()) ;
    assertEquals(orginalEntry.getShowNonDocument(), savedEntry.getShowNonDocument()) ;
    assertEquals(orginalEntry.getShowPreferences(), savedEntry.getShowPreferences()) ;
    assertEquals(orginalEntry.getShowSideBar(), savedEntry.getShowSideBar()) ;
    assertEquals(orginalEntry.getWorkspace(), savedEntry.getWorkspace()) ;
    assertEquals(orginalEntry.getHomePath(), savedEntry.getHomePath()) ;
  }
}
