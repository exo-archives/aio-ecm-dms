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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.cms.comments.CommentsService;
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
  
  private final static String COMMENT            = "comments".intern();

  private final static String LANGUAGES          = "languages".intern();

  private final static String COMMENTOR          = "exo:commentor".intern();

  private final static String COMMENTOR_EMAIL    = "exo:commentorEmail".intern();

  private final static String COMMENTOR_MESSAGES = "exo:commentContent".intern();

  private CommentsService     commentsService    = null;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    commentsService = (CommentsService) container.getComponentInstanceOfType(CommentsService.class);
  }

  /**
   * Test Method: addComment(): 
   * Input: Test node has multi-languages node, but not "jp" language.
   *        Comment node has properties: Commenter = root,
   *                                     Commentor_email = root@exoplatform,
   *                                     Commentor_messages = Hello,
   *                                     Language = jp
   * Expected Result:
   *       "jp" node is added in languages node
   *       comment node is added in "jp" node with properties like input.
   */
  public void testAddCommnent() throws Exception {
    Node test = session.getRootNode().addNode("Test");
    session.save();
    Node languages = test.addNode(LANGUAGES);
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    Node comments = languages.getNode("jp").getNode(COMMENT);
    NodeIterator iterator = comments.getNodes();
    Node temp;
    while(iterator.hasNext()){
      temp = iterator.nextNode();
      assertEquals("root", temp.getProperty(COMMENTOR).getString());
      assertEquals("root@explatform.com", temp.getProperty(COMMENTOR_EMAIL).getString());
      assertEquals("Hello", temp.getProperty(COMMENTOR_MESSAGES).getString());
      assertEquals("jp", languages.getNode("jp").getName());
    }
  }
  
  /**
   * Test Method: addComment()
   * Input: Test node doesn't has languages node.
   * Expected result: Comments node is added in test node.
   */
  public void testAddCommnent2() throws Exception {
    Node test = session.getRootNode().addNode("Test");
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    Node comments = test.getNode(COMMENT);
    NodeIterator iterator = comments.getNodes();
    Node temp;
    while(iterator.hasNext()){
      temp = iterator.nextNode();
      assertEquals("root", temp.getProperty(COMMENTOR).getString());
      assertEquals("root@explatform.com", temp.getProperty(COMMENTOR_EMAIL).getString());
      assertEquals("Hello", temp.getProperty(COMMENTOR_MESSAGES).getString());
    }
  }  
  
  /**
   * Test Method: addComment()
   * Input Node: NodeType is unstructured
   * Expected Result: throws Exception
   */
  public void testAddCommnent3() throws Exception {
    Node test = session.getRootNode().addNode("test3", "nt:file");
    Exception e = null;
    if(test.getPrimaryNodeType().getName().equals("nt:file")){
      test.addNode("jcr:content", "nt:base");
    }
    session.save();
   try{
      commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "vi");
    }catch (Exception ex) {
      e = ex;
    }
    assertNotNull(e);
  }

  /**
   * Test Method: getComments()
   * Input: Node has some comment nodes in "jp" node.
   * Expected Result:
   *        Get all comment nodes with jp language.
   */
  public void testGetComments() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    session.save();
    test.addNode(LANGUAGES);
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    commentsService.addComment(test, "john", "john@explatform.com", null, "Thanks", "jp");
    commentsService.addComment(test, "marry", "marry@explatform.com", null, "Bye", "en");
    
    List<Node> listCommentNode = commentsService.getComments(test, "jp");
    List<Node> listCommentNodeByEn = commentsService.getComments(test, "en");
    Node commentNode1 = listCommentNode.get(0);
    assertEquals("john", commentNode1.getProperty(COMMENTOR).getString());
    assertEquals("john@explatform.com", commentNode1.getProperty(COMMENTOR_EMAIL).getString());
    assertEquals("Thanks", commentNode1.getProperty(COMMENTOR_MESSAGES).getString());
    
    Node commentNode2 = listCommentNode.get(1);
    assertEquals("root", commentNode2.getProperty(COMMENTOR).getString());
    assertEquals("root@explatform.com", commentNode2.getProperty(COMMENTOR_EMAIL).getString());
    assertEquals("Hello", commentNode2.getProperty(COMMENTOR_MESSAGES).getString());
    
    Node commentNode3 = listCommentNodeByEn.get(0);
    assertEquals("marry", commentNode3.getProperty(COMMENTOR).getString());
    assertEquals("marry@explatform.com", commentNode3.getProperty(COMMENTOR_EMAIL).getString());
    assertEquals("Bye", commentNode3.getProperty(COMMENTOR_MESSAGES).getString());
  }
  
  /**
   * Test Method: getComments()
   * Input: Test node doesn't have languages node, so adding comment nodes will be set default 
   *        language. 
   * Expected Result:
   *        Get all comment nodes with default language.
   */
  public void testGetComments2() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    session.save();
    commentsService.addComment(test, "root", "root@explatform.com", null, "Hello", "jp");
    commentsService.addComment(test, "john", "john@explatform.com", null, "Thanks", "cn");
    
    List<Node> listCommentNode = commentsService.getComments(test, "jp");
    Node commentNode1 = listCommentNode.get(0);
    assertEquals("john", commentNode1.getProperty(COMMENTOR).getString());
    assertEquals("john@explatform.com", commentNode1.getProperty(COMMENTOR_EMAIL).getString());
    assertEquals("Thanks", commentNode1.getProperty(COMMENTOR_MESSAGES).getString());
    
    Node commentNode2 = listCommentNode.get(1);
    assertEquals("root", commentNode2.getProperty(COMMENTOR).getString());
    assertEquals("root@explatform.com", commentNode2.getProperty(COMMENTOR_EMAIL).getString());
    assertEquals("Hello", commentNode2.getProperty(COMMENTOR_MESSAGES).getString());
  }  
}
