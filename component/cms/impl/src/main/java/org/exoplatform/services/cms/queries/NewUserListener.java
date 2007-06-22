/**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.queries;

import java.util.HashMap;
import java.util.Iterator;
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
        initSystemData(session, userName) ;
      } catch (RepositoryException re){
        return;
      }
    }   
  }
  
  private void initSystemData(Session session, String userName) throws Exception{
    Node usersHome = (Node) session.getItem(
        cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));   
    
    Node userHome = Utils.makePath(usersHome, userName, "nt:unstructured", getPermissions(userName));
    if(userHome.hasNode(relativePath_)) {
      session.refresh(false);
      return;
    }     
    Node queriesHome =  Utils.makePath(userHome, relativePath_, "nt:unstructured", getQueryPermissions(userName)); 
      
    List users = config_.getUsers();
    boolean userFound = false;
    NewUserConfig.User templateConfig = null;
    for (Iterator iter = users.iterator(); iter.hasNext();) {
      NewUserConfig.User userConfig = (NewUserConfig.User) iter.next();
      String currentName = userConfig.getUserName();            
      if (config_.getTemplate().equals(currentName))  templateConfig = userConfig;
      if (currentName.equals(userName)) {
        List queries = userConfig.getQueries();
        importQueries(queriesHome, queries);
        userFound = true;
        break;
      }
    }
    if (!userFound) {
      //use template conf
      List queries = templateConfig.getQueries();
      importQueries(queriesHome, queries);
    }
    usersHome.save();   
  }
  public Map getPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);     
    permissions.put("any", new String[] {PermissionType.READ});
    permissions.put("*:/admin", perms);
    return permissions;
  }
  
  public Map getQueryPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);         
    permissions.put("*:/admin", perms);
    return permissions;
  }  
  
  public void importQueries(Node queriesHome, List queries) throws Exception {
    QueryManager manager = queriesHome.getSession().getWorkspace().getQueryManager();
    for (Iterator iter = queries.iterator(); iter.hasNext();) {
      NewUserConfig.Query query = (NewUserConfig.Query) iter.next();
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