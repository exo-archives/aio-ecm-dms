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
package org.exoplatform.services.ecm.dms.test;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.login.LoginException;

import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Mar 15, 2009  
 */
public class TestSymLink extends BaseDMSTestCase {
  
  /**
   * Session works with workspace
   */
  Session         session = null;

  SessionProvider sessionProvider;

  /**
   * Set up for testing
   * 
   *  /---TestTreeNode
   *        |
   *        |_____A1
   *        |     |___C1___C1-1___C1-2___C1-3(exo:symlink -->C2)
   *        |     |___C2___C2-2___D(exo:symlink -->C3) 
   *        |     |___C3___C4
   *        | 
   *        |_____A2
   *        |     |___B2(exo:symlink --> C1)
   *        | 
   *        |_____A3(exo:symlink --> C2) 
   * 
   */
  public void setUp() {
    System.out.println("========== Create root node  ========");
    try {
      super.setUp();
      credentials = new CredentialsImpl("root", "exo".toCharArray());
      repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      session = repository.login(credentials, COLLABORATION_WS);
      createTreeInCollaboration();
    } catch (LoginException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchWorkspaceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create tree in System workspace
   *        /------TestTreeNode2
   *                    |
   *                    |_____M1
   *                    |
   *                    |_____N1
   *                    |
   *                    |_____P1
   * 
   * @throws Exception
   */
  public void createTreeInSystem() throws Exception {
    session = repository.login(credentials, SYSTEM_WS);
    Node rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TestTreeNode2");
    Node nodeM1 = testNode.addNode("M1");
    Node nodeN1 = testNode.addNode("N1");
    Node nodeP1 = testNode.addNode("p1");
    /* Set node and properties for Node B1 */
    session.save();
  }
  
  public void addSymlink(Node src, Node target) throws Exception {
    Node nodeM2 = src.addNode("M2","exo:symlink");
    if (target.hasProperty("jcr:uuid")) {
      System.out.println("\n\n jcr uuid = " + target.getProperty("jcr:uuid").getValue());
    } else {
      target.addMixin("mix:referenceable");
    }
    nodeM2.setProperty("exo:workspace",COLLABORATION_WS);
    nodeM2.setProperty("exo:uuid",target.getProperty("jcr:uuid").getString());
    nodeM2.setProperty("exo:primaryType",target.getPrimaryNodeType().getName());    
    target.getSession().save();
    src.getSession().save();
    
  }
  public void createTreeInCollaboration() throws Exception {
    Node rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TestTreeNode");
    Node nodeA1 = testNode.addNode("A1");
    Node nodeA2 = testNode.addNode("A2");
    Node nodeB2 = nodeA2.addNode("B2","exo:symlink");
    
    Node nodeC1 = nodeA1.addNode("C1");
    Node nodeC1_1 = nodeC1.addNode("C1_1");
    Node nodeC1_2 = nodeC1_1.addNode("C1_2");
    Node nodeC1_3 = nodeC1_2.addNode("C1_3","exo:symlink");
    Node nodeC2 = nodeA1.addNode("C2");
    Node nodeC3 = nodeA1.addNode("C3");
    Node nodeC4 = nodeC3.addNode("C4");
    Node nodeC2_2 = nodeC2.addNode("C2_2");
    Node nodeD = nodeC2_2.addNode("D", "exo:symlink");
    if (nodeC1.hasProperty("jcr:uuid")) {
      System.out.println("\n\n jcr uuid = " +nodeC1.getProperty("jcr:uuid").getValue());
    } else {
      nodeC1.addMixin("mix:referenceable");
      nodeC2.addMixin("mix:referenceable");
      nodeC3.addMixin("mix:referenceable");
    }
    nodeB2.setProperty("exo:workspace",COLLABORATION_WS);
    nodeB2.setProperty("exo:uuid",nodeC1.getProperty("jcr:uuid").getString());
    nodeB2.setProperty("exo:primaryType",nodeC1.getPrimaryNodeType().getName());    
    nodeD.setProperty("exo:workspace",COLLABORATION_WS);
    nodeD.setProperty("exo:uuid",nodeC3.getProperty("jcr:uuid").getString());
    nodeD.setProperty("exo:primaryType",nodeC3.getPrimaryNodeType().getName());    
    
    Node nodeA3 = testNode.addNode("A3","exo:symlink");
    nodeA3.setProperty("exo:workspace", COLLABORATION_WS);
    nodeA3.setProperty("exo:uuid", nodeC2.getProperty("jcr:uuid").getString());
    nodeA3.setProperty("exo:primaryType",nodeC2.getPrimaryNodeType().getName());
    
    nodeC1_3.setProperty("exo:workspace", COLLABORATION_WS);
    nodeC1_3.setProperty("exo:uuid", nodeC2.getProperty("jcr:uuid").getString());
    nodeC1_3.setProperty("exo:primaryType",nodeC2.getPrimaryNodeType().getName());
    System.out.println("Get path = " + nodeA3.getPath());
    
    /* Set node and properties for Node B1 */
    session.save();
  }

  /**
   * Browser tree of one node
   * 
   * @param node
   */
  
  public void browserTree(Node node, int iLevel) throws RepositoryException {
    if (iLevel != 0) {
      for (int j = 0; j < iLevel; j++) {
        System.out.print("\t");
        System.out.print("|");
      }
      System.out.println("-------" + node.getName());
    } else
      System.out.println(node.getName());

    for (int j = 0; j < iLevel; j++) {
      System.out.print("\t");
      System.out.print("|");
    }
    System.out.print("\t");
    System.out.println("|");
    for (int j = 0; j < iLevel; j++) {
      System.out.print("\t");
      System.out.print("|");
    }
    System.out.print("\t");
    System.out.println("|");
    /* Get all nodes */
    NodeIterator iterNode = node.getNodes();
    /* Browser node */
    Node tempNode;
    /*
     * for(int j = 0; j < iLevel + 1; j++){ System.out.print("\t"); }
     */
    while (iterNode.hasNext()) {
      for (int j = 0; j < iLevel; j++) {
        System.out.print("\t");
        System.out.print("|");
      }
      System.out.print("\t");
      System.out.println("|");
      for (int j = 0; j < iLevel; j++) {
        System.out.print("\t");
        System.out.print("|");
      }
      System.out.print("\t");
      System.out.println("|");

      tempNode = iterNode.nextNode();
      this.browserTree(tempNode, iLevel + 1);
    }
  }
  
  public void testGetPath() throws Exception {
    String path = "/";
    String expectedPath = "/";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path out put: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
  
  public void testGetPath1() throws Exception {
    String path = "/TestTreeNode";
    String expectedPath = "/TestTreeNode";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path out put: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
  
  public void testGetPath2() throws Exception {
    String path = "/TestTreeNode/A2/B2";
    String expectedPath = "/TestTreeNode/A1/C1";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
  
  public void testGetPath3() throws Exception {
    String path = "/TestTreeNode/A3/C2_2/D/C4";
    String expectedPath = "/TestTreeNode/A1/C3/C4";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
  
  public void testGetPath4() throws Exception {
    String path = "/TestTreeNode/A2/B2/C1_1/C1_2/C1_3/C2_2/D";
    String expectedPath = "/TestTreeNode/A1/C3";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
 
  public void testGetPath5() throws Exception {
    String path = "/TestTreeNode/A1/C1/C1_1/C1_2/C1_3/C2_2/D/C4";
    String expectedPath = "/TestTreeNode/A1/C3/C4";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    }
  }
  
  
  public void testGetPathInOtherWorkspace1() {
    try {
      Node nodeC1 = (Node)session.getItem("/TestTreeNode/A1/C1");
      createTreeInSystem();
      Session systemSession = repository.login(credentials, SYSTEM_WS);
      Node nodeM1 = (Node)systemSession.getItem("/TestTreeNode2/M1");
      addSymlink(nodeM1, nodeC1);
      String path = "/TestTreeNode2/M1/M2";
      String expectedPath = "/TestTreeNode/A1/C1";
      System.out.println("\n\n Path input : " + path);
      System.out.println("\n\n expected Path : " + expectedPath);
      NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
      Node node;
      if (path.length() == 0) return;
      node = (Node)nodeFinder.getItem(REPO_NAME, SYSTEM_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  public void testGetPathInOtherWorkspace2() {
    try {
      Node nodeC2 = (Node)session.getItem("/TestTreeNode/A1/C2");
      createTreeInSystem();
      Session systemSession = repository.login(credentials, SYSTEM_WS);
      Node nodeM1 = (Node)systemSession.getItem("/TestTreeNode2/M1");
      addSymlink(nodeM1, nodeC2);
      String path = "/TestTreeNode2/M1/M2/C2_2/D/C4";
      String expectedPath = "/TestTreeNode/A1/C3/C4";
      System.out.println("\n\n Path input : " + path);
      System.out.println("\n\n expected Path : " + expectedPath);
      NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
      Node node;
      if (path.length() == 0) return;
      node = (Node)nodeFinder.getItem(REPO_NAME, SYSTEM_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  /**
   * Test get path with target node is in other workspace
   *
   */
  
  public void testGetPathInOtherWorkspace3() {
    try {
      Node nodeC1 = (Node)session.getItem("/TestTreeNode/A1/C1");
      createTreeInSystem();
      Session systemSession = repository.login(credentials, SYSTEM_WS);
      Node nodeM1 = (Node)systemSession.getItem("/TestTreeNode2/M1");
      addSymlink(nodeM1, nodeC1);
      String path = "/TestTreeNode2/M1/M2/C1_1/C1_2/C1_3/C2_2/D/C4";
      String expectedPath = "/TestTreeNode/A1/C3/C4";
      System.out.println("\n\n Path input : " + path);
      System.out.println("\n\n expected Path : " + expectedPath);
      NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
      Node node;
      if (path.length() == 0) return;
      node = (Node)nodeFinder.getItem(REPO_NAME, SYSTEM_WS, path);
      System.out.println("Path output: "+ node.getPath());
      assertEquals(expectedPath,node.getPath());
    } catch (PathNotFoundException e) {
      assertTrue(false);
    } catch (RepositoryException e) {
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  public void testGetInvalidPath1() throws Exception {
    String path = "/TestTreeNode/A2/D";
    String expectedPath = "";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    boolean flag = false;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
    } catch (PathNotFoundException e) {
      flag = true;
    } catch (RepositoryException e) {
      flag = true;
    }
    assertTrue(flag);
  }
  
  
  public void testGetInvalidPath2() throws Exception {
    String path = "/TestTreeNode/A2/B2/C2";
    String expectedPath = "";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    boolean flag = false;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
    } catch (PathNotFoundException e) {
      flag = true;
    } catch (RepositoryException e) {
      flag = true;
    }
    assertTrue(flag);
  }
  
  /**
   * Test with target Node is remove: Throws PathNotFoundException
   */
  
  public void testGetInvalidPath3() throws Exception {
    String path = "/TestTreeNode/A3/C2_2";
    String expectedPath = "";
    Node nodeC2 = (Node)session.getItem("/TestTreeNode/A1/C2"); 
    Node nodeA1 = nodeC2.getParent();
    nodeC2.remove();
    nodeA1.save();
    browserTree(nodeA1.getParent(),0);
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : " + expectedPath);
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    boolean flag = false;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
    } catch (PathNotFoundException e) {
      flag = true;
    } catch (RepositoryException e) {
      flag = true;
    }
    assertTrue(flag);
  }

  
  public void testGetInvalidPath4() throws Exception {
    Node nodeC1 = (Node)session.getItem("/TestTreeNode/A1/C1");
    createTreeInSystem();
    Session systemSession = repository.login(credentials, SYSTEM_WS);
    Node nodeM1 = (Node)systemSession.getItem("/TestTreeNode2/M1");
    addSymlink(nodeM1, nodeC1);
    Node nodeA1 = nodeC1.getParent();
    nodeC1.remove();
    nodeA1.save();
    String path = "/TestTreeNode2/M1/M2";
    System.out.println("\n\n Path input : " + path);
    System.out.println("\n\n expected Path : Not found");
    NodeFinder nodeFinder = (NodeFinder)container.getComponentInstanceOfType(NodeFinder.class);
    Node node;
    if (path.length() == 0) return;
    boolean flag = false;
    try {
      node = (Node)nodeFinder.getItem(REPO_NAME, COLLABORATION_WS, path);
      System.out.println("Path output: "+ node.getPath());
    } catch (PathNotFoundException e) {
      flag = true;
    } catch (RepositoryException e) {
      flag = true;
    }
    assertTrue(flag);
  }
  
  public void tearDown() throws Exception {
    Node root;
    try {
      System.out.println("\n\n -----------Teadown-----------------");
      session = repository.login(credentials, COLLABORATION_WS);
      root = session.getRootNode();
      root.getNode("TestTreeNode").remove();
      root.save();
      session = repository.login(credentials, SYSTEM_WS);
      root = session.getRootNode();
      root.getNode("TestTreeNode2").remove();
      root.save();
    } catch (PathNotFoundException e) {
    }
    session = null;
    super.tearDown();
  }

}
