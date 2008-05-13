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

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 4, 2008  
 */
public class TestNodeService extends BaseECMTestCase {
  
  private NodeService nodeService_ ; 
  private RepositoryService repoService_ ;
  private SessionProviderService sessionProviderService_ ;
  private JcrItemInput jcrProper_ ;
  
  public void setUp() throws Exception {
    
    super.setUp();     
    //look up services
    nodeService_ = (NodeService)container.getComponentInstanceOfType(NodeService.class);
    repoService_ = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
    sessionProviderService_ = (SessionProviderService)container.getComponentInstanceOfType(SessionProviderService.class);
  }
  
  //test add node
  public void testAddNode() throws Exception {    
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    System.out.println("\n\n\n\n----This is testAddNode() method: Building....----\n\n\n");
  }
  
  //Test add node1
  public void testAddNode1() throws Exception{
    
    ManageableRepository repository = repoService_.getRepository(REPO_NAME);
    Session session = repository.getSystemSession(COLLABORATION_WS) ;
    Node rootNode = session.getRootNode() ;
    Node parentNode = rootNode.addNode("Test", "nt:unstructured") ;
    Map<String,JcrItemInput> map = new HashMap<String, JcrItemInput>() ;
    
    //node case
    JcrItemInput jcrItemInputNode = new JcrItemInput() ;
    jcrItemInputNode.setType(jcrItemInputNode.NODE) ;
    jcrItemInputNode.setMixinNodeType("mix:votable") ;
    jcrItemInputNode.setPath("/node") ;
    jcrItemInputNode.setPrimaryNodeType("nt:unstructured") ;
    jcrItemInputNode.setValue("NodeTest") ;
    map.put("/node", jcrItemInputNode) ; 
    
    //properties case
    JcrItemInput jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:title");
    jcrItemInputProperties.setValue("PropertiesCase") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;
    
    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ;  
    NodeType nodeTypeTest = nodetTest.getPrimaryNodeType() ;
    
    System.out.println(
         "\n\n----------Test NodeService - 1 -------" +
         "\n\n   Parent node name: "+ parentNode.getName() +
         "\n\n   node test name: "+nodetTest.getName()+
         "\n\n   node test type: "+nodeTypeTest.getName()+
         "\n\n   node test path: "+nodetTest.getPath()+
         "\n\n\n");
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
