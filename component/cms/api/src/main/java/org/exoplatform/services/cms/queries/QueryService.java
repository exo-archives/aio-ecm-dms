package org.exoplatform.services.cms.queries;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

public interface QueryService {
  
  public String getRelativePath();
  
  public List<Query> getQueries(String userName, String repository) throws Exception;
  
  //public QueryResult execute (String queryPath, String repository) throws Exception;
  public QueryResult execute (String queryPath, String workspace, String repository) throws Exception;
  
  public void addQuery(String queryName, String statement, String language, String userName, String repository) throws Exception;
  
  public void removeQuery(String queryPath, String userName, String repository) throws Exception;
  
  public void addSharedQuery(String queryName, String statement, String language, String[] permissions, boolean cachedResult, String repository) throws Exception;
  
  public Node getSharedQuery(String queryName, String repository) throws Exception ;
  
  public List<Node> getSharedQueries(String queryType, List permissions, String repository) throws Exception ;
  
  public List<Node> getSharedQueriesByPermissions(List permissions, String repository) throws Exception ;
  
  public void removeSharedQuery(String queryName, String repository) throws Exception;
  
  public List<Node> getSharedQueries( String repository) throws Exception ;
  
  public void init(String repository) throws Exception ;
}
