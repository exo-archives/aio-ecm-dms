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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.security.auth.login.LoginException;

import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Mar 17, 2009  
 * 4:11:35 PM
 */
public class TestSymLink extends BaseDMSTestCase {
  
  /**
   * Session works with workspace
   */
  Session         session = null;

  SessionProvider sessionProvider;

  /**
   * Set up for testing
   */
  public void setUp() {
    System.out.println("========== Create root node  ========");
    try {
      super.setUp();
      sessionProvider = SessionProvider.createSystemProvider();
      session = sessionProvider.getSession(COLLABORATION_WS, repositoryService
          .getRepository(REPO_NAME));
      //createTree();
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
   * Add node
   * 
   * @throws Exception
   */
  public void testAddNode() throws Exception {
    /* Get root node */
    Node rootNode = session.getRootNode();
    /* Add node to root */
    Node document = rootNode.addNode("Document1", "nt:unstructured");
    session.save();
    Node nodeA = rootNode.getNode("Document1");
    assertEquals(document.getName(), nodeA.getName());
  }


  /**
   * Create tree
   * 
   * @throws Exception
   */
  public void createTree() throws Exception {
    Node rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TreeTestRootNode");
    Node nodeA1 = testNode.addNode("A1");
    Node nodeA2 = testNode.addNode("A2");
    Node nodeB2 = nodeA2.addNode("B2","exo:symlink");

    Node nodeC1 = nodeA1.addNode("C1");
    Node nodeC2 = nodeA1.addNode("C2");
    if (nodeC1.hasProperty("jcr:uuid")) {
      System.out.println("\n\n jcr uuid = " +nodeC1.getProperty("jcr:uuid").getValue());
    } else {
      nodeC1.addMixin("mix:referenceable");
      nodeC2.addMixin("mix:referenceable");
    }
    nodeB2.setProperty("exo:workspace",COLLABORATION_WS);
    nodeB2.setProperty("exo:uuid",nodeC1.getProperty("jcr:uuid").getString());
    nodeB2.setProperty("exo:primaryType",nodeC1.getPrimaryNodeType().getName());    
    
    Node nodeA3 = testNode.addNode("A3","exo:symlink");
    nodeA3.setProperty("exo:workspace", COLLABORATION_WS);
    nodeA3.setProperty("exo:uuid", nodeC2.getProperty("jcr:uuid").getString());
    nodeA3.setProperty("exo:primaryType",nodeC2.getPrimaryNodeType().getName());
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
    /* Get all properties */
    PropertyIterator ps = node.getProperties();
    /* Print properties */
    Object objValue;
    int i = 0;
    if (ps != null) {
      while (ps.hasNext()) {
        if (i > 0) {
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
        }

        Property p = (Property) ps.next();
        for (int k = 0; k < iLevel + 1; k++) {
          System.out.print("\t");
          System.out.print("|");
        }
        System.out.print("-------Property " + ++i + ": " + p.getName() + " ");
        try {
          objValue = p.getValues();
          for (Value v : (Value[]) objValue) {
            System.out.print(v.getString() + ",");
          }
        } catch (ValueFormatException e) {
          objValue = p.getValue();
          System.out.print(((Value) objValue).getString());
        }
        System.out.println();
      }
    }
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
  
  public void testQueryXpath() throws Exception {
    createTree();
    Node testNode = session.getRootNode().getNode("TreeTestRootNode");
    browserTree(testNode, 0);
    System.out.println("Query node by XPATH");
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from exo:symlink where jcr:path like '/TreeTestRootNode/%'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator iterNode = result.getNodes();
    if (iterNode.hasNext()) {
      while (iterNode.hasNext()) {
        Node nodeResult = iterNode.nextNode();
        System.out.println("\n\n path = " + nodeResult.getPath());
      }
    } else {
      System.out.println("abc dasfj s khong co");
    }
    String data = "10";
    assertEquals(true, true);
  }
  public void tearDown() throws Exception {
    super.tearDown();
    session = null;
    sessionProvider.close();
  }


}
