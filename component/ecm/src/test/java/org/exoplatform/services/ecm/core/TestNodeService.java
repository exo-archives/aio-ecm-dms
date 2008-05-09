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
package org.exoplatform.services.ecm.core;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.avalon.framework.activity.Startable;
import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 4, 2008  
 */
public class TestNodeService extends BaseECMTestCase {
  private NodeService nodeService_ ; 
  private RepositoryService repoService_ ;
  private JcrItemInput jcrProper_ ;
  public void setUp() throws Exception {
    super.setUp(); 
    
    //look up services
    nodeService_ = (NodeService)container.getComponentInstanceOfType(NodeService.class);
    repoService_ = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
  }
  
  //test add node
  public void testAddNode() throws Exception {    
    //nodeService.addNode(REPO_NAME, COLLABORATION_WS, null, null, null, true, null);    
    System.out.println("\n\n\n\n----This is testAddNode() method: Building....----\n\n\n");
  }
  
  //Test add node1
  public void testAddNode1() throws Exception{
    ManageableRepository repository = repoService_.getRepository(REPO_NAME);
    Session session = repository.getSystemSession(COLLABORATION_WS) ;
    Node rootNode = session.getRootNode() ;
    String nodeType = "nt:unstructured";
    Node parentNode = rootNode.addNode("Test", nodeType) ;
    boolean isNew = true;
    Map<String,JcrItemInput> jcrProperties = new HashMap<String, JcrItemInput>() ;
    JcrItemInput inputProperty = new JcrItemInput();
    inputProperty.setJcrPath("jcrPath");
    inputProperty.setMixinNodeType("mixintype");
    inputProperty.setPrimaryNodeType("nodetype");
    inputProperty.setType("int type");
    inputProperty.setPropertyValue("Object value");
    inputProperty.setValueType("int type");
    jcrProperties.put("String", inputProperty);
    nodeService_.addNode(parentNode,nodeType, jcrProperties, isNew );    
    System.out.println("\n\n\n---A node added----\n\n\n");
  }
  
  //test move node
  public void testMoveNode() throws Exception{
    //call back
    //nodeService_.moveNode(null, null, null, null, null);
  }
  //test copy node
  public void testCopyNode() throws Exception{
    //call back
    //nodeService_.copyNode(null, null, null, null, null);
  }
}
