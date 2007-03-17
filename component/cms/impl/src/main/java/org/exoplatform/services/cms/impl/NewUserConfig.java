 /**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.impl;

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
    private List  referencedFiles ;
    
    public User() {
      referencedFiles = new ArrayList(5) ;
    }
        
    public String getUserName() {   return userName;  }
    public void setUserName(String userName) {  this.userName = userName;   }
    
    public List getReferencedFiles() {  return referencedFiles;   }
    public void setReferencedFiles(List referencedFiles) {  this.referencedFiles = referencedFiles;  }    
  }  
}
