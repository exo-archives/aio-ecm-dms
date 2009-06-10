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
package org.exoplatform.services.ecm.dms.link;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * Jun 9, 2009  
 */
public class TestLinkManager extends BaseDMSTestCase {
  private LinkManager linkManager;
  private Session session;
  private Node rootNode;
  
  private final static String WORKSPACE = "exo:workspace";
  private final static String UUID = "exo:uuid";
  private final static String PRIMARY_TYPE = "exo:primaryType";
  
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
   *        |     |___A1_3
   *        |     |___A1_4
   *        | 
   *        |_____B1
   *              |___B1_1
   *                    
   */
  public void setUp() throws Exception {
    super.setUp();
    linkManager = (LinkManager) container.getComponentInstanceOfType(LinkManager.class);
    createTree();
  }
  
  public void createTree() throws Exception {
    session = repository.login(credentials, COLLABORATION_WS);
    rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TestTreeNode");
    Node nodeA1 = testNode.addNode("A1");
    nodeA1.addNode("A1_1").addNode("A1_1_1");
    nodeA1.addNode("A1_2");
    nodeA1.addNode("A1_3");
    nodeA1.addNode("A1_4");
    
    testNode.addNode("B1").addNode("B1_1");
    session.save();
    assertNotNull(nodeA1);
  }
  
  public void testCreateLink() throws Exception {
    System.out.println("================== Test Create Link  ==================");
//    Test method createLink(Node parent, Node target)
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    assertEquals(symlinkNodeA1.getName(), nodeB1_1.getName());
    assertEquals(symlinkNodeA1.getProperty(WORKSPACE).getString(), COLLABORATION_WS);
    assertEquals(symlinkNodeA1.getProperty(UUID).getString(), nodeB1_1.getUUID());
    assertEquals(symlinkNodeA1.getProperty(PRIMARY_TYPE).getString(), nodeB1_1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeA1.getPrimaryNodeType().getName(), "exo:symlink");
    
//    Test method createLink(Node parent, String linkType, Node target)
//    linkType = "exo:taxonomyLink"
    Node nodeA1_1 = rootNode.getNode("TestTreeNode/A1/A1_1");
    Node symlinkNodeA1_1 = linkManager.createLink(nodeA1_1, "exo:taxonomyLink", nodeB1_1);
    assertNotNull(symlinkNodeA1_1);
    assertEquals(symlinkNodeA1_1.getName(), nodeB1_1.getName());
    assertEquals(symlinkNodeA1_1.getProperty(WORKSPACE).getString(), COLLABORATION_WS);
    assertEquals(symlinkNodeA1_1.getProperty(UUID).getString(), nodeB1_1.getUUID());
    assertEquals(symlinkNodeA1_1.getProperty(PRIMARY_TYPE).getString(), nodeB1_1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeA1_1.getPrimaryNodeType().getName(), "exo:taxonomyLink");
    
//    Test method createLink(Node parent, String linkType, Node target)
//    linkType = null
    Node nodeA1_2 = rootNode.getNode("TestTreeNode/A1/A1_2");
    Node symlinkNodeA1_2 = linkManager.createLink(nodeA1_2, null, nodeB1_1);
    assertNotNull(symlinkNodeA1_2);
    assertEquals(symlinkNodeA1_2.getName(), nodeB1_1.getName());
    assertEquals(symlinkNodeA1_2.getProperty(WORKSPACE).getString(), COLLABORATION_WS);
    assertEquals(symlinkNodeA1_2.getProperty(UUID).getString(), nodeB1_1.getUUID());
    assertEquals(symlinkNodeA1_2.getProperty(PRIMARY_TYPE).getString(), nodeB1_1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeA1_2.getPrimaryNodeType().getName(), "exo:symlink");
    
//    Test method createLink(Node parent, String linkType, Node target, String linkName)
//    linkType = "exo:taxonomyLink"; linkName = null
    Node nodeA1_3 = rootNode.getNode("TestTreeNode/A1/A1_3");
    Node symlinkNodeA1_3 = linkManager.createLink(nodeA1_3, "exo:taxonomyLink", nodeB1_1, null);
    assertNotNull(symlinkNodeA1_3);
    assertEquals(symlinkNodeA1_3.getName(), nodeB1_1.getName());
    assertEquals(symlinkNodeA1_3.getProperty(WORKSPACE).getString(), COLLABORATION_WS);
    assertEquals(symlinkNodeA1_3.getProperty(UUID).getString(), nodeB1_1.getUUID());
    assertEquals(symlinkNodeA1_3.getProperty(PRIMARY_TYPE).getString(), nodeB1_1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeA1_3.getPrimaryNodeType().getName(), "exo:taxonomyLink");
    
//    Test method createLink(Node parent, String linkType, Node target, String linkName)
//    linkType = "exo:taxonomyLink"; linkName = "A1_3_To_B1_1"
    Node nodeA1_4 = rootNode.getNode("TestTreeNode/A1/A1_4");
    Node symlinkNodeA1_4 = linkManager.createLink(nodeA1_4, "exo:taxonomyLink", nodeB1_1, "A1_3_To_B1_1");
    assertNotNull(symlinkNodeA1_4);
    assertEquals(symlinkNodeA1_4.getName(), "A1_3_To_B1_1");
    assertEquals(symlinkNodeA1_4.getProperty(WORKSPACE).getString(), COLLABORATION_WS);
    assertEquals(symlinkNodeA1_4.getProperty(UUID).getString(), nodeB1_1.getUUID());
    assertEquals(symlinkNodeA1_4.getProperty(PRIMARY_TYPE).getString(), nodeB1_1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeA1_4.getPrimaryNodeType().getName(), "exo:taxonomyLink");
  }
  
