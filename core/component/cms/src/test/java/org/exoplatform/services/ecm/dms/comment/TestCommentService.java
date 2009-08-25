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
package org.exoplatform.services.ecm.dms.comment;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;

/**
 * Created by eXo Platform
 * Author : Nguyen Manh Cuong
 *          manhcuongpt@gmail.com 
 * Jun 9, 2009
 */

/**
 * Unit test for CommentService
 * Methods need to test:
 * 1. addComment() method
 * 2. getComment() method
 */
public class TestCommentService extends BaseDMSTestCase {
  
  private final static String I18NMixin = "mix:i18n";
  
  private final static String ARTICLE = "exo:article";
  
  private final static String TITLE = "exo:title";

  private final static String SUMMARY = "exo:summary";

  private final static String TEXT = "exo:text";
  
  private final static String COMMENT            = "comments".intern();

  private final static String COMMENTOR          = "exo:commentor".intern();

  private final static String COMMENTOR_EMAIL    = "exo:commentorEmail".intern();

  private final static String COMMENTOR_MESSAGES = "exo:commentContent".intern();
  
  private final static String ANONYMOUS = "anonymous".intern() ;

  private CommentsService     commentsService    = null;
  
  private MultiLanguageService multiLangService  = null;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    commentsService = (CommentsService) container.getComponentInstanceOfType(CommentsService.class);
    multiLangService = (MultiLanguageService) container.getComponentInstanceOfType(MultiLanguageService.class);

  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      Test node: doesn't have multiple languages
   *                 doesn't have comment node
   * Expected:
   *      Test node has comment node with 2 comments.
   */
  public void testAddComment1() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if(test.canAddMixin(I18NMixin)){
      test.addMixin(I18NMixin);
    }
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", multiLangService.getDefault(test));
    NodeIterator iter = test.getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
  }

  /**
   * Test Method: addComment()
   * Input:
   *      Test Node: has multiple languages
   *                 doesn't have comment node
   *      language of comment  = default language
   * Expected:
   *      Test node has comment node with 2 comments.
   */
  public void testAddComment2() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", multiLangService.getDefault(test));
    NodeIterator iter = test.getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
  }

  /**
   * Test Method: addComment()
   * Input:
   *      Test Node: has multiple language
   *                 has comment node
   *      first: add comment of root
   *      and then: Test node had comment node --> add more comment
   *      language of comment != default language
   * Expected:
   *      Test node has comment node which added more comment.
   */
  public void testAddComment3() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", multiLangService.getDefault(test));
    NodeIterator iter = test.getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      Test Node: has multiple languages, NodeType of test node is "nt:file" 
   *                 doesn't have comment node, NodeType of COMMENTS node is "nt:unstructured" 
   * Expected Result: throws Exception
   */
  public void testAddComment4() throws Exception {
    Node test = session.getRootNode().addNode("Test", "nt:file");
    if(test.getPrimaryNodeType().getName().equals("nt:file")){
      test.addNode("jcr:content", "nt:base");
    }
    session.save();
    try {
      commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "vi");
    } catch (Exception e) {
    }
  }
  
  /**
   * Case 5:
   * Test Method: addComment()
   * Input: 
   *      Test Node: has multiple languages which contains language of comment (en)
   *                 en node: doesn't have comment node.
   * Expected:
   *      comment node is added in en node with 2 comments
   */
  public void testAddComment5() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "en");
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", "en");
    NodeIterator iter = multiLangService.getLanguage(test, "en").getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      Test Node: has multiple languages which contains language of comment (en)
   *                 en Node: has comment node.
   * Expected:
   *      comment node with en language will be added more comments
   */
  public void testAddComment6() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "en");
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", "en");
    NodeIterator iter = multiLangService.getLanguage(test, "en").getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      Test Node: has multiple languages which doesn't contain language of comment (jp).
   * Expected:
   *         multiple languages adds language of comment.
   *         comment node is added jp node with comments.
   */
  public void testAddComment7() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", "jp");
    NodeIterator iter = multiLangService.getLanguage(test, "jp").getNode(COMMENT).getNodes();
    int i = 0;
    while (iter.hasNext()) {
      check(i++, iter.nextNode());
    }
    assertEquals("jp", multiLangService.getLanguage(test, "jp").getName());
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      Test node doesn't have multiple languages, 
   *      language of comment = null;
   * Expected: throws NullPointerException
   */
  public void testAddComment8() throws Exception {
    Node test = session.getRootNode().addNode("Test");
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    session.save();
    multiLangService.addLanguage(test, createMapInput(), "jp", false);
    try {
      commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", null);
    } catch (NullPointerException e) {
    }
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      commenter's name is null.
   * Expected:
   *      commenter's name will be assigned ANONYMOUS.
   */
  public void testAddComment9() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if(test.canAddMixin(I18NMixin)){
      test.addMixin(I18NMixin);
    }
    session.save();
    commentsService.addComment(test, null, "null@explatform.com", null, "Hello", multiLangService.getDefault(test));
    commentsService.addComment(test, null, "abc@explatform.com", null, "Hello", multiLangService.getDefault(test));
    List<Node> listCommentNode = commentsService.getComments(test, multiLangService.getDefault(test));
    Collections.sort(listCommentNode, new NameComparator());
    Iterator<Node> iter = listCommentNode.iterator();
    while(iter.hasNext()){
      assertEquals(ANONYMOUS, iter.next().getProperty(COMMENTOR).getString());
    }
  }
  
  /**
   * Test Method: addComment()
   * Input:
   *      commenter's email is null.
   * Expected:
   *      comment doesn't have COMMENTOR_EMAIL property.
   */
  public void testAddComment10() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if(test.canAddMixin(I18NMixin)){
      test.addMixin(I18NMixin);
    }
    session.save();
    commentsService.addComment(test, "root", null, null, "Hello", multiLangService.getDefault(test));
    List<Node> listCommentNode = commentsService.getComments(test, multiLangService.getDefault(test));
    Collections.sort(listCommentNode, new NameComparator());
    Iterator<Node> iter = listCommentNode.iterator();
    while(iter.hasNext()){
      assertFalse(iter.next().hasProperty(COMMENTOR_EMAIL));
    }
  }
  
  /**
   * Test Method: updateComment()
   * Input:
   *      comment = Hello;
   *      update comment = Ciao;
   * Expected:
   *      Comment message is "Ciao"
   */
  public void testUpdateComment() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    List<Node> nodes = commentsService.getComments(test, multiLangService.getDefault(test));
    commentsService.updateComment(nodes.get(0), "Ciao");
    nodes = commentsService.getComments(test, multiLangService.getDefault(test));
    assertEquals("Ciao", nodes.get(0).getProperty(COMMENTOR_MESSAGES).getString());
  }
  
  /**
   * Test Method: deleteComment()
   * Input:
   *      comment for node with given email, comment message, commentor.
   * Expected:
   *      Comment for node is delete
   */
  public void testDeleteComment() throws Exception{
    Node test = initNode();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    List<Node> nodes = commentsService.getComments(test, multiLangService.getDefault(test));
    assertEquals(1, nodes.size());
    commentsService.deleteComment(nodes.get(0));
    nodes = commentsService.getComments(test, multiLangService.getDefault(test));
    assertEquals(0, nodes.size());
  }
  /**
   * Test Method: getComments()
   * Input: Test node doesn't have languages node, so adding comment nodes will be set default 
   *        language. 
   * Expected Result:
   *        Get all comment nodes with default language.
   */
  public void testGetComments1() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if(test.canAddMixin(I18NMixin)){
      test.addMixin(I18NMixin);
    }
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", multiLangService.getDefault(test));
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", multiLangService.getDefault(test));
    List<Node> listCommentNode = commentsService.getComments(test, multiLangService.getDefault(test));
    Collections.sort(listCommentNode, new NameComparator());
    Iterator<Node> iter = listCommentNode.iterator();
    int i = 0;
    while(iter.hasNext()){
      check(i++, iter.next());
    }
  }

  /**
   * Test Method: getComments()
   * Input: Node has some comment nodes in "jp" node.
   * Expected Result:
   *        Get all comment nodes with jp language.
   */
  public void testGetComments2() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if(test.canAddMixin(I18NMixin)){
      test.addMixin(I18NMixin);
    }
    session.save();
    multiLangService.addLanguage(test, createMapInput(), "jp", false);
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Thanks", "jp");
    
    List<Node> listCommentNode = commentsService.getComments(test, "jp");
    Collections.sort(listCommentNode, new NameComparator());
    Iterator<Node> iter = listCommentNode.iterator();
    int i = 0;
    while(iter.hasNext()){
      check(i++, iter.next());
    }
    assertEquals(2, listCommentNode.size());
  }

  /**
   * Create a map to use for MultilLanguageService
   */
  private Map<String, JcrInputProperty>  createMapInput() {
    Map<String, JcrInputProperty> map = new HashMap<String, JcrInputProperty>();
    String titlePath = CmsService.NODE + "/" + TITLE;
    String summaryPath = CmsService.NODE + "/" + SUMMARY;
    String textPath = CmsService.NODE + "/" + TEXT;
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(CmsService.NODE);

    inputProperty.setValue("test");
    map.put(CmsService.NODE, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(titlePath);
    inputProperty.setValue("this is title");
    map.put(titlePath, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(summaryPath);
    inputProperty.setValue("this is summary");
    map.put(summaryPath, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(textPath);
    inputProperty.setValue("this is article content");
    map.put(textPath, inputProperty);
    return map;
  }  
  
  /**
   * This method will create a node which is added MultiLanguage
   */  
  private Node initNode() throws Exception{
    Node test = session.getRootNode().addNode("test", ARTICLE);
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    test.setProperty(TITLE, "sport");
    test.setProperty(SUMMARY, "report of season");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
    multiLangService.addLanguage(test, createMapInput(), "en", false);
    multiLangService.addLanguage(test, createMapInput(), "vi", false);
    return test;
  }  
  
  /**
   * This method is used to check case test.
   * @param i          
   * @param node       node is tested
   * @throws Exception
   */
  private void check(int i, Node node) throws Exception{
    switch (i) {
    case 0:
      assertEquals("root", node.getProperty(COMMENTOR).getString());
      assertEquals("root@explatform.com", node.getProperty(COMMENTOR_EMAIL).getString());
      assertEquals("Hello", node.getProperty(COMMENTOR_MESSAGES).getString());
      break;
    case 1:
      assertEquals("marry", node.getProperty(COMMENTOR).getString());
      assertEquals("marry@explatform.com", node.getProperty(COMMENTOR_EMAIL).getString());
      assertEquals("Thanks", node.getProperty(COMMENTOR_MESSAGES).getString());
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
      Node test = session.getRootNode().getNode("Test");
      test.remove();
      session.save();
    }
    super.tearDown();
  }
  
  class NameComparator implements Comparator<Node>{
    public int compare(Node node1, Node node2){
        try {
          return node1.getName().compareTo(node2.getName());
        } catch (RepositoryException e) {
          return 0;
        }
    }
  }
}
