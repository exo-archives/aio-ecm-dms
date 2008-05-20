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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hao Le Minh
 *          hao.le@exoplatform.com
 * May 4, 2008  
 */
public class TestNodeService extends BaseECMTestCase {

  private NodeService nodeService_ ;  
  private SessionProviderService sessionProviderService_ ;

  public void setUp() throws Exception {

    super.setUp();     
    //look up services
    nodeService_ = (NodeService)container.getComponentInstanceOfType(NodeService.class); 
    sessionProviderService_ = (SessionProviderService)
    container.getComponentInstanceOfType(SessionProviderService.class);
  }
  //test add node
  public void testAddNode() throws Exception {    
    System.out.println("\n\n\n\n----This is testAddNode() method: Building....----\n\n\n");
  }

  //Test add node1
  public void testAddNode1() throws Exception {
    //1.Create Data     
    //2.Test
    //2.1 Test Add nt:file
    //2.1 Test Add exo:article
    //3.Remove data
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    System.out.println("\n\n\n-----------NT:UNSTRUCTURED and EXO:TITLE------\n\n\n");
    ManageableRepository repository = repositoryService.getRepository(REPO_NAME);
    Session session = sessionProvider.getSession(COLLABORATION_WS, repository) ;
    Node rootNode = session.getRootNode() ;
    Node parentNode = rootNode.addNode("Test", "nt:unstructured") ;

    //--------check---------
    assertNotNull((Node)session.getItem("/Test"));
    assertEquals("Test", parentNode.getName());
    assertEquals("nt:unstructured", parentNode.getPrimaryNodeType().getName());
    //----------------------

    Map<String,JcrItemInput> map = new HashMap<String, JcrItemInput>() ;

    //node case
    JcrItemInput jcrItemInputNode = new JcrItemInput() ;
    jcrItemInputNode.setType(jcrItemInputNode.NODE) ;
    jcrItemInputNode.setPrimaryNodeType("nt:unstructured") ;
    jcrItemInputNode.setMixinNodeType("mix:votable") ; 
    jcrItemInputNode.setPath("/node") ;
    jcrItemInputNode.setValue("TestNodeName") ;

    //------check--------
    assertNotNull(jcrItemInputNode);
    assertEquals("TestNodeName", jcrItemInputNode.getValue().toString());
    assertEquals("mix:votable", jcrItemInputNode.getMixinNodeTypes()[0].toString());
    assertEquals("/node", jcrItemInputNode.getPath());
    //------------------------

    map.put("/node", jcrItemInputNode) ; 

    //properties case
    JcrItemInput jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:votable"); 
    jcrItemInputProperties.setValue("PropertiesCaseTest") ;
    map.put("/node/exo:votable", jcrItemInputProperties) ;

    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ;
    NodeType nodeTypeTest = nodetTest.getPrimaryNodeType() ;

    //----------------check-------------------------
    assertNotNull(nodetTest);
    assertNotNull(nodeTypeTest);
    assertEquals("nt:unstructured", nodetTest.getPrimaryNodeType().getName());
    //--------------------------

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

    assertNotNull(nodetTest);
    assertEquals("EditValue", jcrItemInputProperties.getValue().toString());

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
    sessionProvider.close() ;
  }
  /**
   * @throws Exception
   */
  public void testAddNodeNTFileArticle() throws Exception{

    ManageableRepository repository = repositoryService.getRepository(REPO_NAME) ;
    Session session = repository.getSystemSession(COLLABORATION_WS) ;
    Node rootNode = session.getRootNode() ;

    //------check-------
    assertNotNull(rootNode);
    assertEquals("/", rootNode.getPath());
    //--------------------

    Node parentNode = rootNode.addNode("Test", "nt:unstructured") ;

    //------check-------
    assertNotNull(parentNode);
    assertEquals("Test", parentNode.getName());
    //---------------------------------
    System.out.println("\n\n\n-----------NT:FILE------\n\n\n");
    InputStream inputStream = null;
    Date date = null;
    Map<String , JcrItemInput> mapFile = prepareFileProperties("myFile", "MimeType", inputStream, date);
    Node testNtFile = nodeService_.addNode(parentNode, "nt:file", mapFile, true) ;
    //--------check------
    assertNotNull(testNtFile);
    assertEquals("/Test", parentNode.getPath());
    assertEquals("nt:file", testNtFile.getPrimaryNodeType().getName());
    //-----------------------
    System.out.println("\n\n\n-----------EXO:ARTICLE------\n\n\n");
    Map<String , JcrItemInput> mapArticle = prepareArticleProperties();
    Node testArticle = nodeService_.addNode(parentNode, "exo:article", mapArticle, true);
    //--------check------
    assertNotNull(testArticle);
    assertEquals("exo:article", testArticle.getPrimaryNodeType().getName());
    //-----------------------
  } 