  public void testIsLink() throws Exception {
    System.out.println("================== Test Is Link  ==================");
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    assertTrue(linkManager.isLink(symlinkNodeA1));
    assertFalse(linkManager.isLink(nodeA1));
  }
  
  public void testGetTarget() throws Exception {
    System.out.println("================== Test Get Target  ==================");
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    
    try {
      linkManager.getTarget(nodeA1);
    } catch (Exception e) {
      fail("\nNode: " + nodeA1.getName() + " is not a symlink");
    }
    assertNotNull(linkManager.getTarget(symlinkNodeA1));
    assertEquals(linkManager.getTarget(symlinkNodeA1), nodeB1_1.getName());
    
    assertNotNull(linkManager.getTarget(symlinkNodeA1, true));
    assertEquals(linkManager.getTarget(symlinkNodeA1, true), nodeB1_1.getName());
  }
  
  public void testIsTargetReachable() throws Exception {
    System.out.println("================== Test IsTargetReachable  ==================");
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    
    try {
      boolean isReachable = linkManager.isTargetReachable(nodeA1);
      assertFalse(isReachable);
    } catch (Exception e) {
      fail("\nNode: " + nodeA1.getName() + " is not a symlink");
    }
    assertTrue(linkManager.isTargetReachable(symlinkNodeA1));
  }
  
  public void testGetTargetPrimaryNodeType() throws Exception {
    System.out.println("================== Test GetTargetPrimaryNodeType  ==================");
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    try {
      String primaryNodeType = linkManager.getTargetPrimaryNodeType(nodeA1);
      System.out.println("\n\n====" + primaryNodeType);
    } catch (Exception e) {
      fail("\nNode: " + nodeA1.getName() + " is not a symlink");
    }
    assertEquals(linkManager.getTargetPrimaryNodeType(symlinkNodeA1), nodeB1_1.getPrimaryNodeType().getName());
  }
  
  public void testUpdateLink() throws Exception {
    System.out.println("================== Test Update Link  ==================");
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeB1 = rootNode.getNode("TestTreeNode/B1");
    Node nodeB1_1 = rootNode.getNode("TestTreeNode/B1/B1_1");
    Node symlinkNodeA1 = linkManager.createLink(nodeA1, nodeB1_1);
    assertNotNull(symlinkNodeA1);
    
    Node symlinkNodeUpdate = linkManager.updateLink(symlinkNodeA1, nodeB1);
    assertNotNull(symlinkNodeUpdate);
    assertEquals(symlinkNodeUpdate.getName(), symlinkNodeA1.getName());
    assertEquals(symlinkNodeUpdate.getProperty(WORKSPACE).getString(), nodeB1.getSession().getWorkspace().getName());
    assertEquals(symlinkNodeUpdate.getProperty(UUID).getString(), nodeB1.getUUID());
    assertEquals(symlinkNodeUpdate.getProperty(PRIMARY_TYPE).getString(), nodeB1.getPrimaryNodeType().getName());
    assertEquals(symlinkNodeUpdate.getPrimaryNodeType().getName(), symlinkNodeA1.getPrimaryNodeType().getName());
  }
  
  public void tearDown() throws Exception {
    Node root;
    try {
      session = repository.login(credentials, COLLABORATION_WS);
      root = session.getRootNode();
      root.getNode("TestTreeNode").remove();
      root.save();
    } catch (PathNotFoundException e) {
    }
    super.tearDown();
  }
}
