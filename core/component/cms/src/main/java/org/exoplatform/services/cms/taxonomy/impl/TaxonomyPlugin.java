/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.taxonomy.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.actions.impl.ActionConfig;
import org.exoplatform.services.cms.actions.impl.ActionConfig.TaxonomyAction;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyConfig.Permission;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyConfig.Taxonomy;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Mar 31, 2009
 */
public class TaxonomyPlugin extends BaseComponentPlugin {
  private String                 workspace                  = "";

  private String                 path                       = "";

  private String                 treeName                   = "";

  private List<Permission>       permissions                = new ArrayList<Permission>(4);

  private boolean                autoCreateInNewRepository_ = true;

  private RepositoryService      repositoryService_;

  private TaxonomyService        taxonomyService_;

  private String                 baseTaxonomiesStorage_;

  private ActionServiceContainer actionServiceContainer_;

  private InitParams             params_;

  public TaxonomyPlugin(InitParams params, RepositoryService repositoryService,
      NodeHierarchyCreator nodeHierarchyCreator, TaxonomyService taxonomyService,
      ActionServiceContainer actionServiceContainer) throws Exception {
    repositoryService_ = repositoryService;
    baseTaxonomiesStorage_ = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
    taxonomyService_ = taxonomyService;
    actionServiceContainer_ = actionServiceContainer;
    params_ = params;
    ValueParam autoCreated = params_.getValueParam("autoCreateInNewRepository");
    ValueParam workspaceParam = params_.getValueParam("workspace");
    ValueParam pathParam = params_.getValueParam("path");
    ValueParam nameParam = params_.getValueParam("treeName");
    if (autoCreated != null)
      autoCreateInNewRepository_ = Boolean.parseBoolean(autoCreated.getValue());
    if (pathParam == null)
      path = baseTaxonomiesStorage_;
    if (workspaceParam == null || workspaceParam.getValue().trim().length() == 0) {
      path = baseTaxonomiesStorage_;
    }
    if (nameParam != null) {
      treeName = nameParam.getValue();
    }
  }

  public void init(String repository) throws Exception {
    if (!autoCreateInNewRepository_)
      return;
    importPredefineTaxonomies(repository);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }

