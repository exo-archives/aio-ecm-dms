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
package org.exoplatform.services.cms.categories.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.Workspace;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

public class CategoriesServiceImpl implements CategoriesService,Startable {		
  private static final String CATEGORY_MIXIN = "exo:categorized";
  private static final String CATEGORY_PROP = "exo:category";
  private static final String COPY = "copy";
  private static final String CUT = "cut"; 
  private static final String CATEGORIES_MANDATORY = "categoryMandatoryWhenFileUpload";
  
  private boolean isCategoriesMandatory;
  private RepositoryService repositoryService_;  
  private String baseTaxonomyPath_ ;  
  List<TaxonomyPlugin> plugins_ = new ArrayList<TaxonomyPlugin>() ;

  public CategoriesServiceImpl(RepositoryService repositoryService,
      NodeHierarchyCreator nodeHierarchyCreator, InitParams params) throws Exception{  
    repositoryService_ = repositoryService;    
    baseTaxonomyPath_ = nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH);
    Iterator<ValueParam> iterator = params.getValueParamIterator();   
    while (iterator.hasNext()) {      
      ValueParam object = iterator.next();
      if (object.getName().trim().equalsIgnoreCase(CATEGORIES_MANDATORY)) {
        String objectValue = object.getValue().trim();
        if (objectValue.equalsIgnoreCase("true")) isCategoriesMandatory = true;
        else isCategoriesMandatory = false;
      }
    } 
  }
  
  public void init(String repository) throws Exception {
    for(TaxonomyPlugin plugin : plugins_) {
      plugin.init(repository) ;
    }
  }
  
  public boolean isCategoriesMandatory() {
    return isCategoriesMandatory;
  }
  
  public void addTaxonomyPlugin(ComponentPlugin plugin) {    
    if(plugin instanceof TaxonomyPlugin) {			
      plugins_.add((TaxonomyPlugin)plugin) ;           
    }
  }

  public Node getTaxonomyHomeNode (String repository,SessionProvider provider) throws Exception {    
    Session session = getSession(repository,provider) ;    
    Node homeTaxonomy = (Node)session.getItem(baseTaxonomyPath_) ;
    return homeTaxonomy ;
  }	
  
  public void addTaxonomy(String parentPath,String childName, String repository) throws Exception  {
    Session adminSession = getSession(repository) ;
    Node parent = (Node)adminSession.getItem(parentPath) ;
    if(parent.hasNode(childName)) {
      throw (new ItemExistsException()) ;
    }		
    Node taxonomyNode = parent.addNode(childName,"exo:taxonomy") ;
    if(taxonomyNode.canAddMixin("mix:referenceable")) {
      taxonomyNode.addMixin("mix:referenceable") ;
    }					
    adminSession.save() ;
    adminSession.logout();
  }

  public void removeTaxonomyNode(String realPath, String repository) throws Exception {
    Session adminSession = getSession(repository) ;    
    Node selectedNode = (Node)adminSession.getItem(realPath) ;
    selectedNode.remove() ;
    adminSession.getRootNode().save();
    adminSession.save();    
    adminSession.logout();
  }						

  public void moveTaxonomyNode(String srcPath, String destPath, String type, String repository) throws Exception { 
    Session session = getSession(repository) ;    		   
    if(CUT.equals(type)) {			
      session.move(srcPath,destPath) ;
      session.save() ;      
      session.logout();
    }
    else if(COPY.equals(type)) {		
      Workspace workspace = session.getWorkspace() ;       
      workspace.copy(srcPath,destPath) ;
      session.save() ;      
      session.logout();
    }
    else {
      session.logout();
      throw( new UnsupportedRepositoryOperationException()) ;		
    }									
  }	

  public boolean hasCategories(Node node) throws Exception {
    if (node.isNodeType(CATEGORY_MIXIN)) {
      if (node.hasProperty("exo:category")) {
        Value[] values = node.getProperty("exo:category").getValues();
        if (values.length > 0)
          return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unused")
  public List<Node> getCategories(Node node, String repository) throws Exception {
    List<Node> cats = new ArrayList<Node>();
    Session session = getSession(repository) ;
    if (node.hasProperty("exo:category")) {
      try {			
        Property categories = node.getProperty("exo:category");
        Value[] values = categories.getValues();
        for (int i = 0; i < values.length; i++) {				
          cats.add(session.getNodeByUUID(values[i].getString()));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }    
    }
    return cats;
  }

  public void removeCategory(Node node, String categoryPath, String repository) throws Exception {
    Session systemSession = getSession(repository) ;
    List<Value> vals = new ArrayList<Value>();
    if (!"*".equals(categoryPath)) {						
      Property categories = node.getProperty("exo:category");
      Value[] values = categories.getValues();
      String uuid2Remove = null;
      for (int i = 0; i < values.length; i++) {
        String uuid = values[i].getString();
        Node refNode = systemSession.getNodeByUUID(uuid);
        if(refNode.getPath().equals(categoryPath)) {
          uuid2Remove = uuid;              
        } else {
          vals.add(values[i]);
        }
      }
      if(uuid2Remove == null) {
        systemSession.logout();
        return; 
      }			
    }
    node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
    node.getSession().save() ;
    systemSession.logout();
  }
  
  private void processRemoveCategory(Node node) throws Exception {
    List<Value> vals = new ArrayList<Value>();
    if (node.hasProperty("exo:category")) node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
    node.save();
  }
  
  private void processCategory(Session systemSession, Node node, String categoryPath) throws Exception {
    Node catNode = (Node) systemSession.getItem(categoryPath.trim());
    Value value2add = node.getSession().getValueFactory().createValue(catNode);      
    if (!node.isNodeType(CATEGORY_MIXIN)) {     
      node.addMixin(CATEGORY_MIXIN);    
      node.setProperty(CATEGORY_PROP, new Value[] {value2add});
      node.save();
    } else {
      List<Value> vals = new ArrayList<Value>();
      Value[] values = node.getProperty(CATEGORY_PROP).getValues();
      for (int i = 0; i < values.length; i++) {
        Value value = values[i];                       
        if(!vals.contains(value)) vals.add(value);
      }
      vals.add(value2add);
      node.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
      node.save();
    }     
  }

  public void addMultiCategory(Node node, String[] arrCategoryPath, String repository) throws Exception {
    Session systemSession = getSession(repository);    
    processRemoveCategory(node);
    for(String categoryPath : arrCategoryPath) {      
      processCategory(systemSession, node, categoryPath);
    }
    systemSession.logout();
  }
  
  public void addCategory(Node node, String categoryPath, String repository) throws Exception {    
    Session systemSession = getSession(repository) ;
    processCategory(systemSession, node, categoryPath);
    systemSession.logout();
  }

  public void addCategory(Node node, String categoryPath, boolean replaceAll, String repository) throws Exception {
    if (replaceAll) {
      removeCategory(node, "*", repository) ;
    }
    addCategory(node, categoryPath, repository) ;
  }    

  public Session getSession(String repository) throws Exception {    
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return manageableRepository.getSystemSession(workspace) ;
  }
  
  private Session getSession(String repository,SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return provider.getSession(workspace,manageableRepository) ;
  }

  public void start() {
    try{
      for(TaxonomyPlugin plugin : plugins_) {
        plugin.init() ;
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public void stop() {
    
  }
}
