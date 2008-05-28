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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

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
@SuppressWarnings("unused")
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

  public void testAddNode() throws Exception {
    //1.Create Data     
    //2.Test
    //2.1 Test Add nt:file (in TestNTFileAndArticle method)
    //2.1 Test Add exo:article (in TestNTFileAndArticle method)
    //3.Remove data
    
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    ManageableRepository repository = repositoryService.getRepository(REPO_NAME);
    Session session = sessionProvider.getSession(COLLABORATION_WS, repository) ;
    Node rootNode = session.getRootNode() ;
    
    //- Create a node named Test from root
    //- Before add Test node: Has'nt node child, root node path is '/'
    //- After add Test node: Has 1 node child named Test, the path Test node id '/Test', Primary type
    //is nt:unstructurred
    
    assertEquals(rootNode.getNodes().getSize(), 0);
    assertEquals(rootNode.getPath(), "/");
    
    Node parentNode = rootNode.addNode("Test", "nt:unstructured") ;
    session.save();
    
    assertEquals("Test", parentNode.getName());
    assertEquals(rootNode.getNodes().getSize(), 1);
    assertEquals(parentNode.getPath(), "/Test");
    assertEquals("nt:unstructured", parentNode.getPrimaryNodeType().getName());
    assertEquals(parentNode.getNodes().getSize(), 0);

    //- Create a node named NodeName
    //- The path nodeTest is '/Test/NodeName'
    //    - If this node existing, index will be NodeName[2]
    //    - If this node do'nt create, new NodeName create
    //- Properties: PrimaryType is nt:unstructured, mixin type is mix:votable
    //- Before add NodeName / NodeName[2], the child nodes in Test node is empty
    //- After add NodeName / NodeName[2], the child nodes in Test node is 1/2
    //- If parent node(Test Node) has remove, the child nodes in root node is empty
    //- If child nodes in Test node has remove, the child nodes in Test node is empty
    //- If NodeName/NodeName[2] has remove, the child node in Test is 1
    
    Map<String,JcrItemInput> map = new HashMap<String, JcrItemInput>() ;

    //node case
    JcrItemInput jcrItemInputNode = getNodeInput("nt:unstructured", "mix:votable", "NodeName","/node") ;
     
    assertEquals("NodeName", jcrItemInputNode.getValue().toString());
    assertEquals("mix:votable", jcrItemInputNode.getMixinNodeTypes()[0].toString());
    assertEquals("/node", jcrItemInputNode.getPath());

    map.put("/node", jcrItemInputNode) ;

    //properties case
    JcrItemInput jcrItemInputProperties = getPropertyInput("/node/exo:votable", "PropValue") ;
    
    assertNotNull(jcrItemInputProperties);
    assertEquals("/node/exo:votable", jcrItemInputProperties.getPath());
    assertEquals("PropValue", jcrItemInputProperties.getValue());
    
    map.put("/node/exo:votable", jcrItemInputProperties) ;

    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ;
    session.save();
    Node nodeTest2 = nodeService_.addNode(parentNode, "nt:unstructured", map, true);
    session.save();
   
    assertNotNull(nodetTest);
    assertEquals(parentNode.getNodes().getSize(), 2);
    assertEquals(nodetTest.getName(), "NodeName");
    assertEquals(nodetTest.getPath(), "/Test/NodeName");
    assertEquals(nodetTest.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(nodetTest.getProperties().getSize(), 5);
    assertEquals(jcrItemInputNode.getMixinNodeTypes()[0].toString(), "mix:votable");
    
    //Edit node test
    jcrItemInputProperties.setValue("EditValue");
    map.put("/node/exo:title", jcrItemInputProperties) ;
    nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, false);
    session.save();
   
    assertNotNull(nodetTest);
    assertEquals("EditValue", jcrItemInputProperties.getValue().toString());
     
    nodeTest2.remove();
    session.save();
    
    assertEquals(parentNode.getNodes().getSize(), 1);
    
    parentNode.remove();
    session.save();
    
    assertEquals(rootNode.getNodes().getSize(), 0);
    
    sessionProvider.close() ;
  }
  
  public void testAddNodeNTFileAndArticle() throws Exception{

    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    ManageableRepository repository = repositoryService.getRepository(REPO_NAME);
    Session session = sessionProvider.getSession(COLLABORATION_WS, repository) ;
    Node rootNode = session.getRootNode() ;

    //1. Create a node from root node named Test
    //- Before add Test node, the child nodes in root node is 0, after is 1
    //- Properties for Test node: Path is /TestA; type nt:unstructed
    
    assertNotNull(rootNode);
    assertEquals(rootNode.getPath(), "/");
    assertEquals(rootNode.getNodes().getSize(), 0);

    Node parentNode = rootNode.addNode("Test", "nt:unstructured") ;
    assertEquals(rootNode.getNodes().getSize(), 1);
    assertEquals(parentNode.getName(), "Test");
    assertEquals(parentNode.getPath(), "/Test");
    assertEquals(parentNode.getPrimaryNodeType().getName(), "nt:unstructured");
    
    //Test nt:file case
    //2. Create a node named myFile, Primary type is nt:file, mimeType is text/html
    //- The path is /Test/myFile
    //- Before add myFile to parent node, child node in this node is 0, after is 1
    //- Require in nt:file nee: Size in JcrItemInput is 5
    Map<String , JcrItemInput> mapFile = prepareFileProperties("myFile", "text/html", new GregorianCalendar());
    
    assertEquals(mapFile.size(), 5);
    assertEquals(parentNode.getNodes().getSize(), 0);
    
    Node testNtFile = nodeService_.addNode(parentNode, "nt:file", mapFile, true) ;
    session.save() ;
    assertEquals(parentNode.getNodes().getSize(), 1) ;
    assertEquals(testNtFile.getPath(), "/Test/myFile") ;
    assertEquals(testNtFile.getPrimaryNodeType().getName(), "nt:file") ;
    assertEquals(testNtFile.getName(), "myFile") ;
    assertTrue(testNtFile.hasNodes()) ;
    String mimeType = testNtFile.getNode("jcr:content").getProperty("jcr:mimeType").getString() ;
    assertEquals(mimeType, "text/html") ;    
    
    //Test exo:article case
    //3. Create exo:artical name MyArticle
    
    Map<String , JcrItemInput> mapArticle = prepareArticleProperties();
    Node testArticle = nodeService_.addNode(parentNode, "exo:article", mapArticle, true);
    session.save();   
    assertNotNull(testArticle);
    assertEquals(testArticle.getName(), "MyArticle");
    assertEquals(testArticle.getPrimaryNodeType().getName(), "exo:article");
    assertEquals(testArticle.getPrimaryNodeType().getName(), "exo:article");
    
    //3. When parent node has move, the child nodes is move
    //- root node has'nt child node
    
    parentNode.remove();
    session.save();
    assertEquals(rootNode.getNodes().getSize(), 0);
  } 

  public void testCopyNode() throws Exception{
    //1. Create data
    //1.1
    //- Create node named NodeByCopy with path /Test/NodeByCopy
    //- Create node named NodeByGiveCpy with path /Test/NodeByGiveCpy
    //- Create node named ChildNode with path /Test/NodeByGiveCpy/ChildNode
    //2 Process copy node
    //2.1 Objective
    //- After copy from ChildNode to NodeByCopy, the path this node is: /Test/NodeByCopy/ChildNode
    //- After copy, ChildNode node still path the same
    //2.2 Requie process
    //- Before ChildNode has copy: primary type is 'nt:unstructured', mixin type is 'mix:votable' and 'mix:commentable'
    //- After ChildNode copy, properties is same
    //- Old path /Test/NodeByGiveCpy/ChildNode, new path /Test/NodeByCopy/ChildNode
    //3 Other
    //- Before copy, child node in NodeByCopy is 0, after is 1
    
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    JcrItemInput jcrItemInputNode_ = null, jcrItemInputProperties_ = null;  
    Node desNode = createTestNode("NodeByCopy", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;
    
    assertNotNull(desNode);
    assertEquals(desNode.getName(), "NodeByCopy");
    assertEquals(desNode.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(desNode.getPath(), "/Test/NodeByCopy");
    assertEquals(desNode.getNodes().getSize(), 0);

    Node testGetNode = createTestNode("NodeByGiveCpy", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    assertNotNull(testGetNode);
    assertEquals(testGetNode.getName(), "NodeByGiveCpy");
    assertEquals(testGetNode.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(testGetNode.getPath(), "/Test/NodeByGiveCpy");

    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured");
    srcNode.addMixin("mix:votable") ;
    srcNode.addMixin("mix:commentable");
    srcNode.getSession().save();
    
    assertNotNull(srcNode);
    assertEquals( srcNode.getName(), "ChildNode");
    assertEquals(srcNode.getPath(), "/Test/NodeByGiveCpy/ChildNode");
    assertEquals(srcNode.getPrimaryNodeType().getName(), "nt:unstructured");
    
    //2. Test
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
    Node testCopyNodeCase = nodeService_.copyNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);
    
    assertNotNull(testCopyNodeCase);
    assertEquals(COLLABORATION_WS, "collaboration");
    assertEquals(testCopyNodeCase.getPath(), "/Test/NodeByCopy/ChildNode");
    assertEquals(desNode.getNodes().getSize(), 1);
   
    assertEquals(testCopyNodeCase.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(testCopyNodeCase.getMixinNodeTypes()[0].getName(), "mix:votable");
    assertEquals(testCopyNodeCase.getMixinNodeTypes()[1].getName(), "mix:commentable"); 

    //3.Remove
    srcNode.remove();//or  desNode.remove();
    sessionProvider.close();
    
  }  

  public void testMoveNode() throws Exception{
   
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    JcrItemInput jcrItemInputNode_ = null, jcrItemInputProperties_ = null;    
    
    //1. create data
    
    //1.1 
    //- Create node named NodeByMove with path /Test/NodeByMove
    //- Create node named NodeByGive with path /Test/NodeByGive
    //- Create node named ChildNode with path /Test/NodeByGive/ChildNode
    //2 Process move node
    //2.1 Objective
    //- Before move node, the ChildNode path is: /Test/NodeByGive/ChildNode
    //- After moved node, the ChildNode path is: /Test/NodeByMove/ChildNode
    //2.2 require
    //- Before and after ChildNode moved, properties this node is the same
    //- After moved node, ChildNode in the old path is disappear
    
    Node desNode = createTestNode("NodeByMove", REPO_NAME, "nt:unstructured", 
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    assertNotNull(desNode);
    assertEquals(desNode.getName(), "NodeByMove");
    assertEquals( REPO_NAME, "repository");
    assertEquals(desNode.getPath(), "/Test/NodeByMove");
    assertEquals(desNode.getPrimaryNodeType().getName(), "nt:unstructured");

    Node testGetNode = createTestNode("NodeByGive", REPO_NAME, "nt:unstructured",
        jcrItemInputNode_, jcrItemInputProperties_, sessionProvider) ;

    assertNotNull(testGetNode);
    assertEquals(testGetNode.getName(), "NodeByGive");
    assertEquals(testGetNode.getPath(), "/Test/NodeByGive");
    assertEquals("nt:unstructured", testGetNode.getPrimaryNodeType().getName());

    Node srcNode = testGetNode.addNode("ChildNode", "nt:unstructured") ;
    srcNode.addMixin("mix:votable");
    srcNode.addMixin("mix:commentable");

    assertNotNull(srcNode);
    assertEquals(srcNode.getName(), "ChildNode");
    assertEquals(srcNode.getPath(), "/Test/NodeByGive/ChildNode"); //Before moved
    assertEquals(srcNode.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(srcNode.getMixinNodeTypes()[0].getName(), "mix:votable"); //properties before move
    assertEquals(srcNode.getMixinNodeTypes()[1].getName(), "mix:commentable");

    srcNode.getSession().save();
    String srcPath = srcNode.getPath();
    String desPath = desNode.getPath();
  
    //2. test
    Node testMoveNodeCase = nodeService_.moveNode(REPO_NAME, COLLABORATION_WS, 
        srcPath, COLLABORATION_WS, desPath, sessionProvider);

    assertNotNull(testMoveNodeCase);
    assertEquals(testMoveNodeCase.getPath(), "/Test/NodeByMove/ChildNode");//After moved
    assertNotSame(srcNode.getPath(), "/Test/NodeByGive/ChildNode"); //ChildNode is disappear
    assertEquals(srcNode.getPrimaryNodeType().getName(), "nt:unstructured");
    assertEquals(testMoveNodeCase.getMixinNodeTypes()[0].getName(), "mix:votable"); //properties after moved
    assertEquals(testMoveNodeCase.getMixinNodeTypes()[1].getName(), "mix:commentable");
    
    //3. remove data
    desNode.remove();//or srcNode.remove();
    sessionProvider.close();
    
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
      session.save();
    }
    Map<String,JcrItemInput> map = new HashMap<String, JcrItemInput>() ;

    //node case
    jcrItemInputNode = getNodeInput(nodeType, "mix:votable", nodeName,"/node") ;
    map.put("/node", jcrItemInputNode) ; 

    //properties case
    jcrItemInputProperties = getPropertyInput("/node/exo:title", "PropertiesCaseTest") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;

    Node nodetTest = nodeService_.addNode(parentNode, "nt:unstructured", map, true) ; 
    session.save();

    return nodetTest;
  }

  private Map<String, JcrItemInput> prepareArticleProperties() {
    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();
    JcrItemInput itemInput = getNodeInput("exo:article", "mix:votable", "MyArticle","/node");
    map.put("/node", itemInput);
    
    JcrItemInput itemInputPropText = getPropertyInput("/node/exo:text", "MyText");
    map.put("/node/exo:text", itemInputPropText);
    
    JcrItemInput itemInputPropTitle = getPropertyInput("/node/exo:title", "MyTitle");
    map.put("/node/exo:title", itemInputPropTitle);
    
    JcrItemInput itemInputPropSummary = getPropertyInput("/node/exo:summary", "MySummary");
    map.put("/node/exo:summary", itemInputPropSummary);
    
    return map ;
  }

  private Map<String, JcrItemInput> prepareFileProperties(String fileName,
      String mimeType, Calendar cal) throws Exception{

    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();

    //Node - nt:file
    JcrItemInput itemInput = getNodeInput("nt:file", "mix:votable", fileName,"/node");
    map.put("/node", itemInput);

    //Node - jcr:content
    JcrItemInput itemInputJcrContent = getNodeInput("nt:resource", null, "jcr:content","/node/jcr:content");
    map.put("/node/jcr:content", itemInputJcrContent);

    //properties - data
    JcrItemInput itemInputData = getPropertyInput("/node/jcr:content/jcr:data", "");
    map.put("/node/jcr:content/jcr:data", itemInputData);

    //properties - mimetype
    //mimetype can be: text/html,text/plain....
    JcrItemInput itemInputMimeType = getPropertyInput("/node/jcr:content/jcr:mimeType", mimeType);
    map.put("/node/jcr:content/jcr:mimeType", itemInputMimeType);

    //properties - last modified
    JcrItemInput itemInputLastMdf = getPropertyInput("/node/jcr:content/jcr:lastModified", cal);
    map.put("/node/jcr:content/jcr:lastModified", itemInputLastMdf);
        
    return map;
  }

  private JcrItemInput getNodeInput(String primaryType, String mixinTypes, String nodeName, String path) {
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setType(JcrItemInput.NODE) ;    
    jcrItemInput.setPath(path);
    jcrItemInput.setPrimaryNodeType(primaryType);
    if(mixinTypes!= null && mixinTypes.length() > 0  ) {
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
