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
package org.exoplatform.services.cms.timeline.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.timeline.TimelineService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 22, 2009  
 * 8:19:51 AM
 */
public class TimelineServiceImpl implements TimelineService {

  private static final Log LOG = ExoLogger.getLogger("cms.timeline.TimelineServiceImpl");
  private static final String EXO_DATE_MODIFIED = "exo:dateModified";
  private static final String SELECT_QUERY = "SELECT * FROM nt:base WHERE ";
  private RepositoryService repositoryService_;
  private TemplateService templateService_;
  
  public TimelineServiceImpl(RepositoryService repoService, TemplateService templateService
      ) throws Exception {
    repositoryService_ = repoService;
    templateService_ = templateService;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOfEarlierThisMonth(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOfEarlierThisWeek(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOfEarlierThisYear(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOfToday(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception {
    List<Node> documentsOfToday = new ArrayList<Node>();
    Session session = getSession(sessionProvider, repository, workspace);
    SimpleDateFormat formatDateTime = new SimpleDateFormat();
    formatDateTime.applyPattern("yyyy-MM-dd");
    String currentDate = formatDateTime.format((new GregorianCalendar()).getTime());
    StringBuilder sb = new StringBuilder();
    sb.append(SELECT_QUERY)
      .append("(" + buildDocumentTypePattern(repository) + ")")
      .append(" AND ")
      .append(" CONTAINS("+ EXO_DATE_MODIFIED +", '" + currentDate + "')");
    QueryResult result = executeQuery(session, sb.toString(), Query.SQL);
    NodeIterator nodeIter = result.getNodes();
    while(nodeIter.hasNext()) {
      documentsOfToday.add(nodeIter.nextNode());
    }
    return documentsOfToday;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOfYesterday(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Create a query statement
   * @param userName Logged in user
   * @return String
   */
  private String prepareQueryStatement(String userName) {
    return null;
  }
  
  private Session getSession(SessionProvider sessionProvider, String repository, String workspace
      ) throws RepositoryException, RepositoryConfigurationException {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    return sessionProvider.getSession(workspace, manageableRepository);
  }
  
  private QueryResult executeQuery(Session session, String statement, String language
      ) throws Exception {
    try {
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(statement, language);
      return query.execute();
    } catch(Exception e) {
      LOG.error("Can not execute query", e);
      return null;
    }          
  }

  private String buildDocumentTypePattern(String repository) throws Exception {
    List<String> documentFileTypes = templateService_.getAllDocumentNodeTypes(repository);
    StringBuilder sb = new StringBuilder();
    for(String documentType : documentFileTypes) {
      if(sb.length() > 0) sb.append(" OR ");
      sb.append("jcr:primaryType='"+documentType+"'");
    }
    return sb.toString();
  }
}
