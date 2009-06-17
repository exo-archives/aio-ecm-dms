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
package org.exoplatform.services.ecm.dms.drive;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * Jun 11, 2009  
 */
public class TestDriveService extends BaseDMSTestCase {
  private ManageDriveService driveService;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private Session session;
  private Node rootNode;
  private String drivePath;
  
  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:accessPermissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:icon".intern() ;
  private static String PATH = "exo:path".intern() ;
  private static String VIEW_REFERENCES = "exo:viewPreferences".intern() ;
  private static String VIEW_NON_DOCUMENT = "exo:viewNonDocument".intern() ;
  private static String VIEW_SIDEBAR = "exo:viewSideBar".intern() ;
  private static String SHOW_HIDDEN_NODE = "exo:showHiddenNode".intern() ;
  private static String ALLOW_CREATE_FOLDER = "exo:allowCreateFolder".intern() ;
  
  /**
   * Set up for testing
   * 
   * In Collaboration workspace
   * 
   *  /---TestTreeNode
   *        |
   *        |_____A1
   *        |     |___A1_1
   *        |         |___A1_1_1
   *        |     |___A1_2
   *        | 
   *        |_____B1
   *              |___B1_1
   *                    
   */
  public void setUp() throws Exception {
    super.setUp();
    driveService = (ManageDriveService)container.getComponentInstanceOfType(ManageDriveService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    drivePath = nodeHierarchyCreator.getJcrPath(BasePath.EXO_DRIVES_PATH);
    createTree();
  }
  
  public void createTree() throws Exception {
    session = repository.login(credentials, COLLABORATION_WS);
    rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TestTreeNode");
    Node nodeA1 = testNode.addNode("A1");
    nodeA1.addNode("A1_1").addNode("A1_1_1");
    nodeA1.addNode("A1_2");
    testNode.addNode("B1").addNode("B1_1");
    session.save();
  }
  
  public void testInit() throws Exception {
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node myDrive = (Node)mySession.getItem(drivePath);
    assertEquals(myDrive.getNodes().getSize(), 3);
  }
  
  public void testAddDrive() throws Exception {
    driveService.addDrive("MyDrive", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node myDrive = (Node)mySession.getItem(drivePath + "/MyDrive");
    assertNotNull(myDrive);
    assertEquals(myDrive.getProperty(WORKSPACE).getString(), COLLABORATION_WS) ;
    assertEquals(myDrive.getProperty(PERMISSIONS).getString(), "*:/platform/administrators");
    assertEquals(myDrive.getProperty(PATH).getString(), "/TestTreeNode/A1");      
    assertEquals(myDrive.getProperty(VIEWS).getString(), "admin-view");
    assertEquals(myDrive.getProperty(ICON).getString(), "");
    assertEquals(myDrive.getProperty(VIEW_REFERENCES).getBoolean(), true);
    assertEquals(myDrive.getProperty(VIEW_NON_DOCUMENT).getBoolean(), true);
    assertEquals(myDrive.getProperty(VIEW_SIDEBAR).getBoolean(), true);
    assertEquals(myDrive.getProperty(ALLOW_CREATE_FOLDER).getString(), "nt:folder");
    assertEquals(myDrive.getProperty(SHOW_HIDDEN_NODE).getBoolean(), true);
  }
  
  public void testGetDriveByName() throws Exception {
    driveService.addDrive("MyDrive", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    DriveData driveData1 = driveService.getDriveByName("abc", REPO_NAME);
    assertNull(driveData1);
    DriveData driveData2 = driveService.getDriveByName("MyDrive", REPO_NAME);
    assertNotNull(driveData2);
    assertEquals(driveData2.getWorkspace(), COLLABORATION_WS) ;
    assertEquals(driveData2.getPermissions(), "*:/platform/administrators");
    assertEquals(driveData2.getHomePath(), "/TestTreeNode/A1");      
    assertEquals(driveData2.getViews(), "admin-view");
    assertEquals(driveData2.getIcon(), "");
    assertEquals(driveData2.getViewPreferences(), true);
    assertEquals(driveData2.getViewNonDocument(), true);
    assertEquals(driveData2.getViewSideBar(), true);
    assertEquals(driveData2.getAllowCreateFolder(), "nt:folder");
    assertEquals(driveData2.getShowHiddenNode(), true);
  }
  
  public void testGetAllDrives() throws Exception {
    driveService.addDrive("MyDrive1", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    driveService.addDrive("MyDrive2", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_1", "admin-view, system-view", "", true, true, true, false, REPO_NAME, "Both");
    List<DriveData> listDriveData = driveService.getAllDrives(REPO_NAME);
    assertEquals(listDriveData.size(), 2);
    assertEquals(listDriveData.get(0).getWorkspace(), COLLABORATION_WS) ;
    assertEquals(listDriveData.get(0).getPermissions(), "*:/platform/administrators");
    assertEquals(listDriveData.get(0).getHomePath(), "/TestTreeNode/A1");      
    assertEquals(listDriveData.get(0).getViews(), "admin-view");
    assertEquals(listDriveData.get(0).getIcon(), "");
    assertEquals(listDriveData.get(0).getViewPreferences(), true);
    assertEquals(listDriveData.get(0).getViewNonDocument(), true);
    assertEquals(listDriveData.get(0).getViewSideBar(), true);
    assertEquals(listDriveData.get(0).getAllowCreateFolder(), "nt:folder");
    assertEquals(listDriveData.get(0).getShowHiddenNode(), true);
    
    assertEquals(listDriveData.get(1).getWorkspace(), COLLABORATION_WS) ;
    assertEquals(listDriveData.get(1).getPermissions(), "*:/platform/user");
    assertEquals(listDriveData.get(1).getHomePath(), "/TestTreeNode/A1_1");      
    assertEquals(listDriveData.get(1).getViews(), "admin-view, system-view");
    assertEquals(listDriveData.get(1).getIcon(), "");
    assertEquals(listDriveData.get(1).getViewPreferences(), true);
    assertEquals(listDriveData.get(1).getViewNonDocument(), true);
    assertEquals(listDriveData.get(1).getViewSideBar(), true);
    assertEquals(listDriveData.get(1).getAllowCreateFolder(), "Both");
    assertEquals(listDriveData.get(1).getShowHiddenNode(), false);
  }
  
  public void testRemoveDrive() throws Exception {
    driveService.addDrive("MyDrive1", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    driveService.addDrive("MyDrive2", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_1", "admin-view, system-view", "", true, true, true, false, REPO_NAME, "Both");
    assertEquals(driveService.getAllDrives(REPO_NAME).size(), 2);
    driveService.removeDrive("MyDrive1", REPO_NAME);
    assertEquals(driveService.getAllDrives(REPO_NAME).size(), 1);
    driveService.removeDrive("xXx", REPO_NAME);
    assertEquals(driveService.getAllDrives(REPO_NAME).size(), 1);
  }
  
  public void testGetAllDriveByPermission() throws Exception {
    driveService.addDrive("MyDrive1", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    driveService.addDrive("MyDrive2", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_1", "admin-view, system-view", "", true, true, true, false, REPO_NAME, "Both");
    driveService.addDrive("MyDrive3", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_2", "system-view", "", true, true, true, true, REPO_NAME, "nt:unstructured");
    List<DriveData> listDriveData = driveService.getAllDrives(REPO_NAME);
    assertEquals(listDriveData.size(), 3);
    List<DriveData> driveDatas = driveService.getAllDriveByPermission("*:/platform/user", REPO_NAME);
    assertEquals(driveDatas.size(), 2);
    assertEquals(driveDatas.get(0).getWorkspace(), COLLABORATION_WS) ;
    assertEquals(driveDatas.get(0).getPermissions(), "*:/platform/user");
    assertEquals(driveDatas.get(0).getHomePath(), "/TestTreeNode/A1_1");      
    assertEquals(driveDatas.get(0).getViews(), "admin-view, system-view");
    assertEquals(driveDatas.get(0).getIcon(), "");
    assertEquals(driveDatas.get(0).getViewPreferences(), true);
    assertEquals(driveDatas.get(0).getViewNonDocument(), true);
    assertEquals(driveDatas.get(0).getViewSideBar(), true);
    assertEquals(driveDatas.get(0).getAllowCreateFolder(), "Both");
    assertEquals(driveDatas.get(0).getShowHiddenNode(), false);
    
    assertEquals(driveDatas.get(1).getWorkspace(), COLLABORATION_WS) ;
    assertEquals(driveDatas.get(1).getPermissions(), "*:/platform/user");
    assertEquals(driveDatas.get(1).getHomePath(), "/TestTreeNode/A1_2");      
    assertEquals(driveDatas.get(1).getViews(), "system-view");
    assertEquals(driveDatas.get(1).getIcon(), "");
    assertEquals(driveDatas.get(1).getViewPreferences(), true);
    assertEquals(driveDatas.get(1).getViewNonDocument(), true);
    assertEquals(driveDatas.get(1).getViewSideBar(), true);
    assertEquals(driveDatas.get(1).getAllowCreateFolder(), "nt:unstructured");
    assertEquals(driveDatas.get(1).getShowHiddenNode(), true);
    
    List<DriveData> driveDatas2 = driveService.getAllDriveByPermission("*:/platform/xXx", REPO_NAME);
    assertEquals(driveDatas2.size(), 0);
  }
  
  public void testIsUsedView() throws Exception {
    driveService.addDrive("MyDrive1", COLLABORATION_WS, "*:/platform/administrators", "/TestTreeNode/A1", "admin-view", "", true, true, true, true, REPO_NAME, "nt:folder");
    driveService.addDrive("MyDrive2", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_1", "admin-view, system-view", "", true, true, true, false, REPO_NAME, "Both");
    driveService.addDrive("MyDrive3", COLLABORATION_WS, "*:/platform/user", "/TestTreeNode/A1_2", "system-view", "", true, true, true, true, REPO_NAME, "nt:unstructured");
    List<DriveData> listDriveData = driveService.getAllDrives(REPO_NAME);
    assertEquals(listDriveData.size(), 3);
    assertTrue(driveService.isUsedView("system-view", REPO_NAME));
    assertFalse(driveService.isUsedView("xXx", REPO_NAME));
  }
  
  public void tearDown() throws Exception {
    Node root;
    try {
      Session mySession = repository.login(credentials, DMSSYSTEM_WS);
      Node rootDrive = (Node)mySession.getItem(drivePath);
      NodeIterator iter = rootDrive.getNodes();
      while (iter.hasNext()) {
        iter.nextNode().remove();
      }
      rootDrive.getSession().save();
      session = repository.login(credentials, COLLABORATION_WS);
      root = session.getRootNode();
      root.getNode("TestTreeNode").remove();
      root.save();
    } catch (PathNotFoundException e) {
    }
    super.tearDown();
  }
}
