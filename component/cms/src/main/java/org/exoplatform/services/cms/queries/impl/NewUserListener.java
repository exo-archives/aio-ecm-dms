/**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.queries.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class NewUserListener extends UserEventListener {
  
  private static final String[] perms = {PermissionType.READ, PermissionType.ADD_NODE, 
    PermissionType.SET_PROPERTY, PermissionType.REMOVE };  
  
  private NewUserConfig config_;
  private RepositoryService jcrService_;
  private CmsConfigurationService cmsConfigurationService_;
  private String relativePath_;

  public NewUserListener(RepositoryService jcrService,
                         CmsConfigurationService cmsConfigurationService, 
                         InitParams params)    throws Exception {
    jcrService_ = jcrService;
    cmsConfigurationService_ = cmsConfigurationService;
    config_ = (NewUserConfig) params.getObjectParamValues(NewUserConfig.class).get(0);
    relativePath_ = params.getValueParam("relativePath").getValue();
  }
  
  public void preSave(User user, boolean isNew)
      throws Exception {        
    String userName = user.getUserName();
    prepareSystemWorkspace(userName);
  }
  
  private void prepareSystemWorkspace(String userName) throws Exception {
    Session session = null;    
    //Manage production workspace
    List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
    for(RepositoryEntry repo : repositories) {
      try {
        session = jcrService_.getRepository(repo.getName()).getSystemSession(cmsConfigurationService_
            .getWorkspace(repo.getName()));
        Node usersHome = (Node) session.getItem(
            cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));
        initSystemData(usersHome, userName) ;
        session.save();
        session.logout();
      } catch (RepositoryException re){
        session.logout();
        return;
      }
    }   
  }
  
  private void initSystemData(Node usersHome, String userName) throws Exception{           
    Node userHome = Utils.makePath(usersHome, userName, "nt:unstructured", getPermissions(userName));
    if(userHome.hasNode(relativePath_)) {    
      return;
    }     
    Node queriesHome =  Utils.makePath(userHome, relativePath_, "nt:unstructured", getQueryPermissions(userName));           
    boolean userFound = false;
    NewUserConfig.User templateConfig = null;
    for (NewUserConfig.User userConfig : config_.getUsers()) {      
      String currentName = userConfig.getUserName();            
      if (config_.getTemplate().equals(currentName))  templateConfig = userConfig;
      if (currentName.equals(userName)) {
        List<NewUserConfig.Query> queries = userConfig.getQueries();
        importQueries(queriesHome, queries);
        userFound = true;
        break;
      }
    }
    if (!userFound) {
      //use template conf
      List<NewUserConfig.Query> queries = templateConfig.getQueries();
      importQueries(queriesHome, queries);
    }
    usersHome.save();   
  }
  public Map getPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);         
    permissions.put("*:/admin", perms);
    return permissions;
  }
  
  public Map getQueryPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);         
    permissions.put("*:/admin", perms);
    return permissions;
  }  
  
  public void importQueries(Node queriesHome, List<NewUserConfig.Query> queries) throws Exception {
    QueryManager manager = queriesHome.getSession().getWorkspace().getQueryManager();
    for (NewUserConfig.Query query:queries) {      
      String queryName = query.getQueryName();
      String language = query.getLanguage();
      String statement = query.getQuery();
      Query queryNode = manager.createQuery(statement, language);
      String absPath = queriesHome.getPath() + "/" + queryName;
      queryNode.storeAsNode(absPath);
    }    
  }

  public void preDelete(User user) {
  }
}