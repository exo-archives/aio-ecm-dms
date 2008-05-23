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
package org.exoplatform.services.ecm.taxonomy;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@yahoo.com
 * May 21, 2008  
 */
public class TestTaxonomyManagerService extends BaseECMTestCase {
  
  /**
   * This test method include all method are relative to category 
   * @throws Exception
   */
  public void testCategory() throws Exception {     
    // Prepare taxonomy list
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node taxonomies = root.addNode("Taxonomies", "nt:unstructured");
    Node testNode = root.addNode("Test", "nt:file");                
    Node content1 = testNode.addNode("jcr:content", "nt:resource");
    content1.setProperty("jcr:lastModified", Calendar.getInstance());
    content1.setProperty("jcr:mimeType", "text/xml");
    content1.setProperty("jcr:data", "");
    
    session.save();
    
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    TaxonomyManagerService taxonomyService = (TaxonomyManagerService) container
        .getComponentInstanceOfType(TaxonomyManagerService.class);
    taxonomyService.addTaxonomy("/Taxonomies", "cms", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Taxonomies", "calendar", REPO_NAME, sessionProvider);    
    taxonomyService.addTaxonomy("/Taxonomies/cms", "new", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Taxonomies/cms", "sport", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Taxonomies/calendar", "birthday", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Taxonomies/calendar", "call", REPO_NAME, sessionProvider);    
    
    //testAddCategory
    taxonomyService.addCategory(testNode, "/Taxonomies/cms/new", sessionProvider);
    taxonomyService.addCategory(testNode, "/Taxonomies/cms/sport", sessionProvider);
    
    List<Node> listNode = taxonomyService.getCategories(testNode, sessionProvider);
    assertEquals(2, listNode.size());
    Node node1 = listNode.get(0);
    Node node2 = listNode.get(1);
    assertEquals("new", node1.getName());
    assertEquals("sport", node2.getName());
    
    //testRemoveTaxonomy
    taxonomyService.removeCategory(testNode, "/Taxonomies/cms/new", sessionProvider);    
    listNode = taxonomyService.getCategories(testNode, sessionProvider);
    assertEquals(1, listNode.size());
    node1 = listNode.get(0);
    assertEquals("sport", node1.getName());
    
    
    //testAddCategory with param replaceAll
    taxonomyService.addCategory(testNode, "/Taxonomies/cms/new", true, sessionProvider);
    listNode = taxonomyService.getCategories(testNode, sessionProvider);
    assertEquals(1, listNode.size());
    node1 = listNode.get(0);
    assertEquals("new", node1.getName());
    
    //remove data
    taxonomies.remove();
    testNode.remove();
    session.save();
  }

  public void testAddTaxonomy() throws Exception {
    // Prepare data
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test = root.addNode("Test", "nt:unstructured");
    session.save();

    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);

    TaxonomyManagerService taxonomyService = (TaxonomyManagerService) container
        .getComponentInstanceOfType(TaxonomyManagerService.class);
    taxonomyService.addTaxonomy("/Test", "abc", REPO_NAME, sessionProvider);
    Node abcNode = test.getNode("abc");
    assertNotNull(abcNode);
    assertEquals("exo:taxonomy", abcNode.getPrimaryNodeType().getName());

    // remove data
    test.remove();
    session.save();
  }

  public void testCopyTaxonomy() throws Exception {
    // Prepare data
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test1 = root.addNode("Test1", "nt:unstructured");
    Node test2 = root.addNode("Test2", "nt:unstructured");
    session.save();

    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);

    TaxonomyManagerService taxonomyService = (TaxonomyManagerService) container
        .getComponentInstanceOfType(TaxonomyManagerService.class);
    taxonomyService.addTaxonomy("/Test1", "taxo1", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Test1", "taxo2", REPO_NAME, sessionProvider);

    Node taxo2 = test1.getNode("taxo2");
    assertNotNull(taxo2);   
    taxonomyService.copyTaxonomy(taxo2.getPath(), "/Test2/taxo2", REPO_NAME, sessionProvider);

    Node taxo22 = test2.getNode("taxo2");
    assertNotNull(taxo22);

    // remove data
    test1.remove();
    test2.remove();
    session.save();
  }

  public void testCutTaxonomy() throws Exception {
    // Prepare data
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test1 = root.addNode("Test3", "nt:unstructured");
    Node test2 = root.addNode("Test4", "nt:unstructured");
    session.save();

    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);

    TaxonomyManagerService taxonomyService = (TaxonomyManagerService) container
        .getComponentInstanceOfType(TaxonomyManagerService.class);
    taxonomyService.addTaxonomy("/Test3", "taxo1", REPO_NAME, sessionProvider);
    taxonomyService.addTaxonomy("/Test3", "taxo2", REPO_NAME, sessionProvider);

    taxonomyService.cutTaxonomy("/Test3/taxo2", "/Test4/taxo2", REPO_NAME, sessionProvider);

    Node taxo22 = test2.getNode("taxo2");
    assertNotNull(taxo22);

    Node taxo2 = null;
    try {
      taxo2 = test1.getNode("taxo2");
      fail("This must throw PathNotFound");
    } catch (PathNotFoundException e) {
    }
    assertNull(taxo2);

    // remove data
    test1.remove();
    test2.remove();
    session.save();
  }

  public void testRemoveTaxonomy() throws Exception {
    // Prepare data
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test3 = root.addNode("Test3", "nt:unstructured");
    session.save();

    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);

    TaxonomyManagerService taxonomyService = (TaxonomyManagerService) container
        .getComponentInstanceOfType(TaxonomyManagerService.class);
    taxonomyService.addTaxonomy("/Test3", "taxo1", REPO_NAME, sessionProvider);
    taxonomyService.removeTaxonomy(test3.getPath() + "/" + "taxo1", REPO_NAME, sessionProvider);
    Node taxo1 = null;
    try {
      taxo1 = test3.getNode("taxo1");
      fail("This must throw PathNotFound");
    } catch (PathNotFoundException e) {
    }
    assertNull(taxo1);

    // remove data
    test3.remove();
    session.save();
  }
}
