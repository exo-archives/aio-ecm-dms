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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * Mar 31, 2009  
 */
public class TaxonomyServiceImpl implements TaxonomyService {
  private SessionProviderService providerService_;
  
  public TaxonomyServiceImpl(SessionProviderService providerService) throws Exception {
    providerService_ = providerService;
  }
  
  public void addCategories(Node node, String taxonomyName, String[] categoryPaths) throws RepositoryException {
    
  }

  public void addCategory(Node node, String taxonomyName, String categoryPath) throws RepositoryException {
    
  }

  public void addTaxonomyNode(String repository, String workspace, String parentPath, String taxoNodeName) throws RepositoryException {
    
  }

  public void addTaxonomyPlugin(TaxonomyPlugin plugin) {
    
  }

  public List<Node> getAllTaxonomyTrees(String repository, boolean system) throws RepositoryException {
    return null;
  }

  public List<Node> getAllTaxonomyTrees(String repository) throws RepositoryException {
    return null;
  }

  public List<String> getCategories(Node node, String taxonomyName) throws RepositoryException {
    return null;
  }

//  The method getTaxonomyTree will try to find an exo:symlink at ${exoTaxoTreesDefinitionPath}/${taxonomyName} 
//  if the link exists, the method will return the target node thanks to the method LinkManager.getTarget(..). 
//  The system session will be used.
  
  public Node getTaxonomyTree(String repository, String taxonomyName) throws RepositoryException {
    
    return null;
  }

  public boolean hasCategories(Node node, String taxonomyName) throws RepositoryException {
    return false;
  }

  public boolean hasTaxonomyTree(String repository, String taxonomyName) throws RepositoryException {
    return false;
  }

  public void init(String repository) throws Exception {
    
  }

  public void moveTaxonomyNode(String repository, String workspace, String srcPath, String destPath, String type) throws RepositoryException {
    
  }

  public void removeCategory(Node node, String taxonomyName, String categoryPath) throws RepositoryException {
    
  }

  public void removeTaxonomyNode(String repository, String workspace, String absPath) throws RepositoryException {
    
  }

  public void removeTaxonomyTree(String taxonomyName) throws RepositoryException {
    
  }

  public void updateTaxonomyTree(String taxonomyName, Node taxonomyTree) throws RepositoryException {
    
  }
  
  public void addTaxonomyTree(Node taxonomyTree) throws RepositoryException, TaxonomyAlreadyExistsException {
    // TODO Auto-generated method stub
    
  }
  
  private Session getSession(ManageableRepository manageRepository, String workspaceName,
      boolean system) throws RepositoryException {
    if (system)
      return providerService_.getSystemSessionProvider(null).getSession(workspaceName, manageRepository);
    return providerService_.getSessionProvider(null).getSession(workspaceName, manageRepository);
  }
}
