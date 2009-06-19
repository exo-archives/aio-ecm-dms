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
package org.exoplatform.services.ecm.dms.folksonomy;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by eXo Platform
 * Author : Nguyen Manh Cuong
 *          manhcuongpt@gmail.com
 * Jun 12, 2009
 */

/**
 * Unit test for FolksonomyService
 * Methods need to test
 * 1. init() method
 * 2. addTag() method
 * 3. getTag() method
 * 4. getAllTags method
 * 5. getDocumentOnTags() method
 * 6. getLinkedTagsOnDocument() method
 * 7. updateStyle() method
 */
public class TestFolksonomyService extends BaseDMSTestCase {

  private final static String HTML_STYLE_PROP = "exo:htmlStyle".intern();  

  private final static String TAG_RATE_PROP = "exo:styleRange".intern();
  
  private String exoTagStylePath;
  
  private String baseTagsPath;
  
  private FolksonomyService folksonomyService = null;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    folksonomyService = (FolksonomyService) container.getComponentInstanceOfType(FolksonomyService.class);
    NodeHierarchyCreator nodehierarchyCreator = (NodeHierarchyCreator) container
    .getComponentInstanceOfType(NodeHierarchyCreator.class);
    exoTagStylePath = nodehierarchyCreator.getJcrPath(BasePath.EXO_TAG_STYLE_PATH);
    baseTagsPath = nodehierarchyCreator.getJcrPath(BasePath.EXO_TAGS_PATH);
  }

  /**
   * Test Method: init()
   * Input: specified repository
   * Expected: Tag Styles is initialized
   */
    public void testInit() throws Exception {
      Session systemSession = getSystemSession(REPO_NAME);
      Node exoTagsHomeNode = (Node) systemSession.getItem(exoTagStylePath);
      NodeIterator iterator = exoTagsHomeNode.getNodes();
      Node temp;
      int i = 0;
      while(iterator.hasNext()){
        temp = iterator.nextNode();
        check(i, temp);
        i++;
      }
    }

  /**
   * Test Method: addTag()
   * Input: Node test, tags don't exist in repository,
   *        test node doesn't follow exo:folksonomized NodeType
   * Expected Result: 
   *        tags are added in repository test has property: exo:folksonomy
   *        test node has a property which value is equal with UUID of tagNode
   */
  public void testAddTag() throws Exception {
    Session systemSession = getSystemSession(REPO_NAME);
    Node exoTagsHomeNode = (Node) systemSession.getItem(baseTagsPath);
    Node test = session.getRootNode().addNode("Test");
    session.save();
    String[] tagNames = { "AAAA" };
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    Property property = test.getProperty("exo:folksonomy");
    Value[] values;
    if (property.getDefinition().isMultiple()) {
      values = property.getValues();
      for (Value val : values) {
        assertEquals(val.getString(), exoTagsHomeNode.getNode("AAAA").getUUID());
      }
    }
    assertTrue(exoTagsHomeNode.hasNode("AAAA"));
    assertTrue(test.isNodeType("exo:folksonomized"));
  }

  /**
   * Test Method: addTag()
   * Input: Test node has exo:folksonomized NodeType, Tags existed in repository
   * Expected Result:
   *        Test node has a property which value is equal with UUID of tag node
   *        Property add more value equal UUID of tag node
   */
  public void testAddTag1() throws Exception {
    Session systemSession = getSystemSession(REPO_NAME);
    Node exoTagsHomeNode = (Node) systemSession.getItem(baseTagsPath);    
    Node test = session.getRootNode().addNode("Test");
    session.save();
    String[] tagNames = { "AAAA", "BBBB"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    Property property = test.getProperty("exo:folksonomy");
    Value[] values;
    if (property.getDefinition().isMultiple()) {
      values = property.getValues();
      Value val1 = values[0];
      Value val2 = values[1];
      assertEquals(val1.getString(), exoTagsHomeNode.getNode("AAAA").getUUID());
      assertEquals(val2.getString(), exoTagsHomeNode.getNode("BBBB").getUUID());
    }
  }
  
  /**
   * Test Method: getTag()
   * Input: Repository has tag node "AAAA" with path : "/jcr:system/exo:ecm/exo:folksonomies/exo:tags/AAAA"
   * Expected:
   *        "AAAA" is existed with path: "/jcr:system/exo:ecm/exo:folksonomies/exo:tags/AAAA". 
   */
  public void testGetTag() throws Exception{
    String tagPath = baseTagsPath + "/AAAA";
    Node root = session.getRootNode();
    Node test = root.addNode("Test");
    session.save();
    String[] tagNames = {"AAAA"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    
    Node node = folksonomyService.getTag(tagPath, REPO_NAME);
    assertEquals(tagPath, node.getPath());
  }
  
  /**
   * Test Method: getTag()
   * Input: Repository doesn't have tagNames DDDD
   * Expected: throws exceptions
   */
  public void testGetTag1()throws Exception{
    String tagPath = baseTagsPath + "/DDDD";    
    Exception e = null;
    try{
      folksonomyService.getTag(tagPath, REPO_NAME);
    } catch (PathNotFoundException ex) {
      e = ex;
    }
    assertNotNull(e);
  }
  
  /**
   * Test Method: getAllTags()
   * Input: Repository has tags: "AAAA", "BBBB", "CCCC"
   * Expected:
   *        Get all tags in repository: "AAAA", "BBBB", "CCCC"
   *        UUID of tag nodes are equal value of "exo:folksonomy" property
   */
  public void testGetAllTags() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    session.save();
    String[] tagNames = {"AAAA", "BBBB", "CCCC"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    
    List<Node> listTags = folksonomyService.getAllTags(REPO_NAME);
    Node tagNode1 = listTags.get(0);
    Node tagNode2 = listTags.get(1);
    Node tagNode3 = listTags.get(2);
    Property property = test.getProperty("exo:folksonomy");
    Value[] values;
    if (property.getDefinition().isMultiple()) {
      values = property.getValues();
      Value val1 = values[0];
      Value val2 = values[1];
      Value val3 = values[2];
      assertEquals(val1.getString(), tagNode1.getUUID());
      assertEquals(val2.getString(), tagNode2.getUUID());
      assertEquals(val3.getString(), tagNode3.getUUID());
    }
    assertEquals("AAAA", tagNode1.getName());
    assertEquals("BBBB", tagNode2.getName());
    assertEquals("CCCC", tagNode3.getName());
    
  }
  
  /**
   * Test Method: getAllTagStyle()
   * Input: Repository has some tag styles. 
   * Expected: all tag styles are listed
   */
  public void testGetAllTagStyle() throws Exception{
    List<Node> listTagStyles = folksonomyService.getAllTagStyle(REPO_NAME);
    assertNotNull(listTagStyles);
    assertEquals(5, listTagStyles.size());
    assertEquals("nomal", listTagStyles.get(0).getName());
    assertEquals("interesting", listTagStyles.get(1).getName());
    assertEquals("attractive", listTagStyles.get(2).getName());
    assertEquals("hot", listTagStyles.get(3).getName());
    assertEquals("hotest", listTagStyles.get(4).getName());
  }
  
  /**
   * Test Method: getTagStyle()
   * Input: Tag style name is nomal
   * Expected:
   *     Value of HTML_STYLE_PROP property of nomal tag style:
   *          font-size: 12px; font-weight: bold; color: #6b6b6b; font-family: verdana; text-decoration:none;
   */
  public void testGetTagStyle() throws Exception{
    String value = "font-size: 12px; font-weight: bold; color: #6b6b6b; font-family: verdana; text-decoration:none;";
    String htmlTagStyle = folksonomyService.getTagStyle("nomal", REPO_NAME);
    assertEquals(value, htmlTagStyle);
  }
  
  /**
   * Test Method: getTagStyle()
   * Input: Tag style is existed in repository
   * Expected: throws Exception
   */
  public void testGetTagStyle1() {
    Exception e = null;
    try {
      folksonomyService.getTagStyle("normal", REPO_NAME);
    } catch (Exception ex) {
      e = ex;
    }
    assertNotNull(e);
  }
  
  /**
   * Test Method: getDocumentsOnTag()
   * Input: Test1 and Test2 node added EEEE tag.
   * Expected:
   *        Test1 and Test2 node are listed.
   */
  public void testGetDocumentsOnTag() throws Exception{
    String tagPath = baseTagsPath + "/EEEE";    
    Node test = session.getRootNode().addNode("Test1");
    Node test1 = session.getRootNode().addNode("Test2");
    session.save();
    String[] tagNames = {"AAAA", "BBBB", "CCCC", "EEEE"};
    String[] tagNames1 = {"EEEE"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    folksonomyService.addTag(test1, tagNames1, REPO_NAME);

    List<Node> list = folksonomyService.getDocumentsOnTag(tagPath, REPO_NAME);
    Node node1 = list.get(0);
    Node node2 = list.get(1);
    assertEquals("Test1", node1.getName());
    assertEquals("Test2", node2.getName());
  }
  
  /**
   * Test Method: getDocumentOnTag()
   * Input: there hasn't got any node to be added FFFF tag.
   * Expected: throws Exception.
   */
  public void testGetDocumentOnTag() throws Exception{
    String tagPath = baseTagsPath + "/FFFF";        
    Exception e = null;
    try {
      folksonomyService.getDocumentsOnTag(tagPath, REPO_NAME);
    } catch (Exception ex) {
      e =ex;
    }
    assertNotNull(e);
  }
  
  /**
   * Test Method: getLinkedTagsOfDocument()
   * Input: Document node has 3 linked tags: tag1, tag2, tag3.
   * Expected:
   *        List all linked tags of document node.  
   */
  public void testGetLinkedTagsOfDocument() throws Exception{
    Node document = session.getRootNode().addNode("document");
    session.save();
    String[] tagNames = {"tag1", "tag2", "tag3"};
    folksonomyService.addTag(document, tagNames, REPO_NAME);
    
    List<Node> tagList = folksonomyService.getLinkedTagsOfDocument(document, REPO_NAME);
    Node tagNode1 = tagList.get(0);
    Node tagNode2 = tagList.get(1);
    Node tagNode3 = tagList.get(2);
    
    Property property = document.getProperty("exo:folksonomy");
    Value[] values;
    if (property.getDefinition().isMultiple()) {
      values = property.getValues();
      Value val1 = values[0];
      Value val2 = values[1];
      Value val3 = values[2];
      assertEquals(val1.getString(), tagNode1.getUUID());
      assertEquals(val2.getString(), tagNode2.getUUID());
      assertEquals(val3.getString(), tagNode3.getUUID());
    }
    
    assertEquals("tag1", tagNode1.getName());
    assertEquals("tag2", tagNode2.getName());
    assertEquals("tag3", tagNode3.getName());
  }

  /**
   * Test Method: updateStype()
   * Input: 
   *   HTML_STYLE_PROP: htmlStyle:"font-size: 12px; font-weight: bold; color: #6b6b6b; font-family: verdana; text-decoration:none;"
   *   TAG_RATE_PROP: "0..2"
   * Expected: 
   *   HTML_STYLE_PROP: "font-size: 20px; font-weight: bold;"
   *   TAG_RATE_PROP:    "2..5"
   */
  public void testUpdateStype() throws Exception{
    Session systemSession = getSystemSession(REPO_NAME);
    String tagPath = exoTagStylePath + "/nomal";
    String tagRate = "2..5";
    String htmlStyle = "font-size: 20px; font-weight: bold;";
    folksonomyService.updateStype(tagPath, tagRate, htmlStyle, REPO_NAME);
    Node nomalNode = (Node) systemSession.getItem(tagPath);
    
    assertEquals(tagRate, nomalNode.getProperty(TAG_RATE_PROP).getValue().getString());
    assertEquals(htmlStyle, nomalNode.getProperty(HTML_STYLE_PROP).getValue().getString());
  }
  
  /**
   * This method will get session with specified
   * @param repo
   * @return
   * @throws Exception
   */
  private Session getSystemSession(String repo) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repo);
    return manageableRepository.getSystemSession(manageableRepository.getConfiguration()
        .getSystemWorkspaceName());
  }

  /**
   * Check test cases when run testInit()
   * @param i
   * @param node
   * @throws Exception
   */
  private void check(int i, Node node) throws Exception{
    switch (i) {
    case 0:
      assertEquals(
          "font-size: 12px; font-weight: bold; color: #6b6b6b; font-family: verdana; text-decoration:none;",
          node.getProperty(HTML_STYLE_PROP).getValue().getString());
      assertEquals("0..2", node.getProperty(TAG_RATE_PROP).getValue().getString());
      break;
    case 1:
      assertEquals(
          "font-size: 13px; font-weight: bold; color: #5a66ce; font-family: verdana; text-decoration:none;",
          node.getProperty(HTML_STYLE_PROP).getValue().getString());
      assertEquals("2..5", node.getProperty(TAG_RATE_PROP).getValue().getString());
      break;
    case 2:
      assertEquals(
          "font-size: 15px; font-weight: bold; color: blue; font-family: Arial; text-decoration:none;",
          node.getProperty(HTML_STYLE_PROP).getValue().getString());
      assertEquals("5..7", node.getProperty(TAG_RATE_PROP).getValue().getString());
      break;
    case 3:
      assertEquals(
          "font-size: 18px; font-weight: bold; color: #ff9000; font-family: Arial; text-decoration:none;",
          node.getProperty(HTML_STYLE_PROP).getValue().getString());
      assertEquals("7..10", node.getProperty(TAG_RATE_PROP).getValue().getString());
      break;
    case 4:
      assertEquals(
          "font-size: 20px; font-weight: bold; color: red; font-family:Arial; text-decoration:none;",
          node.getProperty(HTML_STYLE_PROP).getValue().getString());
      assertEquals("10..*", node.getProperty(TAG_RATE_PROP).getValue().getString());
      break;

    default:
      break;
    }
  }  
}
