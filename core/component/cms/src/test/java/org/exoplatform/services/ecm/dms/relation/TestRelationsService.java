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
package org.exoplatform.services.ecm.dms.relation;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestRelationsService extends BaseDMSTestCase {
    
  private RelationsService relationsService;
  
  public void setUp() throws Exception {
    super.setUp();
    relationsService = (RelationsService)container.getComponentInstanceOfType(RelationsService.class);
    
    Session sessionCollaboration = repository.login(credentials, COLLABORATION_WS);
    Node root = sessionCollaboration.getRootNode();
    Node aaa = root.addNode("AAA");
    Node bbb = root.addNode("BBB");
    Node ccc = root.addNode("CCC");
    sessionCollaboration.save();
    
    relationsService.addRelation(aaa, bbb.getPath(), COLLABORATION_WS, REPO_NAME);
    relationsService.addRelation(aaa, ccc.getPath(), COLLABORATION_WS, REPO_NAME);   
  }
  
  public void testHasRelations() throws Exception {
    Session sessionCollaboration = repository.login(credentials, COLLABORATION_WS);    
    Node root = sessionCollaboration.getRootNode();
    Node aaa = root.getNode("AAA");
    Node ccc = root.getNode("CCC");
    
    assertTrue(relationsService.hasRelations(aaa));
    assertFalse(relationsService.hasRelations(ccc));
  }
  
  public void testGetRelations() throws Exception {
    Session sessionCollaboration = repository.login(credentials, COLLABORATION_WS);
    Node root = sessionCollaboration.getRootNode();
    Node aaa = root.getNode("AAA");
    
    List<Node> listRelation = relationsService.getRelations(aaa, REPO_NAME, SessionProviderFactory.createSessionProvider());    
    assertEquals(2, listRelation.size());
  }
  
  public void testRemoveRelation() throws Exception {
    Session sessionCollaboration = repository.login(credentials, COLLABORATION_WS);
    Node root = sessionCollaboration.getRootNode();
    Node aaa = root.getNode("AAA");
    
    List<Node> beforeRemove = relationsService.getRelations(aaa, REPO_NAME, SessionProviderFactory.createSessionProvider());    
    assertEquals(2, beforeRemove.size());
    
    relationsService.removeRelation(aaa, "/CCC", REPO_NAME);     
    
    List<Node> afterRemove = relationsService.getRelations(aaa, REPO_NAME, SessionProviderFactory.createSessionProvider());    
    assertEquals(1, afterRemove.size());
  }
  
  public void testAddRelation() throws Exception {
    Session sessionCollaboration = repository.login(credentials, COLLABORATION_WS);
    Node root = sessionCollaboration.getRootNode();
    Node aaa = root.getNode("AAA");
    
    Node ddd = root.addNode("DDD");
    sessionCollaboration.save();
    
    List<Node> beforeAdd = relationsService.getRelations(aaa, REPO_NAME, SessionProviderFactory.createSessionProvider());    
    assertEquals(1, beforeAdd.size());
    
    relationsService.addRelation(aaa, ddd.getPath(), COLLABORATION_WS, REPO_NAME);
    
    List<Node> afterAdd = relationsService.getRelations(aaa, REPO_NAME, SessionProviderFactory.createSessionProvider());    
    assertEquals(2, afterAdd.size());
  }
}
