/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.cms.queries;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface QueryService {

  /**
   * Get the relative path
   * 
   * @return
   */
  public String getRelativePath();

  /**
   * Get queries by giving the following params : userName, repository, provider
   * 
   * @param userName String Can be <code>null</code>
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return queries List<Query>
   * @see Query
   * @see SessionProvider
   * @throws Exception
   */
  public List<Query> getQueries(String userName, String repository, SessionProvider provider)
      throws Exception;

  /**
   * Execute query by giving the following params : queryPath, workspace,
   * repository, provider, userId
   * 
   * @param queryPath String The path of query
   * @param workspace String The name of workspace
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @param userId String The id of current user
   * @return queries QueryResult
   * @see QueryResult
   * @see SessionProvider
   * @throws Exception
   */
  public QueryResult execute(String queryPath, String workspace, String repository,
      SessionProvider provider, String userId) throws Exception;

  /**
   * Add new query by giving the following params : queryName, statement,
   * language, userName, repository
   * 
   * @param queryName String The name of query
   * @param statement String The statement query
   * @param language String The language is requested
   * @param userName String Can be <code>null</code>
   * @param repository String The name of repository
   * @throws Exception
   */
  public void addQuery(String queryName, String statement, String language, String userName,
      String repository) throws Exception;

  /**
   * Remove query by giving the following params : queryPath, userName,
   * repository
   * 
   * @param queryPath String The path of query
   * @param userName String Can be <code>null</code>
   * @param repository String The name of repository
   * @throws Exception
   */
  public void removeQuery(String queryPath, String userName, String repository) throws Exception;

  /**
   * Add new shared query by giving the following params: queryName, statement,
   * language, permissions, cachedResult, repository
   * 
   * @param queryName String The name of query
   * @param statement String The statement query
   * @param language String The language is requested
   * @param permissions String[]
   * @param cachedResult boolean Choosen for caching results
   * @param repository String The name of repository
   * @throws Exception
   */
  public void addSharedQuery(String queryName, String statement, String language,
      String[] permissions, boolean cachedResult, String repository) throws Exception;
  
  /**
   * Add new shared query by giving the following params: queryName, statement,
   * language, permissions, cachedResult, repository
   * 
   * @param queryName String The name of query
   * @param statement String The statement query
   * @param language String The language is requested
   * @param permissions String[]
   * @param cachedResult boolean Choosen for caching results
   * @param repository String The name of repository
   * @param provider Session provider
   * @throws Exception
   */
  public void addSharedQuery(String queryName, String statement, String language,
      String[] permissions, boolean cachedResult, String repository, SessionProvider provider) throws Exception;  

  /**
   * Get shared queries by giving the following params : userId, repository,
   * provider
   * 
   * @param userId String The id of current user
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return sharedQueries List<Node>
   * @see Node
   * @see SessionProvider
   * @throws Exception
   */
  public Node getSharedQuery(String queryName, String repository, SessionProvider provider)
      throws Exception;

  /**
   * Remove share query by giving the following params : queryName, repository
   * 
   * @param queryName String The name of query
   * @param repository String The name of repository
   * @throws Exception
   */
  public void removeSharedQuery(String queryName, String repository) throws Exception;

  /**
   * Get shared queries by giving the following params : repository, provider
   * 
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return sharedQueries List<Node>
   * @see Node
   * @see SessionProvider
   * @throws Exception
   */
  public List<Node> getSharedQueries(String repository, SessionProvider provider) throws Exception;

  /**
   * Get query with path by giving the following params : queryPath, userName,
   * repository, provider
   * 
   * @param queryPath String The path of query
   * @param userName String The name of current user
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return query Query
   * @see Node
   * @see Query
   * @see SessionProvider
   * @throws Exception
   */
  public Query getQueryByPath(String queryPath, String userName, String repository,
      SessionProvider provider) throws Exception;

  /**
   * Get shared queries by giving the following params : userId, repository,
   * provider
   * 
   * @param userId String The id of current user
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return sharedQueries List<Node>
   * @see Node
   * @see SessionProvider
   * @throws Exception
   */
  public List<Node> getSharedQueries(String userId, String repository, SessionProvider provider)
      throws Exception;

  /**
   * Get shared queries by giving the following params : queryType, userId,
   * repository, provider
   * 
   * @param queryType String The type of query
   * @param userId String The id of current user
   * @param repository String The name of repository
   * @param provider SessionProvider
   * @return sharedQueries List<Node>
   * @see Node
   * @see SessionProvider
   * @throws Exception
   */
  public List<Node> getSharedQueries(String queryType, String userId, String repository,
      SessionProvider provider) throws Exception;

  /**
   * Init all query plugin by giving the following params : repository
   * 
   * @param repository String The name of repository
   * @see QueryPlugin
   * @throws Exception
   */
  public void init(String repository) throws Exception;
}
