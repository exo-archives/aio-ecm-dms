/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.services.ecm.dms.cms.taxonomy;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyNodeAlreadyExistsException;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Jun 14, 2009  
 */
public class TaxonomyServiceTest extends BaseDMSTestCase {

  private TaxonomyService      taxonomyService;

  private String                       definitionPath;

  private String                       storagePath;

  private LinkManager                  linkManage;

  private Session                      dmsSesssion;
  
  private NodeHierarchyCreator nodeHierarchyCreator;
  
  /**
   * *  @see {@link # testInit()}
   */
  public void setUp() throws Exception {
    super.setUp();
    taxonomyService = (TaxonomyService) container.getComponentInstanceOfType(TaxonomyService.class);
    dmsSesssion = repository.login(credentials, DMSSYSTEM_WS);
    nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    linkManage = (LinkManager)container.getComponentInstanceOfType(LinkManager.class);
    definitionPath =  nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_DEFINITION_PATH);
    storagePath =  nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
  }
  

  /**
   *  Test method TaxonomyService.addTaxonomyPlugin()
   *  @see {@link # testInit()}
   */
  public void testAddTaxonomyPlugin() throws Exception {
  }

  /**
   *  Test method TaxonomyService.init()
   *  Create system taxonomy tree in dms-system
   *  @see {@link # testInit()}
   */
  public void testInit() throws Exception {
    Node systemTreeDef = (Node) dmsSesssion.getItem(definitionPath + "/System");
    Node systemTreeStorage = (Node) dmsSesssion.getItem(storagePath + "/System");
    assertNotNull(systemTreeDef);
    assertNotNull(systemTreeStorage);
    assertEquals(systemTreeStorage, linkManage.getTarget(systemTreeDef, true));
  }
  
  // Test method TaxonomyService.getTaxonomyTree(String repository, String taxonomyName, boolean system)
  public void testGetTaxonomyTree1() throws Exception {
    Node systemTree = taxonomyService.getTaxonomyTree(REPO_NAME, "System");
    assertNotNull(systemTree);
  }
  
  // Test method TaxonomyService.addTaxonomyTree(Node taxonomyTree)
  public void testAddTaxonomyTree1() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Doc");
    Node docTree = (Node)session.getItem("/MyDocuments/Doc");
    taxonomyService.addTaxonomyTree(docTree);
    assertTrue(dmsSesssion.itemExists(definitionPath + "/Doc"));
    Node definitionDocTree = (Node)dmsSesssion.getItem(definitionPath + "/Doc");
    assertEquals(docTree, linkManage.getTarget(definitionDocTree));
  }

  // Test method TaxonomyService.addTaxonomyTree(Node taxonomyTree) with one tree has already existed
  public void testAddTaxonomyTree2() throws RepositoryException, TaxonomyNodeAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Doc");
    Node docTree = (Node)session.getItem("/MyDocuments/Doc");
    try {
      taxonomyService.addTaxonomyTree(docTree);
      taxonomyService.addTaxonomyTree(docTree);
    } catch(TaxonomyAlreadyExistsException e) {
      
    }
  }
  
  // Test method TaxonomyService.getTaxonomyTree(String repository, String taxonomyName)
  public void testGetTaxonomyTree2() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Music");
    Node musicTree = (Node)session.getItem("/MyDocuments/Music");
    taxonomyService.addTaxonomyTree(musicTree);
    assertTrue(dmsSesssion.itemExists(definitionPath + "/Music"));
    Node musicTreeDefinition = (Node)dmsSesssion.getItem(definitionPath + "/Music");
    assertEquals(musicTree, linkManage.getTarget(musicTreeDefinition));
  }

  // Test method TaxonomyService.getAllTaxonomyTrees(String repository)
  public void testGetAllTaxonomyTrees2() throws Exception {
    assertEquals(1, taxonomyService.getAllTaxonomyTrees(REPO_NAME).size());
  }

  // Test method TaxonomyService.getTaxonomyTree(String repository, String taxonomyName)
  public void testRemoveTaxonomyTree() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Miscellaneous");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Miscellaneous", "Shoes");
    Node miscellaneous = (Node)session.getItem("/MyDocuments/Miscellaneous");
    taxonomyService.addTaxonomyTree(miscellaneous);
    assertTrue(dmsSesssion.itemExists(definitionPath + "/Miscellaneous"));
    taxonomyService.removeTaxonomyTree("Miscellaneous");
    assertFalse(dmsSesssion.itemExists(definitionPath + "/Miscellaneous"));
  }
  
  // Test method TaxonomyService.getAllTaxonomyTrees(String repository, boolean system)
  public void testGetAllTaxonomyTrees1() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Champion Leage");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Europa");
    Node championLeague = (Node)session.getItem("/MyDocuments/Europa");
    Node europa = (Node)session.getItem("/MyDocuments/Champion Leage");
    int totalTree1 = taxonomyService.getAllTaxonomyTrees(REPO_NAME).size();
    taxonomyService.addTaxonomyTree(championLeague);
    taxonomyService.addTaxonomyTree(europa);
    int totalTree2 = taxonomyService.getAllTaxonomyTrees(REPO_NAME).size();
    assertEquals(2, totalTree2 - totalTree1);
  }

  // Test method TaxonomyService.hasTaxonomyTree(String repository, String taxonomyName)
  public void testHasTaxonomyTree() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Primera Liga");
    taxonomyService.addTaxonomyTree((Node)session.getItem("/MyDocuments/Primera Liga"));
    assertTrue(taxonomyService.hasTaxonomyTree(REPO_NAME, "System"));
    assertTrue(taxonomyService.hasTaxonomyTree(REPO_NAME, "Primera Liga"));
  }
  
  // Test method TaxonomyService.addTaxonomyNode()
  public void testAddTaxonomyNode1() throws TaxonomyNodeAlreadyExistsException, RepositoryException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Sport");
    Node taxonomyNode = (Node)session.getItem("/MyDocuments/Sport");
    assertTrue(taxonomyNode.isNodeType("exo:taxonomy"));
  }

  // Test method TaxonomyService.addTaxonomyNode() throws TaxonomyNodeAlreadyExistsException when Already exist node
  public void testAddTaxonomyNode2() throws RepositoryException {
    try {
      session.getRootNode().addNode("MyDocuments");
      session.save();
      taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Sport");
      taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Sport");
    } catch (TaxonomyNodeAlreadyExistsException e) {
    }
  }

  // Test method TaxonomyService.removeTaxonomyNode()
  public void testRemoveTaxonomyNode() throws RepositoryException, TaxonomyNodeAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Tennis");
    taxonomyService.removeTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Tennis");
    assertFalse(session.itemExists("/MyDocuments/Tennis"));
  }
  
  // Test method TaxonomyService.moveTaxonomyNode()
  public void testMoveTaxonomyNode() throws RepositoryException, TaxonomyNodeAlreadyExistsException  {
    session.getRootNode().addNode("MyDocuments");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Serie");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Budesliga");
    taxonomyService.moveTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "/Serie", "cut");
    taxonomyService.moveTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Budesliga", "/Budesliga", "copy");
    assertFalse(session.itemExists("/MyDocuments/Serie"));
    assertTrue(session.itemExists("/Serie"));
    assertTrue(session.itemExists("/Budesliga"));
    assertTrue(session.itemExists("/MyDocuments/Budesliga"));
  }
  
  // Test method TaxonomyService.addCategory()
  public void testAddCategory() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Serie");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "A");
    Node rootTree = (Node)session.getItem("/MyDocuments/Serie");
    session.save();
    taxonomyService.addTaxonomyTree(rootTree);
    taxonomyService.addCategory(article, "Serie", "A");
    Node link = (Node)session.getItem("/MyDocuments/Serie/A/Article");
    assertTrue(link.isNodeType("exo:taxonomyLink"));
    assertEquals(article, linkManage.getTarget(link));
  }
  
  // Test method TaxonomyService.addCategories()
  public void testAddCategories() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Serie");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "A");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "B");
    Node rootTree = (Node)session.getItem("/MyDocuments/Serie");
    taxonomyService.addTaxonomyTree(rootTree);
    taxonomyService.addCategories(article, "Serie", new String[] {"A", "B"});
    Node link1 = (Node)session.getItem("/MyDocuments/Serie/A/Article");
    Node link2 = (Node)session.getItem("/MyDocuments/Serie/B/Article");
    assertTrue(link1.isNodeType("exo:taxonomyLink"));
    assertEquals(article, linkManage.getTarget(link1));
    assertTrue(link2.isNodeType("exo:taxonomyLink"));
    assertEquals(article, linkManage.getTarget(link2));
  }
  
  // Test method TaxonomyService.hasCategories()
  public void testHasCategories() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Budesliga");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Serie");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "A");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Serie", "B");
    Node rootTree1 = (Node)session.getItem("/MyDocuments/Serie");
    Node rootTree2 = (Node)session.getItem("/MyDocuments/Budesliga");
    taxonomyService.addTaxonomyTree(rootTree1);
    taxonomyService.addTaxonomyTree(rootTree2);
    taxonomyService.addCategories(article, "Serie", new String[] {"A", "B"});
    assertTrue(taxonomyService.hasCategories(article, "Serie"));
    assertFalse(taxonomyService.hasCategories(article, "Budesliga"));
  }
  
  // Test method TaxonomyService.getCategories()
  public void testGetCategories() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Stories");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Stories", "Homorous");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Stories", "Fairy");
    Node rootTree = (Node)session.getItem("/MyDocuments/Stories");
    taxonomyService.addTaxonomyTree(rootTree);
    taxonomyService.addCategories(article, "Stories", new String[] {"Homorous", "Fairy"});
    List<Node> lstNode = taxonomyService.getCategories(article, "Stories");
    Node taxoLink1 = (Node)session.getItem("/MyDocuments/Stories/Homorous");
    Node taxoLink2 = (Node)session.getItem("/MyDocuments/Stories/Fairy");
    assertEquals(2, lstNode.size());
    assertTrue(lstNode.contains(taxoLink1));
    assertTrue(lstNode.contains(taxoLink2));
  }
  
  // Test method TaxonomyService.getAllCategories()
  public void testGetAllCategories() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Culture");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "News");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/News", "Politics");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Culture", "Foods");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Culture", "Art");
    Node rootTree1 = (Node)session.getItem("/MyDocuments/Culture");
    Node rootTree2 = (Node)session.getItem("/MyDocuments/News");
    taxonomyService.addTaxonomyTree(rootTree1);
    taxonomyService.addTaxonomyTree(rootTree2);
    taxonomyService.addCategories(article, "Culture", new String[] {"Foods", "Art"});
    taxonomyService.addCategory(article, "News", "Politics");
    List<Node> lstNode = taxonomyService.getAllCategories(article);
    Node taxoLink1 = (Node)session.getItem("/MyDocuments/Culture/Foods");
    Node taxoLink2 = (Node)session.getItem("/MyDocuments/Culture/Art");
    Node taxoLink3 = (Node)session.getItem("/MyDocuments/News/Politics");
    assertEquals(3, lstNode.size());
    assertTrue(lstNode.contains(taxoLink1));
    assertTrue(lstNode.contains(taxoLink2));
    assertTrue(lstNode.contains(taxoLink3));
  }
  
  // Test method TaxonomyService.removeCategory()
  public void testRemoveCategory() throws RepositoryException, TaxonomyNodeAlreadyExistsException, TaxonomyAlreadyExistsException {
    session.getRootNode().addNode("MyDocuments");
    Node article = session.getRootNode().addNode("Article");
    session.save();
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "Education");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments", "News");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/Education", "Language");
    taxonomyService.addTaxonomyNode(REPO_NAME, COLLABORATION_WS, "/MyDocuments/News", "Weather");
    Node rootTree1 = (Node)session.getItem("/MyDocuments/Education");
    taxonomyService.addTaxonomyTree(rootTree1);
    Node rootTree2 = (Node)session.getItem("/MyDocuments/News");
    taxonomyService.addTaxonomyTree(rootTree2);
    taxonomyService.addCategory(article, "Education", "Language");
    taxonomyService.addCategory(article, "News", "Weather");
    List<Node> lstNode = taxonomyService.getAllCategories(article);
    assertEquals(2, lstNode.size());
    taxonomyService.removeCategory(article, "Education", "Language");
    lstNode = taxonomyService.getAllCategories(article);
    assertEquals(1, lstNode.size());
    taxonomyService.removeCategory(article, "News", "Weather");
    lstNode = taxonomyService.getAllCategories(article);
    assertEquals(0, lstNode.size());
  }

  public void tearDown() throws Exception {
    List<Node> lstNode = taxonomyService.getAllTaxonomyTrees(REPO_NAME);
    for(Node tree : lstNode) {
      if (!tree.getName().equals("System"))
        taxonomyService.removeTaxonomyTree(tree.getName());
    }
    if (session.itemExists("/MyDocuments")) {
      session.getItem("/MyDocuments").remove();
      session.save();
    }
    super.tearDown();
  }
}



