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
package org.exoplatform.services.ecm.comment;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.ecm.BaseECMTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 9, 2008  
 */
public class TestCommentService extends BaseECMTestCase{
  private CommentService commentService_ = null;   
    
  public void testGetComment() throws Exception {
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test = root.addNode("Test", "nt:unstructured");
    test.setProperty("exo:language", "English");
    session.save();
    //Add Comment
    commentService_ = (CommentService)container.getComponentInstanceOfType(CommentService.class);
    commentService_.addComment(test, "root", "xxx5669@yahoo.com", null, "Cam on nha", null);
    commentService_.addComment(test, "john", "xxx5668@yahoo.com", null, "thanks", null);
    
    //Get Comment
    List<Node> list = commentService_.getComment(test, "English");
    assertEquals(2, list.size());
    
    Node node1 = list.get(0);//node1 : commenter: john
    Node node2 = list.get(1);//node2 : commenter: root
    
    //Test
    System.out.println("\n-----------------------------TestGetComment 1\n");
    assertEquals("john", node1.getProperty(commentService_.COMMENTOR).getString());
    assertEquals("thanks", node1.getProperty(commentService_.MESSAGE).getString());
    
    System.out.println("\n-----------------------------TestGetComment 2\n");
    assertEquals("root", node2.getProperty(commentService_.COMMENTOR).getString());
    assertEquals("Cam on nha", node2.getProperty(commentService_.MESSAGE).getString());
    
  }
  
  public void testAddComment() throws Exception {    
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS) ;    
    Node root = session.getRootNode();    
    Node test = root.addNode("Test", "nt:unstructured");    
    session.save();
    assertNotNull(test);
    commentService_ = (CommentService)container.getComponentInstanceOfType(CommentService.class);     
    commentService_.addComment(test, "root", "xxx5669@yahoo.com", null, "Cam on nha", null);
    commentService_.addComment(test, "john", "xxx5668@yahoo.com", null, "thanks", null);
    commentService_.addComment(test, "", "xxx5667@yahoo.com", null, "thanks alot", null);
    Node comments = test.getNode(commentService_.COMMENTS);
    
    NodeIterator nodeiterate = comments.getNodes();
    assertEquals(3, nodeiterate.getSize());
    int i = 1;
    
    while (nodeiterate.hasNext()) {         
      Node node = nodeiterate.nextNode();                 
      check(i, node);
      i++;
    }    
    assertNotNull(comments);    
  }
  
  private void check(int i, Node node) throws PathNotFoundException, RepositoryException {
    switch (i) {
    case 1:      
      assertEquals("root", node.getProperty(commentService_.COMMENTOR).getString());
      assertEquals("xxx5669@yahoo.com", node.getProperty(commentService_.COMMENTOR_EMAIL).getString());
      assertEquals("Cam on nha", node.getProperty(commentService_.MESSAGE).getString());
      break;      
    case 2:
      assertEquals("john", node.getProperty(commentService_.COMMENTOR).getString());
      assertEquals("xxx5668@yahoo.com", node.getProperty(commentService_.COMMENTOR_EMAIL).getString());
      assertEquals("thanks", node.getProperty(commentService_.MESSAGE).getString());
      break;
    case 3:
      assertEquals(commentService_.ANONYMOUS, node.getProperty(commentService_.COMMENTOR).getString());
      assertEquals("xxx5667@yahoo.com", node.getProperty(commentService_.COMMENTOR_EMAIL).getString());
      assertEquals("thanks alot", node.getProperty(commentService_.MESSAGE).getString());
      break;
    default:
      break;
    }      
  }
  
}
