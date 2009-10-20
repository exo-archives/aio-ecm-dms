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
package org.exoplatform.services.cms.documents.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.documents.DocumentTypeService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh
 * minh.dang@exoplatform.com Oct 6, 2009 3:39:28 AM
 */
public class DocumentTypeServiceImpl implements DocumentTypeService {
  private static final Log    LOG           = ExoLogger
                                                .getLogger("cms.documents.DocumentTypeServiceImpl");

  private final static String         OWNER         = "exo:owner".intern();

  private final static String         QUERY         = " SELECT * FROM nt:resource WHERE";

  private final static String JCR_MINE_TYPE = "jcr:mimeType".intern();
  
  private final static String LANGUAGE      = "sql";
    
  private final static String AND         = " AND ";
  
  private final static String CONTAINS    = " contains";
  
  private final static String SINGLE_QUOTE    = "'";
  
  private final static String OR         = " OR ";
  
  private final static String BEGIN_BRANCH         = " ( ";
  
  private final static String END_BRANCH         = " ) ";
      
  private RepositoryService   repositoryService_ ;
        
  public DocumentTypeServiceImpl(RepositoryService repoService) {
    repositoryService_ = repoService;
  }
      
  public List<Node> getAllDocumentsByType(String workspace, String repository,
      SessionProvider sessionProvider, String mimeType) throws Exception {
    
    String[] mimeTypes = { mimeType };
    
    // Get all document types
    return getAllDocumentsByType(workspace,repository,sessionProvider,mimeTypes);
  }

  public List<Node> getAllDocumentsByUser(String workspace, String repository,
      SessionProvider sessionProvider, String[] mimeTypes, String userName) throws Exception {
    Session session = sessionProvider.getSession(workspace,
                                                    repositoryService_.getRepository(repository));
    List<Node> resultList = new ArrayList<Node>();
    QueryResult results = null;
    try {
      results = executeQuery(session, parseQuery(mimeTypes, userName.trim()), LANGUAGE);
    } catch(Exception e) {
      LOG.error(e.getMessage());
    }
    NodeIterator iterator  = results.getNodes();  
    Node documentNode = null;
    while (iterator.hasNext()) {
      documentNode = iterator.nextNode();
      
      // Add a node is nt:resource type to list
      resultList.add(documentNode);
    }
    
    // Return list of document nodes 
    return resultList;
  }

  public List<Node> getAllDocumentsByType(String workspace, String repository,
      SessionProvider sessionProvider, String[] mimeTypes) throws Exception {
    Session session = sessionProvider.getSession(workspace,repositoryService_.getRepository(repository));
    
    List<Node> resultList = new ArrayList<Node>();
    QueryResult results = null;
    try {
      
      // Execute sql query and return a results
      results = executeQuery(session, parseQuery(mimeTypes, null), LANGUAGE);
    } catch(Exception e) {
      LOG.error(e.getMessage());
    }    
    NodeIterator iterator  = results.getNodes();
    Node documentNode = null;
    while (iterator.hasNext()) {
      
      // Get a node which is nt:resource type
      documentNode = iterator.nextNode();
      
      // Add a node is nt:resource type to list
      resultList.add(documentNode);
    }
    
    // Return list of nodes
    return resultList;
     
  }

  private QueryResult executeQuery(Session session, String statement, String language)
      throws Exception, RepositoryException {
    try {
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(statement, language);
      return query.execute();
    } catch(Exception e) {
      throw new Exception("SQL query fail",e);
    }          
  }

  private String parseQuery(String[] mimeTypes, String user) throws Exception {
    StringBuilder query = new StringBuilder();
    query.append(QUERY);    
    
    if (user == null) {
      for (int index=0; index < mimeTypes.length; index++) {
        query.append(CONTAINS).append("(").append(JCR_MINE_TYPE).append(",").append(SINGLE_QUOTE)
             .append(mimeTypes[index].trim()).append(SINGLE_QUOTE).append(")");
        
        // Append OR operator for next branch statement   
        if (index < mimeTypes.length-1) 
          query.append(OR);                              
      }
    } else {      
      query.append(CONTAINS).append("(").append(OWNER).append(",").append(SINGLE_QUOTE)
           .append(user).append(SINGLE_QUOTE).append(")")
           .append(AND);
      query.append(BEGIN_BRANCH);
      for (int index=0; index < mimeTypes.length; index++) {        
        query.append(CONTAINS).append("(").append(JCR_MINE_TYPE).append(",").append(SINGLE_QUOTE)
             .append(mimeTypes[index].trim()).append(SINGLE_QUOTE).append(")");
        
        // Append OR operator for next branch statement
        if (index < mimeTypes.length-1) { 
          query.append(OR);
        } else {
          query.append(END_BRANCH);
        } 
      }      
    }
    
    // Return query statement.
    return query.toString();
  }
}
