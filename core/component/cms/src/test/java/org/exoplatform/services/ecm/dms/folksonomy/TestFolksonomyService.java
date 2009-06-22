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

import edu.emory.mathcs.backport.java.util.Arrays;

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
  
  private Node test;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    folksonomyService = (FolksonomyService) container.getComponentInstanceOfType(FolksonomyService.class);
    NodeHierarchyCreator nodehierarchyCreator = (NodeHierarchyCreator) container
    .getComponentInstanceOfType(NodeHierarchyCreator.class);
    exoTagStylePath = nodehierarchyCreator.getJcrPath(BasePath.EXO_TAG_STYLE_PATH);
    baseTagsPath = nodehierarchyCreator.getJcrPath(BasePath.EXO_TAGS_PATH);
    test = session.getRootNode().addNode("Test");
    session.save();
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
    String[] tagNames = { "AAAA" };
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    Property property = test.getProperty("exo:folksonomy");
    Value[] values = property.getValues();
      for (Value val : values) {
        assertEquals(val.getString(), exoTagsHomeNode.getNode("AAAA").getUUID());
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
  @SuppressWarnings("unchecked")
  public void testAddTag1() throws Exception {
    Session systemSession = getSystemSession(REPO_NAME);
    Node exoTagsHomeNode = (Node) systemSession.getItem(baseTagsPath);    
    String[] tagNames = { "AAAA", "BBBB"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    Property property = test.getProperty("exo:folksonomy");
    List list = Arrays.asList(new String[] {exoTagsHomeNode.getNode("AAAA").getUUID(), exoTagsHomeNode.getNode("BBBB").getUUID()});
    Value[] values = property.getValues();
    for (Value value : values) {
        assertTrue(list.contains(value.getString()));
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
    try{
      folksonomyService.getTag(tagPath, REPO_NAME);
      fail();
    } catch (PathNotFoundException ex) {
    }
  }
  
  /**
   * Test Method: getAllTags()
   * Input: Repository has tags: "AAAA", "BBBB", "CCCC"
   * Expected:
   *        Get all tags in repository: "AAAA", "BBBB", "CCCC"
   *        UUID of tag nodes are equal value of "exo:folksonomy" property
   */
  @SuppressWarnings("unchecked")
  public void testGetAllTags() throws Exception{
    String[] tagNames = {"AAAA", "BBBB", "CCCC"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    List<Node> listTags = folksonomyService.getAllTags(REPO_NAME);
    List list = Arrays.asList(new String[] {listTags.get(0).getUUID(), listTags.get(1).getUUID(), listTags.get(2).getUUID()});
    List listTagNames = Arrays.asList(new String[] {"AAAA", "BBBB", "CCCC"});
    Property property = test.getProperty("exo:folksonomy");
    Value[] values = property.getValues();
    for (Value value : values) {
      assertTrue(list.contains(value.getString()));
    }
    for (Node node : listTags) {
      assertTrue(listTagNames.contains(node.getName()));
    }
  }
  
  /**
   * Test Method: getAllTagStyle()
   * Input: Repository has some tag styles. 
   * Expected: all tag styles are listed
   */
  @SuppressWarnings("unchecked")
  public void testGetAllTagStyle() throws Exception{
    List<Node> listTagStyles = folksonomyService.getAllTagStyle(REPO_NAME);
    List listStyleName = Arrays.asList(new String[] {"nomal", "interesting", "attractive", "hot", "hotest"});
    assertNotNull(listTagStyles);
    for (Node styleNode : listTagStyles) {
      assertTrue(listStyleName.contains(styleNode.getName()));
    }
    assertEquals(5, listTagStyles.size());
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
    try {
      folksonomyService.getTagStyle("normal", REPO_NAME);
      fail("normal is not existed");
    } catch (Exception ex) {
    }
  }
  
  /**
   * Test Method: getDocumentsOnTag()
   * Input: Test1 and Test2 node added EEEE tag.
   * Expected:
   *        Test1 and Test2 node are listed.
   */
  public void testGetDocumentsOnTag() throws Exception{
    String tagPath = baseTagsPath + "/EEEE";    
    Node test1 = test.addNode("Test1");
    Node test2 = test.addNode("Test2");
    session.save();
    String[] tagNames = {"AAAA", "BBBB", "CCCC", "EEEE"};
    String[] tagNames1 = {"EEEE"};
    folksonomyService.addTag(test1, tagNames, REPO_NAME);
    folksonomyService.addTag(test2, tagNames1, REPO_NAME);
    List<Node> list = folksonomyService.getDocumentsOnTag(tagPath, REPO_NAME);
    assertTrue(list.contains(test1));
    assertTrue(list.contains(test2));
  }
  
  /**
   * Test Method: getDocumentOnTag()
   * Input: there hasn't got any node to be added FFFF tag.
   * Expected: throws Exception.
   */
  public void testGetDocumentOnTag() throws Exception{
    String tagPath = baseTagsPath + "/FFFF";        
    try {
      folksonomyService.getDocumentsOnTag(tagPath, REPO_NAME);
      fail("FFFF tag is not existed");
    } catch (Exception ex) {
    }
  }
  
  /**
   * Test Method: getLinkedTagsOfDocument()
   * Input: test node has 3 linked tags: tag1, tag2, tag3.
   * Expected:
   *        List all linked tags of test node.  
   */
  @SuppressWarnings("unchecked")
  public void testGetLinkedTagsOfDocument() throws Exception{
    String[] tagNames = {"tag1", "tag2", "tag3"};
    folksonomyService.addTag(test, tagNames, REPO_NAME);
    List<Node> tagList = folksonomyService.getLinkedTagsOfDocument(test, REPO_NAME);
    List listTagNames = Arrays.asList(new String[] {"tag1", "tag2", "tag3"});
    List listValue = Arrays.asList(new String[] {tagList.get(0).getUUID(), tagList.get(1).getUUID(), tagList.get(2).getUUID()});
    Property property = test.getProperty("exo:folksonomy");
    Value[] values = property.getValues();
    for (Value value : values) {
      assertTrue(listValue.contains(value.getString()));
    }
    for (Node node : tagList) {
      assertTrue(listTagNames.contains(node.getName()));
    }
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
  
  /**
   * Clean data test
   */
  public void tearDown() throws Exception {
    if (session.itemExists("/Test")) {
      test = session.getRootNode().getNode("Test");
      test.remove();
      session.save();
    }
    super.tearDown();
  }    
}
