/**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cms.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class NewUserListener extends UserEventListener {
  
  private static final String[] perms = {PermissionType.READ, PermissionType.ADD_NODE, 
    PermissionType.SET_PROPERTY, PermissionType.REMOVE };
  
  private NewUserConfig config_;

  private ConfigurationManager cservice_;

  private Collection localeConfigs_;

  private RepositoryService jcrService_;

  private Log log_;

  private CmsConfigurationService cmsConfigurationService_;

  private String relativePath_;

  public NewUserListener(ConfigurationManager cservice,
                         LocaleConfigService localeService,
                         RepositoryService jcrService,
                         CmsConfigurationService cmsConfigurationService, 
                         LogService logService,
                         InitParams params)    throws Exception {
    cservice_ = cservice;
    jcrService_ = jcrService;
    cmsConfigurationService_ = cmsConfigurationService;
    log_ = logService.getLog(getClass().getName());
    config_ = (NewUserConfig) params.getObjectParamValues(NewUserConfig.class).get(0);
    localeConfigs_ = localeService.getLocalConfigs();
    relativePath_ = params.getValueParam("relativePath").getValue();
  }
  
  public void preSave(User user, boolean isNew)
      throws Exception {        
    String userName = user.getUserName();

    prepareSystemWorkspace(userName);
    prepareWorkpsace(cmsConfigurationService_.getDraftWorkspace(), userName);
    prepareWorkpsace(cmsConfigurationService_.getBackupWorkspace(), userName);
  }
  
  private void prepareSystemWorkspace(String userName) throws Exception {
    ManageableRepository jcrRepository = jcrService_.getRepository();
    Session session = null;    
    //Manage production workspace
     try {
       session = jcrRepository.getSystemSession(cmsConfigurationService_
           .getWorkspace());
     } catch (RepositoryException re){
       return;
     }      
     Node usersHome = (Node) session.getItem(
         cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));
     
     Node siteHome = Utils.makePath(usersHome,
                                    userName + "/" + relativePath_,
                                    "nt:unstructured",
                                    getPermissions(userName));
     
     List users = config_.getUsers();
     boolean userFound = false;
     NewUserConfig.User templateConfig = null;
     for (Iterator iter = users.iterator(); iter.hasNext();) {
       NewUserConfig.User userConfig = (NewUserConfig.User) iter.next();
       String currentName = userConfig.getUserName();            
       if (config_.getTemplate().equals(currentName))  templateConfig = userConfig;
       if (currentName.equals(userName)) {
         List files = userConfig.getReferencedFiles();
         importLocalisedContent(siteHome, currentName, files);
         userFound = true;
         break;
       }
     }
     if (!userFound && templateConfig != null) {
       List files = templateConfig.getReferencedFiles();
       importLocalisedContent(siteHome, templateConfig.getUserName(), files);
     }
     usersHome.save();    
  }
  
  private void prepareWorkpsace(String workspace, String userName) throws Exception {
    Session session = null;
    try {
      ManageableRepository jcrRepository = jcrService_.getRepository();
      session = jcrRepository.getSystemSession(workspace);
    } catch (RepositoryException re){
      return;
    }     
    Node usersHome = (Node) session.getItem(
        cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));    
    
    Node siteHome = Utils.makePath(usersHome,
                                   userName + "/" + relativePath_,
                                   "nt:unstructured",
                                   getPermissions(userName));
    
    for (Iterator iterator = localeConfigs_.iterator(); iterator.hasNext();) {
      LocaleConfig localeConfig = (LocaleConfig) iterator.next();
      String locale = localeConfig.getLanguage();
      if(!siteHome.hasNode(locale)){
        siteHome.addNode(locale);
      }
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
  
  private void importLocalisedContent(Node siteHome, String userName, List files) throws Exception{
    for (Iterator iterator = localeConfigs_.iterator(); iterator.hasNext();) {
      LocaleConfig localeConfig = (LocaleConfig) iterator.next();
      String locale = localeConfig.getLanguage();
      Node localisedNode = siteHome.hasNode(locale) ?
                           siteHome.getNode(locale) :
                           siteHome.addNode(locale);
      
      importInJCR(files, localisedNode, locale, userName);
    }    
  }

  private void importInJCR(List files, Node localizedHome, String locale, String userName) {
    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
      String file = (String) iterator.next();
      String folderPath = file.substring(0, file.lastIndexOf("/"));
      if ("".equals(folderPath)) folderPath = "/" ;
      else if(!"/".equals(folderPath.substring(0,0))) folderPath = "/" + folderPath ;
      String fileName = file.substring(file.lastIndexOf("/") + 1);
      String warPath = cmsConfigurationService_.getContentLocation()
            + cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH) + "/" + userName  
//            + "/" + locale + "/" + file;
      			+ "/" + locale + folderPath + fileName;
      try {
        InputStream iS = cservice_.getInputStream(warPath);
        Node parentFileNode = Utils.makePath(localizedHome, folderPath, "nt:folder");  
        Node realFileNode = parentFileNode.addNode(fileName, "nt:file");
        Node contentNode = realFileNode.addNode("jcr:content", "nt:resource");
        contentNode.setProperty("jcr:encoding", "UTF-8");
        contentNode.setProperty("jcr:data", iS);
        contentNode.setProperty("jcr:mimeType", "text/html");
        contentNode.setProperty("jcr:lastModified", new GregorianCalendar(new Locale(locale)));
      } catch (Exception e1) {
        //means the version of the file for that locale does not exist
      }
    }
  }

  public void preDelete(User user) {
    Session session;
    try {
      //use a anonymous connection for the configuration as the user is not
      // authentified at that time
      Repository jcrRepository = jcrService_.getRepository();
      
      //Manage production workspace
      session = jcrRepository.login();
      Node usersHome = (Node) session.getItem(
          cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));
      usersHome.getNode(user.getUserName()).remove();
      usersHome.save();
      
      //Manage draft workspace
      session = jcrRepository.login(cmsConfigurationService_.getDraftWorkspace());
      usersHome = (Node) session.getItem(
          cmsConfigurationService_.getJcrPath(BasePath.CMS_USERS_PATH));
      usersHome.getNode(user.getUserName()).remove();
      usersHome.save();                  
    } catch (PathNotFoundException ex) {
      log_.info("Can not delete home dir of user " + user.getUserName());
    } catch (RepositoryException e) {
      log_.error("RepositoryException while trying to delete a user home dir", e);
    } catch (RepositoryConfigurationException e) {
      log_.error("RepositoryException while trying to delete a user home dir", e);
    }
  }
}