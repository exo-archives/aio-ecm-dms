package org.exoplatform.services.cms.queries;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

public interface QueryService {
  
  public String getRelativePath();
  
  public List<Query> getQueries(String userName) throws Exception;
  
  public Query getQuery(String queryPath) throws Exception;
  
  public QueryResult execute (String queryPath) throws Exception;
  
  public void addQuery(String queryName, String statement, String language, String userName) throws Exception;
  
  public void removeQuery(String queryPath, String userName) throws Exception;
  
  public void addSharedQuery(String queryName, String statement, String language, String[] permissions, boolean cachedResult) throws Exception;
  
  public Node getSharedQuery(String queryName) throws Exception ;
  
  public List<Node> getSharedQueries(String queryType, List permissions) throws Exception ;
  
  public List<Node> getSharedQueriesByPermissions(List permissions) throws Exception ;
  
  public void removeSharedQuery(String queryName) throws Exception;
  
  public List<Node> getSharedQueries() throws Exception ;
}
