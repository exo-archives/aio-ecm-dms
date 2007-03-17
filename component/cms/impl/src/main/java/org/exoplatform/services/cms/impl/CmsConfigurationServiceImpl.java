/*
 * Created on Mar 18, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;

/**
 * @author benjaminmestrallet
 */
public class CmsConfigurationServiceImpl implements CmsConfigurationService,
    Startable {

  private RepositoryService jcrService_;

  List<AddPathPlugin> plugins_;

  private PropertiesParam propertiesParam_;

  public CmsConfigurationServiceImpl(InitParams params,
      RepositoryService jcrService) throws Exception {
    jcrService_ = jcrService;

    plugins_ = new ArrayList<AddPathPlugin>();

    propertiesParam_ = params.getPropertiesParam("cms.configuration");
  }

  public void start() {
    try {
      init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
  }

  private void init() throws Exception {
    ManageableRepository jcrRepository = jcrService_.getRepository();
    Map<String, String[]> permissions = null;
    for (int j = 0; j < plugins_.size(); j++) {
      CmsConfig config = plugins_.get(j).getPaths();
      List jcrPaths = config.getJcrPaths();
      for (Iterator iter = jcrPaths.iterator(); iter.hasNext();) {
        CmsConfig.JcrPath jcrPath = (CmsConfig.JcrPath) iter.next();
        List workspaces = jcrPath.getWorkspaces();
        for (Iterator iterator = workspaces.iterator(); iterator.hasNext();) {
          CmsConfig.Workspace workspace = (CmsConfig.Workspace) iterator.next();
          Session session = jcrRepository.getSystemSession(workspace.getName());
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
        }
      }
    }
  }

  public String getWorkspace() {
    return propertiesParam_.getProperty("workspace");
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
    for (int j = 0; j < plugins_.size(); j++) {
      CmsConfig config = plugins_.get(j).getPaths();
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
    if (plugin instanceof AddPathPlugin)
      plugins_.add((AddPathPlugin) plugin);
  }

  public ComponentPlugin removePlugin(String name) {
    return null;
  }

  public Collection getPlugins() {
    return null;
  }

}