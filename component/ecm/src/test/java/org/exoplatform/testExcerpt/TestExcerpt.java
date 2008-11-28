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
package org.exoplatform.testExcerpt;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.ecm.BaseECMTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Aug 8, 2008  
 */
public class TestExcerpt extends BaseECMTestCase {
  private String s1 = "Additionally there is a parameter that controls the format of the excerpt created. " +
      "In JCR 1.9 the default is set to" + 
      "org.exoplatform.services.jcr.impl.core.query.lucene.DefaultHTMLExcerpt. " +
      "the configuration parameter for this setting is:" +
      "This is the test for Excerpt query";
  private String s2 = "It is a test for Excerpt query." + 
  "Searching with synonyms is integrated in the jcr:contains() function and uses the same syntax " +
  "like synonym searches with Google. If a search term is prefixed with ~ also synonyms of the search term are considered. Example:";
  private String s3 = "JCR supports such features as Lucene Fuzzy Searches";
  
  public void testExcerpt() throws Exception {
    System.out.println("\n\n----------Test Excerpt");
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();    
    
    Node node1 = root.addNode("Node1", "exo:article");
    node1.setProperty("exo:title", "Node1");
    node1.setProperty("exo:text", s1);
    
    Node node2 = root.addNode("Node2", "exo:article");
    node2.setProperty("exo:title", "Node2");
    node2.setProperty("exo:text", s2);
    
    Node node3 = root.addNode("Node3", "exo:article");
    node3.setProperty("exo:title", "Node3");
    node3.setProperty("exo:text", s3);
    
    session.save();
    
    System.out.println("\n\n-----Test with SQL");
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query q1 = queryManager.createQuery("select excerpt(.) from exo:article where contains(., 'test')", Query.SQL);
    QueryResult result1 = q1.execute();
    for (RowIterator it = result1.getRows(); it.hasNext();) {
      Row r = it.nextRow();
      Value excerpt = r.getValue("rep:excerpt(.)");
      System.out.println("\n\n---" + excerpt.getString());
    }
    
    System.out.println("\n\n-----Test with XPATH");
    Query q2 = queryManager.createQuery("//*[jcr:contains(., 'test')]/(@exo:text|rep:excerpt(.))", Query.XPATH);
    QueryResult result2 = q2.execute();
    for (RowIterator it = result2.getRows(); it.hasNext();) {
      Row r = it.nextRow();
      Value excerpt = r.getValue("rep:excerpt(.)");
      System.out.println("\n\n---" + excerpt.getString());
    }
  }
}
