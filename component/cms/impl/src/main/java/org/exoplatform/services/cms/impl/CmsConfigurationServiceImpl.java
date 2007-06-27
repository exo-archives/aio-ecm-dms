/*
 * Created on Mar 18, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.ext.ReDefineNodeTypePlugin;
import org.exoplatform.services.cms.ext.SuperTypeConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class CmsConfigurationServiceImpl implements CmsConfigurationService,
    Startable {

  private RepositoryService jcrService_;

  List<AddPathPlugin> pathPlugins_ = new ArrayList<AddPathPlugin>();
  private List<ReDefineNodeTypePlugin> nodeTypePlugins_ = new ArrayList<ReDefineNodeTypePlugin>();

  private PropertiesParam propertiesParam_;

  public CmsConfigurationServiceImpl(InitParams params,
      RepositoryService jcrService) throws Exception {
    jcrService_ = jcrService;
    propertiesParam_ = params.getPropertiesParam("cms.configuration");
  }

  public void start() {
    try {
      String repository = jcrService_.getDefaultRepository().getConfiguration().getName() ;
      processNodeTypePlugin(repository) ;      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    try {
      processAddPathPlugin() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
  }

  public void init(String repository) throws Exception {
    processNodeTypePlugin(repository) ;
    initBasePath(repository) ;
  }
  
  private void processNodeTypePlugin(String repository) throws Exception {
    ExtendedNodeTypeManager nodeTypeManager = jcrService_.getRepository(repository).getNodeTypeManager() ;
    for(ReDefineNodeTypePlugin plugin:nodeTypePlugins_) {
      SuperTypeConfig config = plugin.getAddSuperTypeConfig() ;
      String sourceTypeName = config.getSourceNodeType() ;
      NodeType sourceNodeType = nodeTypeManager.getNodeType(sourceTypeName) ;
      List<String> targetedTypeNames = config.getTargetedNodeTypes() ;      
      for(String name:targetedTypeNames) {
        ExtendedNodeType extNodeType = (ExtendedNodeType)nodeTypeManager.getNodeType(name) ;
        NodeType[] declaredSuperTypes = extNodeType.getDeclaredSupertypes() ;        
        List<NodeType> temp = new ArrayList<NodeType>() ;
        temp.add(sourceNodeType) ;
        temp.addAll(Arrays.<NodeType>asList(declaredSuperTypes)) ;
        extNodeType.setDeclaredSupertypes(temp.toArray(new NodeType[temp.size()])) ;
      }
    }    
  }

  private void processAddPathPlugin()  throws Exception {           
    Map<String, String[]> permissions = null;    
    for (int j = 0; j < pathPlugins_.size(); j++) {
      CmsConfig config = pathPlugins_.get(j).getPaths();
      List jcrPaths = config.getJcrPaths();
      for (Iterator iter = jcrPaths.iterator(); iter.hasNext();) {
        CmsConfig.JcrPath jcrPath = (CmsConfig.JcrPath) iter.next();
        List workspaces = jcrPath.getWorkspaces();
        for (Iterator iterator = workspaces.iterator(); iterator.hasNext();) {
          CmsConfig.Workspace workspace = (CmsConfig.Workspace) iterator.next();
          Session session = jcrService_.getRepository(config.getRepository())
                                       .getSystemSession(workspace.getName());
          permissions = new HashMap<String, String[]>();
          List perms = workspace.getPermissions();
          for (Iterator iterator2 = perms.iterator(); iterator2.hasNext();) {
            CmsConfig.Permission perm = (CmsConfig.Permission) iterator2.next();
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
            permissions.put(perm.getOwner(), permsList.toArray(permsArray));
          }
          Utils.makePath(session.getRootNode(), jcrPath.getPath(),
              "nt:unstructured", permissions);
          session.save();
          session.logout() ;
        }
      }
    }
  }
  
  private void initBasePath(String repository) throws Exception {
    Map<String, String[]> permissions = null;  
    String defaultRepository = jcrService_.getDefaultRepository().getConfiguration().getName() ;
    String defaultSystemWorkspace = getWorkspace(defaultRepository);
    for (int j = 0; j < pathPlugins_.size(); j++) {
      CmsConfig config = pathPlugins_.get(j).getPaths();
      List jcrPaths = config.getJcrPaths();
      for (Iterator iter = jcrPaths.iterator(); iter.hasNext();) {
        CmsConfig.JcrPath jcrPath = (CmsConfig.JcrPath) iter.next();
        List workspaces = jcrPath.getWorkspaces();
        for (Iterator iterator = workspaces.iterator(); iterator.hasNext();) {
          CmsConfig.Workspace workspace = (CmsConfig.Workspace) iterator.next();
          if(workspace.getName().equals(defaultSystemWorkspace)) {
            Session session = jcrService_.getRepository(repository)
            .getSystemSession(getWorkspace(repository));
            permissions = new HashMap<String, String[]>();
            List perms = workspace.getPermissions();
            for (Iterator iterator2 = perms.iterator(); iterator2.hasNext();) {
              CmsConfig.Permission perm = (CmsConfig.Permission) iterator2.next();
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
              permissions.put(perm.getOwner(), permsList.toArray(permsArray));
            }
            Utils.makePath(session.getRootNode(), jcrPath.getPath(),
            "nt:unstructured", permissions);
            session.save();
            session.logout() ;
          }
        }
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
    else if(plugin instanceof ReDefineNodeTypePlugin) nodeTypePlugins_.add((ReDefineNodeTypePlugin)plugin) ;
  }

  @SuppressWarnings("unused")
  public ComponentPlugin removePlugin(String name) {
    return null;
  }

  public Collection getPlugins() {
    return null;
  }

}