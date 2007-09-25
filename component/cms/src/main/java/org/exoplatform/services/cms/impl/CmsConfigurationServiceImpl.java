/*
 * Created on Mar 18, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.CmsConfig.JcrPath;
import org.exoplatform.services.cms.impl.CmsConfig.Permission;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class CmsConfigurationServiceImpl implements CmsConfigurationService, Startable {

  final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;

  private RepositoryService jcrService_;

  List<AddPathPlugin> pathPlugins_ = new ArrayList<AddPathPlugin>();  

  private PropertiesParam propertiesParam_;

  public CmsConfigurationServiceImpl(InitParams params,
      RepositoryService jcrService) throws Exception {
    jcrService_ = jcrService;
    propertiesParam_ = params.getPropertiesParam("cms.configuration");
  }

  public void start() {    
    try {
      processAddPathPlugin() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
  }

  public void init(String repository) throws Exception {   
    initBasePath(repository) ;
  }  
  
  private void processAddPathPlugin()  throws Exception {           
    HashMap<String, String[]> permissions = new HashMap<String,String[]>();
    Session session = null ;
    for(AddPathPlugin pathPlugin:pathPlugins_) {
      CmsConfig cmsConfig = pathPlugin.getPaths() ;
      String repository = cmsConfig.getRepository() ;
      List<JcrPath> jcrPaths = cmsConfig.getJcrPaths() ;
      for(String workspaceName:cmsConfig.getWorkspaces()) {
        session = jcrService_.getRepository(repository).getSystemSession(workspaceName) ;
        Node rootNode = session.getRootNode() ;
        for(JcrPath jcrPath:jcrPaths) {                    
          permissions.clear() ;
          for(Permission perm:jcrPath.getPermissions()) {
            List<String> permsList = new ArrayList<String>();
            if ("true".equals(perm.getRead()))
              permsList.add(PermissionType.READ);
            if ("true".equals(perm.getAddNode()))
              permsList.add(PermissionType.ADD_NODE);
            if ("true".equals(perm.getSetProperty()))
              permsList.add(PermissionType.SET_PROPERTY);
            if ("true".equals(perm.getRemove()))
              permsList.add(PermissionType.REMOVE);
            String[] permsArray = new String[permsList.size()];
            permissions.put(perm.getIdentity(), permsList.toArray(permsArray));
          }                    
          String nodeType = jcrPath.getNodeType() ;
          if(nodeType == null || nodeType.length() == 0) {
            nodeType = NT_UNSTRUCTURED ;
          }
          Utils.makePath(rootNode, jcrPath.getPath(),nodeType, permissions);
        }
        session.save() ;
        session.logout() ;
      }
    }    
  }

  private void initBasePath(String repository) throws Exception {    
    HashMap<String, String[]> permissions = new HashMap<String,String[]>();
    Session session = null ;
    String defaultRepository = jcrService_.getDefaultRepository().getConfiguration().getName() ;
    String defaultSystemWorkspace = getWorkspace(defaultRepository);    
    for(AddPathPlugin pathPlugin:pathPlugins_) {
      CmsConfig cmsConfig = pathPlugin.getPaths() ;
      List<JcrPath> jcrPaths = cmsConfig.getJcrPaths() ;
      for(String workspaceName:cmsConfig.getWorkspaces()) {
        //only init data for system workspace as same with system workspace of default repository
        if(!workspaceName.equals(defaultSystemWorkspace)) continue ;
        session = jcrService_.getRepository(repository).getSystemSession(getWorkspace(repository)) ;
        Node rootNode = session.getRootNode() ;
        for(JcrPath jcrPath:jcrPaths) {                    
          permissions.clear() ;
          for(Permission perm:jcrPath.getPermissions()) {
            List<String> permsList = new ArrayList<String>();
            if ("true".equals(perm.getRead()))
              permsList.add(PermissionType.READ);
            if ("true".equals(perm.getAddNode()))
              permsList.add(PermissionType.ADD_NODE);
            if ("true".equals(perm.getSetProperty()))
              permsList.add(PermissionType.SET_PROPERTY);
            if ("true".equals(perm.getRemove()))
              permsList.add(PermissionType.REMOVE);
            String[] permsArray = new String[permsList.size()];
            permissions.put(perm.getIdentity(), permsList.toArray(permsArray));
          }                    
          String nodeType = jcrPath.getNodeType() ;
          if(nodeType == null || nodeType.length() == 0) {
            nodeType = NT_UNSTRUCTURED ;
          }
          Utils.makePath(rootNode, jcrPath.getPath(),nodeType, permissions);
        }
        session.save() ;
        session.logout() ;
      }
    }       
  }
  
  public String getWorkspace() {
    return propertiesParam_.getProperty("workspace");
  }

  public String getWorkspace(String repository) throws Exception{
    return jcrService_.getRepository(repository).getConfiguration().getSystemWorkspaceName() ;
  }

  public String getDraftWorkspace() {
    return propertiesParam_.getProperty("draft");
  }

  public String getBackupWorkspace() {
    return propertiesParam_.getProperty("backup");
  }

  public String getContentLocation() {
    return propertiesParam_.getProperty("contentLocation");
  }

  public String getJcrPath(String alias) {
    for (int j = 0; j < pathPlugins_.size(); j++) {
      CmsConfig config = pathPlugins_.get(j).getPaths();
      List jcrPaths = config.getJcrPaths();
      for (Iterator iter = jcrPaths.iterator(); iter.hasNext();) {
        CmsConfig.JcrPath jcrPath = (CmsConfig.JcrPath) iter.next();
        if (jcrPath.getAlias().equals(alias)) {
          return jcrPath.getPath();
        }
      }
    }
    return null;
  }

  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof AddPathPlugin) pathPlugins_.add((AddPathPlugin) plugin);    
  }

  @SuppressWarnings("unused")
  public ComponentPlugin removePlugin(String name) {
    return null;
  }

  public Collection getPlugins() {
    return null;
  }    

}