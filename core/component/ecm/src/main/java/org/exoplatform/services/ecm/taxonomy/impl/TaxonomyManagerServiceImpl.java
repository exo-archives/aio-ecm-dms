/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.taxonomy.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.ecm.taxonomy.TaxonomyManagerService;
import org.exoplatform.services.ecm.taxonomy.impl.HierarchicalTaxonomyConfig.Taxonomy;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 20, 2008  
 */
@Deprecated
public class TaxonomyManagerServiceImpl implements TaxonomyManagerService, Startable {
  private static final String CATEGORY_MIXIN = "exo:categorized";
  private static final String CATEGORY_PROP = "exo:category";

  private RepositoryService repositoryService_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private List<TaxonomyPlugin> plugins_ = new ArrayList<TaxonomyPlugin>(3);
  private Log log = ExoLogger.getLogger("ecm:taxonomiesManager") ;
  
  public TaxonomyManagerServiceImpl(RepositoryService repositoryService, NodeHierarchyCreator hierarchyCreator) {
    this.repositoryService_ = repositoryService;
    this.nodeHierarchyCreator_ = hierarchyCreator ;
  }

  public void addPlugin(ComponentPlugin plugin) {   
    if(plugin instanceof TaxonomyPlugin) {      
      plugins_.add((TaxonomyPlugin)plugin) ; 
    }        
  }

  public void addCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception {    
    ManageableRepository manageRepository = (ManageableRepository)node.getSession().getRepository();
    String workspace = manageRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = sessionProvider.getSession(workspace, manageRepository);    
    Session curentSession = node.getSession();
    Node catNode = (Node)session.getItem(categoryPath);
    String catNodeUUID = catNode.getUUID();    
    Value value2add = session.getValueFactory().createValue(catNode);

    if (!node.isNodeType(CATEGORY_MIXIN)) {
      node.addMixin(CATEGORY_MIXIN);
      node.setProperty(CATEGORY_PROP, new Value[]{value2add});
      return ;
    } 
    List<Value> vals = new ArrayList<Value>();
    Value[] values = node.getProperty(CATEGORY_PROP).getValues();
    for (Value value: values) {    
      String uuid = value.getString();      
      if (uuid.equals(catNodeUUID)) { continue; }
      vals.add(value);
    }
    vals.add(value2add);
    node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
    curentSession.save();                   
  }

  public void addCategory(Node node, String categoryPath, boolean replaceAll, SessionProvider sessionProvider) throws Exception {
    if (replaceAll) {
      removeCategory(node, "*", sessionProvider) ;
    }
    addCategory(node, categoryPath, sessionProvider) ;
  }

  public List<Node> getCategories(Node node, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageRepository = (ManageableRepository)node.getSession().getRepository();
    String workspace = manageRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = sessionProvider.getSession(workspace, manageRepository);    
    List<Node> listNode = new ArrayList<Node>();    
    if (!node.hasProperty(CATEGORY_PROP)) 
      return listNode ;    
    Property categories = node.getProperty(CATEGORY_PROP);
    Value[] values = categories.getValues();
    for (Value value : values) {
      listNode.add(session.getNodeByUUID(value.getString()));
    }    
    return listNode;            
  }

  public void removeCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageRepository = (ManageableRepository)node.getSession().getRepository();
    String workspace = manageRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = sessionProvider.getSession(workspace, manageRepository);    

    List<Value> listValue = new ArrayList<Value>();
    if (!"*".equals(categoryPath)) {
      Value[] values = node.getProperty(CATEGORY_PROP).getValues();
      Node nodeTaxo = (Node)session.getItem(categoryPath);
      String nodeTaxoUUID = nodeTaxo.getUUID();      
      for (Value value : values) {        
        if (!value.getString().equals(nodeTaxoUUID)) {
          listValue.add(value);
        }        
      }            
    }  
    node.setProperty(CATEGORY_PROP, listValue.toArray(new Value[listValue.size()]));
    node.getSession().save();
  }

  private Session getSession(String repository, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return sessionProvider.getSession(workspace, manageableRepository);    
  }

  public void addTaxonomy(String parentPath, String childName, String repository, SessionProvider sessionProvider) throws Exception {
    Session session = getSession(repository, sessionProvider);
    Node parent = (Node)session.getItem(parentPath);
    if (parent.hasNode(childName)) { throw new ItemExistsException(); };
    Node taxonomyNode = parent.addNode(childName, "exo:taxonomy");
    if (taxonomyNode.canAddMixin("mix:referenceable")) {
      taxonomyNode.addMixin("mix:referenceable");
    }
    session.save();            
  }

  public void copyTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception {    
    Session session = getSession(repository, sessionProvider);
    session.getWorkspace().copy(srcPath, destPath);
    session.save();    
  }  

  public void cutTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception {    
    Session session = getSession(repository, sessionProvider);
    session.move(srcPath, destPath);
    session.save();        
  }

  public void removeTaxonomy(String realPath, String repository, SessionProvider sessionProvider) throws Exception {    
    Session session = getSession(repository, sessionProvider);
    Node selectedNode = (Node)session.getItem(realPath);
    selectedNode.remove();
    session.save();    
  }

  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    for(RepositoryEntry entry : repositoryService_.getConfig().getRepositoryConfigurations()) {
      String repoName = entry.getName();
      String defaultWorkspace = entry.getDefaultWorkspaceName();
      try {                
        ManageableRepository repository = repositoryService_.getRepository(repoName);
        Session session = sessionProvider.getSession(defaultWorkspace, repository);
        String taxonomiesHomePath = nodeHierarchyCreator_.getJcrPath("eXoTaxonomyService");      
        Node rootTaxonomies = (Node)session.getItem(taxonomiesHomePath);
        if (rootTaxonomies.hasNodes()) { return; }
        for (TaxonomyPlugin taxonomyPlugin : plugins_) {
          for (HierarchicalTaxonomyConfig config : taxonomyPlugin.getPredefinedTaxonomies()) {         
            for (Taxonomy taxonomy : config.getTaxonomies()) {
              makeTaxonomy(rootTaxonomies, taxonomy.getPath());
            }
          }
        }
        session.save() ;
      } catch (Exception e) {
        log.error("Can not init taxonomies for repository: " + repoName, e);
      }            
    }
    sessionProvider.close();
    //should clear plugin to optimize memory
    plugins_.clear();
  }

  public void stop() { }  

  private void makeTaxonomy(Node rootNode, String path) throws PathNotFoundException, RepositoryException {    
    String[] tokens = path.split("/");
    Node node = rootNode;
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i];
      if (node.hasNode(token)) {
        node = node.getNode(token);
        continue; 
      }            
      node = node.addNode(token, "exo:taxonomy");
      if (!node.isNodeType("mix:referenceable")) {
        node.addMixin("mix:referenceable");
      }
      if (node.canAddMixin("exo:privilegeable")) {
        node.addMixin("exo:privilegeable");
      }              
    }    
    rootNode.getSession().save();
  }
  
  
}
