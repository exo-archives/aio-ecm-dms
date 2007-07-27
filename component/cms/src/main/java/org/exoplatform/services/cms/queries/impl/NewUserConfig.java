 /**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.queries.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Mestrallet
 * benjamin.mestrallet@exoplatform.com
 */
public class NewUserConfig {    
  private String repository ;
  private String template;  
  private List<User> users = new ArrayList<User>(5);
  
  public String getRepository() { return repository ; }
  public void setRepository(String rp) { this.repository = rp; }
  
  public String getTemplate() { return template; }
  public void setTemplate(String template) { this.template = template; }
    
  public List<User> getUsers() {   return users; }
  public void setUsers(List<User> s) {  this.users = s; }  
    
  static public class User {
    private String userName ;
    private List<Query>  queries = new ArrayList<Query>(5) ;
    
    public User() { }
        
    public String getUserName() {   return userName;  }
    public void setUserName(String userName) {  this.userName = userName;   }

    public List<Query> getQueries() { return queries; }
    public void setQueries(List<Query> queries) { this.queries = queries;  }
    
  }
  
  static public class Query {
    private String queryName ;
    private String language;
    private String query;
    
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
     
    public String getQueryName() { return queryName; }
    public void setQueryName(String queryName) { this.queryName = queryName; }
  }
}