  //test copy node
  public void testCopyNode() throws Exception{

    System.out.println("\n\n\n-----------TEST COPY NODE-------\n\n\n");

    //1. Create data
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    JcrItemInput jcrItemInputNode_ = null, jcrItemInputProperties_ = null;  
    Node desNode = createTestNode("NodeByCopy", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    //-----------------check-------
    assertNotNull(desNode);
    assertEquals("NodeByCopy", desNode.getName());
    assertEquals("nt:unstructured", desNode.getPrimaryNodeType().getName());
    //-----------------------------

    Node testGetNode = createTestNode("NodeByGiveCpy", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    //-----------------check-------
    assertNotNull(testGetNode);
    assertEquals("NodeByGiveCpy", testGetNode.getName());
    assertEquals("nt:unstructured", testGetNode.getPrimaryNodeType().getName());
    //-----------------------------

    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured") ;

    //------------check-----------------
    assertNotNull(srcNode);
    assertEquals("ChildNode", srcNode.getName());
    assertEquals("nt:unstructured", srcNode.getPrimaryNodeType().getName());
    //-----------------------------

    srcNode.getSession().save();
    //2. Test
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
    Node testCopyNodeCase = nodeService_.copyNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);

    //---------check----
    assertNotNull(testCopyNodeCase);
    assertEquals("collaboration", COLLABORATION_WS);
    //---------------

    System.out.println("\n\n---Source path node-----"+srcNode.getPath()+"---------\n\n");
    System.out.println("\n\n---Dest path node-----"+desNode.getPath()+"---------\n\n");
    System.out.println("\n\n---The path after copyed node-----"+testCopyNodeCase.getPath()+"----\n\n");
    //3.Remove
    desNode = null;
  }  
  //test move node
  public void testMoveNode() throws Exception{

    System.out.println("\n\n\n-----------TEST MOVE NODE-------\n\n\n");

    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    JcrItemInput jcrItemInputNode_ = null, jcrItemInputProperties_ = null;    
    Node desNode = createTestNode("NodeByMove", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    //----------check-------------
    assertNotNull(desNode);
    assertEquals("NodeByMove", desNode.getName());
    assertEquals("repository", REPO_NAME);
    assertEquals("nt:unstructured", desNode.getPrimaryNodeType().getName());
    //------------------------------

    Node testGetNode = createTestNode("NodeByGive", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    //---------check------
    assertNotNull(testGetNode);
    assertEquals("NodeByGive", testGetNode.getName());
    assertEquals("nt:unstructured", testGetNode.getPrimaryNodeType().getName());
    //--------------------------

    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured") ;

    //------------check----------
    assertNotNull(srcNode);
    assertEquals("ChildNode", srcNode.getName());
    assertEquals("nt:unstructured", srcNode.getPrimaryNodeType().getName());
    //------------------------------------

    srcNode.getSession().save();
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
    Node testMoveNodeCase = nodeService_.moveNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);

    //-------check---------
    assertNotNull(testMoveNodeCase);
    //----------------------------------
    System.out.println("\n\n---Source path node-----"+srcNode.getPath()+"-\n\n");
    System.out.println("\n\n---Dest path node-----"+desNode.getPath()+"---\n\n");
    System.out.println("\n\n---The path after moved node--"+testMoveNodeCase.getPath()+"-\n\n");
    //remove data
    desNode = null;
  }

  private Node createTestNode(String nodeName, String repoString, String nodeType,
      JcrItemInput jcrItemInputNode, JcrItemInput jcrItemInputProperties,
      SessionProvider sessionProvider) throws Exception{

    ManageableRepository repository = repositoryService.getRepository(repoString);
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

    //----------check-----------
    assertEquals(jcrItemInputNode.NODE, jcrItemInputNode.getType());
    assertEquals("mix:votable" , jcrItemInputNode.getMixinNodeTypes()[0].toString());
    assertEquals("/node", jcrItemInputNode.getPath());
    //---------------------------

    //properties case
    jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:title");
    jcrItemInputProperties.setValue("PropertiesCaseTest") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;

    //----------check-----------
    assertEquals(jcrItemInputProperties.PROPERTY, jcrItemInputProperties.getType());
    assertEquals("/node/exo:title", jcrItemInputProperties.getPath());
    //---------------------------

    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ; 

    //------check--------
    assertNotNull(nodetTest);
    assertNotNull(map);
    assertEquals("nt:unstructured", nodetTest.getPrimaryNodeType().getName());
    //-----------------------

    rootNode = null;
    return nodetTest;
  }

  private Map<String, JcrItemInput> prepareArticleProperties() {
    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();
    JcrItemInput itemInput = getNodeInput("exo:article", "mix:votable", "MyArticle");
    map.put("/node", itemInput);
    
    JcrItemInput itemInputProp = getPropertyInput("/node/exo:text", "MyArticle");
    map.put("/node/exo:text", itemInputProp);
    
    return map ;
  }

  private Map<String, JcrItemInput> prepareFileProperties(String fileName,String mimeType,InputStream fileData,Date dateLst) throws Exception{

    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();

    //Node - nt:file
    JcrItemInput itemInput = getNodeInput("nt:file", "mix:votable", fileName);
    map.put("/node", itemInput);

    //Node - jcr:content
    JcrItemInput itemInputJcrContent = getNodeInput("nt:resource", "", "jcrContent");
    map.put("/node/jcr:content", itemInputJcrContent);

    //properties - data
    InputStream inputStream = new ByteArrayInputStream("my data".getBytes());
    JcrItemInput itemInputData = getPropertyInput("/node/jcr:content/jcr:data", inputStream);
    map.put("/node/jcr:content/jcr:data", itemInputData);

    //properties - mime type
    //mimetype can be: text/html,text/plain....
    JcrItemInput itemInputMimeType = getPropertyInput("/node/jcr:content/jcr:mimeType", mimeType);
    map.put("/node/jcr:content/jcr:mimeType", itemInputMimeType);

    //properties - last modified
    dateLst = new Date();
    JcrItemInput itemInputLastMdf = getPropertyInput("/node/jcr:content/jcr:lastModified", dateLst.getTime());
    map.put("/node/jcr:content/jcr:lastModified", itemInputLastMdf);
        
    return map;
  }

  private JcrItemInput getNodeInput(String primaryType, String mixinTypes, String nodeName) {
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setType(JcrItemInput.NODE) ;    
    jcrItemInput.setPath("/node");
    jcrItemInput.setPrimaryNodeType(primaryType);
    if(mixinTypes!= null && mixinTypes.length()!= 0  ) {
      jcrItemInput.setMixinNodeType(mixinTypes);       
    }    
    jcrItemInput.setValue(nodeName);
    return jcrItemInput ;
  }  

  private JcrItemInput getPropertyInput(String propertyPath,Object propertyValue) {
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setPath(propertyPath);    
    jcrItemInput.setValue(propertyValue);
    return jcrItemInput ;
  }
}