  public String getWorkspace() {
    return workspace;
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  private void importPredefineTaxonomies(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    if (getWorkspace() == null || getWorkspace().trim().length() == 0) {
      setWorkspace(repositoryService_.getRepository(repository).getConfiguration()
          .getSystemWorkspaceName());
    }
    Session session = manageableRepository.getSystemSession(getWorkspace());
    Node taxonomyStorageNode = (Node) session.getItem(path);
    Node taxonomyStorageNodeSystem = null;
    if (taxonomyStorageNode.hasProperty("exo:isImportedChildren")) {
      session.logout();
      return;
    }
    taxonomyStorageNode.setProperty("exo:isImportedChildren", true);
    Iterator<ObjectParameter> it = params_.getObjectParamIterator();
    while (it.hasNext()) {
      ObjectParameter objectParam = it.next();
      if (objectParam.getName().equals("permission.configuration")) {
        taxonomyStorageNodeSystem = Utils.makePath(taxonomyStorageNode, treeName, "exo:taxonomy",
            null);
        session.save();
        TaxonomyConfig config = (TaxonomyConfig) objectParam.getObject();
        for (Taxonomy taxonomy : config.getTaxonomies()) {
          Map mapPermissions = getPermissions(taxonomy.getPermissions());
          if (mapPermissions != null) {
            ((ExtendedNode) taxonomyStorageNodeSystem).setPermissions(mapPermissions);
          }
          if (taxonomyStorageNodeSystem.canAddMixin("mix:referenceable")) {
            taxonomyStorageNodeSystem.addMixin("mix:referenceable");
          }
        }
      } else if (objectParam.getName().equals("taxonomy.configuration")) {
        TaxonomyConfig config = (TaxonomyConfig) objectParam.getObject();
        for (Taxonomy taxonomy : config.getTaxonomies()) {
          Node taxonomyNode = Utils.makePath(taxonomyStorageNodeSystem, taxonomy.getPath(),
              "exo:taxonomy", getPermissions(taxonomy.getPermissions()));
          if (taxonomyNode.canAddMixin("mix:referenceable")) {
            taxonomyNode.addMixin("mix:referenceable");
          }
          taxonomyNode.getSession().save();
        }
      } else if (objectParam.getName().equals("predefined.actions")) {
        taxonomyStorageNodeSystem = Utils.makePath(taxonomyStorageNode, treeName, "exo:taxonomy",
            null);
        session.save();
        ActionConfig config = (ActionConfig) objectParam.getObject();
        List actions = config.getActions();
        for (Iterator iter = actions.iterator(); iter.hasNext();) {
          TaxonomyAction action = (TaxonomyAction) iter.next();
          addAction(action, taxonomyStorageNodeSystem, repository);
        }
      }

    }
    taxonomyStorageNode.save();
    try {
      taxonomyService_.addTaxonomyTree(taxonomyStorageNodeSystem);
    } catch (TaxonomyAlreadyExistsException e) {
      e.printStackTrace();
    }
    session.save();
    session.logout();
  }

  private void addAction(ActionConfig.TaxonomyAction action, Node srcNode, String repository)
      throws Exception {
    Map<String, JcrInputProperty> sortedInputs = new HashMap<String, JcrInputProperty>();
    JcrInputProperty jcrInputName = new JcrInputProperty();
    jcrInputName.setJcrPath("/node/exo:name");
    jcrInputName.setValue(action.getName());
    sortedInputs.put("/node/exo:name", jcrInputName);
    JcrInputProperty jcrInputDes = new JcrInputProperty();
    jcrInputDes.setJcrPath("/node/exo:description");
    jcrInputDes.setValue(action.getDescription());
    sortedInputs.put("/node/exo:description", jcrInputDes);
    JcrInputProperty jcrInputLife = new JcrInputProperty();
    jcrInputLife.setJcrPath("/node/exo:lifecyclePhase");
    jcrInputLife.setValue(action.getLifecyclePhase());
    sortedInputs.put("/node/exo:lifecyclePhase", jcrInputLife);
    
    JcrInputProperty rootProp = sortedInputs.get("/node");
    if (rootProp == null) {
      rootProp = new JcrInputProperty();
      rootProp.setJcrPath("/node");
      rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
      sortedInputs.put("/node", rootProp);
    } else {
      rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
    }
    actionServiceContainer_.addAction(srcNode, repository, action.getType(), sortedInputs);
    Node actionNode = actionServiceContainer_.getAction(srcNode, action.getName());
    if (action.getRoles() != null) {
      String[] roles = StringUtils.split(action.getRoles(), ";");
      actionNode.setProperty("exo:roles", roles);
    }
    actionNode.setProperty("exo:storeHomePath", action.getHomePath());
    actionNode.setProperty("exo:targetWorkspace", action.getTargetWspace());
    actionNode.setProperty("exo:targetPath", action.getTargetPath());

    Iterator mixins = action.getMixins().iterator();
    while (mixins.hasNext()) {
      ActionConfig.Mixin mixin = (ActionConfig.Mixin) mixins.next();
      actionNode.addMixin(mixin.getName());
      Map<String, String> props = mixin.getParsedProperties();
      Set keys = props.keySet();
      for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        actionNode.setProperty(key, props.get(key));
      }
    }
    actionNode.getSession().save();
  }

  public Map getPermissions(List<Permission> listPermissions) {
    Map<String, String[]> permissionsMap = new HashMap<String, String[]>();
    for (Permission permission : listPermissions) {
      StringBuilder strPer = new StringBuilder();
      if ("true".equals(permission.getRead()))
        strPer.append(PermissionType.READ);
      if ("true".equals(permission.getAddNode()))
        strPer.append(",").append(PermissionType.ADD_NODE);
      if ("true".equals(permission.getSetProperty()))
        strPer.append(",").append(PermissionType.SET_PROPERTY);
      if ("true".equals(permission.getRemove()))
        strPer.append(",").append(PermissionType.REMOVE);
      permissionsMap.put(permission.getIdentity(), strPer.toString().split(","));
    }
    return permissionsMap;
  }

  public void init() throws Exception {
    if (autoCreateInNewRepository_) {
      for (RepositoryEntry repositoryEntry : repositoryService_.getConfig()
          .getRepositoryConfigurations()) {
        importPredefineTaxonomies(repositoryEntry.getName());
      }
      return;
    }
    ValueParam param = params_.getValueParam("repository");
    String repository = null;
    if (param == null) {
      repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
    } else {
      repository = param.getValue();
    }
    importPredefineTaxonomies(repository);
  }
}
