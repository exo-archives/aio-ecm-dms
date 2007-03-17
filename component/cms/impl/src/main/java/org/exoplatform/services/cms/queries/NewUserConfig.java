 /**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.queries;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Mestrallet
 * benjamin.mestrallet@exoplatform.com
 */
public class NewUserConfig {    
  private String template;  
  private List users = new ArrayList(5);
  
  public String getTemplate() { return template; }
  public void setTemplate(String template) { this.template = template; }
    
  public List getUsers() {   return users; }
  public void setUsers(List s) {  this.users = s; }  
    
  static public class User {
    private String userName ;
    private List  queries ;
    
    public User() {
      queries = new ArrayList(5) ;
    }
        
    public String getUserName() {   return userName;  }
    public void setUserName(String userName) {  this.userName = userName;   }

    public List getQueries() {
      return queries;
    }

    public void setQueries(List queries) {
      this.queries = queries;
    }
  }
  
  static public class Query {
    private String queryName ;
    private String language;
    private String query;
    
    public String getQuery() {
      return query;
    }
    public void setQuery(String query) {
      this.query = query;
    }    
    public String getLanguage() {
      return language;
    }
    public void setLanguage(String language) {
      this.language = language;
    }
    public String getQueryName() {
      return queryName;
    }
    public void setQueryName(String queryName) {
      this.queryName = queryName;
    }
  }
}
