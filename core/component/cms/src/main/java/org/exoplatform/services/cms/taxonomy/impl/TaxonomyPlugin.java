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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.categories.impl.TaxonomyConfig;
import org.exoplatform.services.cms.categories.impl.TaxonomyConfig.Taxonomy;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyConfig.Permission;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * Mar 31, 2009  
 */
public class TaxonomyPlugin extends BaseComponentPlugin {
  private String workspace;
  private String path;
  private String name;
  private List<Permission> permissions = new ArrayList<Permission>(4);
  
  private boolean autoCreateInNewRepository_ = true;
  private RepositoryService repositoryService_;  
  private String baseTaxonomiesStorage_;  
  private InitParams params_;  
  
  public TaxonomyPlugin(InitParams params, RepositoryService repositoryService, NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    repositoryService_ = repositoryService;
    baseTaxonomiesStorage_ = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);    
    params_ = params;
    ValueParam valueParam = params_.getValueParam("autoCreateInNewRepository");
    if (valueParam !=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(valueParam.getValue());
    }   
  }
  
  public void init(String repository) throws Exception {
  if (!autoCreateInNewRepository_) return;
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
//    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
//    String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();    
//    Session session = manageableRepository.getSystemSession(workspace);    
//    Node taxonomyHomeNode = (Node)session.getItem(baseTaxonomiesPath_);
//    //TODO Need remove this code
//    if(taxonomyHomeNode.hasProperty("exo:isImportedChildren"))  { 
//      session.logout();
//      return; 
//    }
//    taxonomyHomeNode.setProperty("exo:isImportedChildren",true);
//    Iterator<ObjectParameter> it = params_.getObjectParamIterator();
//    while(it.hasNext()) {
//      TaxonomyConfig config = (TaxonomyConfig)it.next().getObject();
//      for(Taxonomy taxonomy : config.getTaxonomies()) {
//        Node taxonomyNode = Utils.makePath(taxonomyHomeNode, taxonomy.getPath(), "exo:taxonomy", getPermissions(taxonomy.getPermissions()));
//        if(taxonomyNode.canAddMixin("mix:referenceable")) {
//          taxonomyNode.addMixin("mix:referenceable");
//        }
//      }
//    }
//    taxonomyHomeNode.save();
//    session.save();
//    session.logout();
  }
}
