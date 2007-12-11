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
  
  public String getRelativePath();  
  public List<Query> getQueries(String userName, String repository,SessionProvider provider) throws Exception;  
  public QueryResult execute (String queryPath, String workspace, String repository,SessionProvider provider) throws Exception;
  
  public void addQuery(String queryName, String statement, String language, String userName, String repository) throws Exception;  
  public void removeQuery(String queryPath, String userName, String repository) throws Exception;  
  public void addSharedQuery(String queryName, String statement, String language, String[] permissions, boolean cachedResult, String repository) throws Exception;
  
  public Node getSharedQuery(String queryName, String repository, SessionProvider provider) throws Exception ;     
  public void removeSharedQuery(String queryName, String repository) throws Exception;  
  public List<Node> getSharedQueries( String repository,SessionProvider provider) throws Exception ;  
  public Query getQueryByPath(String queryPath, String userName, String repository, SessionProvider provider) throws Exception ;
  
  public List<Node> getSharedQueries(String userId,String repository,SessionProvider provider) throws Exception ;  
  public List<Node> getSharedQueries(String queryType, String userId,String repository,SessionProvider provider) throws Exception ;
  
  public void init(String repository) throws Exception ;
}
