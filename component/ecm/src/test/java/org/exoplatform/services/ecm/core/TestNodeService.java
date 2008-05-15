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
import javax.jcr.PathNotFoundException;
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
  private JcrItemInput jcrItemInputNode_, jcrItemInputProperties_;
  JcrItemInput jcrItemInputProperties = null;;
  
  public void setUp() throws Exception {
    
    super.setUp();     
    //look up services
    nodeService_ = (NodeService)container.getComponentInstanceOfType(NodeService.class);
    repoService_ = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
    sessionProviderService_ = (SessionProviderService)
    container.getComponentInstanceOfType(SessionProviderService.class);
  }
  
  //test add node
  public void testAddNode() throws Exception {    
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    System.out.println("\n\n\n\n----This is testAddNode() method: Building....----\n\n\n");
  }
  
  //Test add node1
  public void testAddNode1() throws Exception {
    //1.Create Data     
    //2.Test
        //2.1 Test Add nt:file
        //2.1 Test Add exo:article
    //3.Remove data
    
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
    jcrItemInputNode.setValue("TestNodeName") ;
    map.put("/node", jcrItemInputNode) ; 
    
    //properties case
    JcrItemInput jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:title");
    jcrItemInputProperties.setValue("PropertiesCaseTest") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;
    
    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ; 
    NodeType nodeTypeTest = nodetTest.getPrimaryNodeType() ;
    
    System.out.println(
         "\n\n----------Test NodeService - 1 -------" +
         "\n\n   Parent node name: "+ parentNode.getName() +
         "\n\n   node test name: "+nodetTest.getName()+
         "\n\n   node test type: "+nodeTypeTest.getName()+
         "\n\n   node test path: "+nodetTest.getPath()+
         "\n\n   property - path: "+ jcrItemInputProperties.getPath()+
         "\n\n   property - type: "+ jcrItemInputProperties.getType()+
         "\n\n   property - value: "+ jcrItemInputProperties.getValue().toString()+
         "\n\n\n");
    
   //Edit node test
    jcrItemInputProperties.setValue("EditValue");
    map.put("/node/exo:title", jcrItemInputProperties) ;
    nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, false);
    
    System.out.println(
        "\n\n----------Test NodeService - AFTER EDITED -------" +
        "\n\n   Parent node name: "+ parentNode.getName() +
        "\n\n   node test name: "+nodetTest.getName()+
        "\n\n   node test type: "+nodeTypeTest.getName()+
        "\n\n   node test path: "+nodetTest.getPath()+
        "\n\n   property - path: "+ jcrItemInputProperties.getPath()+
        "\n\n   property - type: "+ jcrItemInputProperties.getType()+
        "\n\n   property - value: "+ jcrItemInputProperties.getValue().toString()+
        "\n\n\n");
    
    //Remove datas
    repository = null;
    rootNode = null;
    parentNode = null;
    session = null;
    map = null;
    jcrItemInputNode = null;
 }
  
  //test move node
  public void testMoveNode() throws Exception{
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    Node desNode = createTestNode("NodeByMove", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;
    Node testGetNode = createTestNode("NodeByGive", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;
    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured") ;
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
    Node testMoveNodeCase = nodeService_.moveNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);
    
    System.out.println("\n\n---Source path node-----"+srcNode.getPath()+"-\n\n");
    System.out.println("\n\n---Dest path node-----"+desNode.getPath()+"---\n\n");
    System.out.println("\n\n---The path after moved node--"+testMoveNodeCase.getPath()+"-\n\n");
    
  }
  
  //test copy node
  public void testCopyNode() throws Exception{
    //1. Create data
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    Node desNode = createTestNode("NodeByCopy", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;
    Node testGetNode = createTestNode("NodeByGive", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;
    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured") ;
    srcNode.getSession().save();
    //2. Test
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
    Node testCopyNodeCase = nodeService_.copyNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);
    
    System.out.println("\n\n---Source path node-----"+srcNode.getPath()+"---------\n\n");
    System.out.println("\n\n---Dest path node-----"+desNode.getPath()+"---------\n\n");
    System.out.println("\n\n---The path after copy node-----"+testCopyNodeCase.getPath()+"----\n\n");
    //3.Remove
  }
  
  private Node createTestNode(String nodeName, String repoString, String nodeType,
      JcrItemInput jcrItemInputNode, JcrItemInput jcrItemInputProperties,
      SessionProvider sessionProvider) throws Exception{
    
    ManageableRepository repository = repoService_.getRepository(repoString);
    Session session = sessionProvider.getSession(COLLABORATION_WS, repository) ;
    Node rootNode = session.getRootNode() ;
    
    Node parentNode = null ;
    try {
      parentNode = rootNode.getNode("Test") ;
    } catch(PathNotFoundException pe) {
      parentNode = rootNode.addNode("Test", "nt:unstructured") ;
    }
    Map<String,JcrItemInput> map = new HashMap<String, JcrItemInput>() ;
    
    //node case
    jcrItemInputNode = new JcrItemInput() ;
    jcrItemInputNode.setType(jcrItemInputNode.NODE) ;
    jcrItemInputNode.setMixinNodeType("mix:votable") ;
    jcrItemInputNode.setPath("/node") ;
    jcrItemInputNode.setPrimaryNodeType(nodeType) ;
    jcrItemInputNode.setValue(nodeName) ;
    map.put("/node", jcrItemInputNode) ; 
    
    //properties case
    jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:title");
    jcrItemInputProperties.setValue("PropertiesCaseTest") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;
    
    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ; 
    return nodetTest;
  }
}
