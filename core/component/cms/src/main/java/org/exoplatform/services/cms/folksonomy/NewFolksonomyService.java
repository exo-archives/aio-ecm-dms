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
package org.exoplatform.services.cms.folksonomy;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.cms.taxonomy.impl.TaxonomyNodeAlreadyExistsException;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Nov 13, 2009  
 * 10:52:05 AM
 */
public interface NewFolksonomyService {
	public static final int PUBLIC_FOLKSONOMY = 0;
	public static final int SITE_FOLKSONOMY = 1;
	public static final int GROUP_FOLKSONOMY = 2;
	public static final int USER_FOLKSONOMY = 3;
	
  /**
   * Adds a new category path to the given node
   * 
   * @param node The node for which we add the category 
   * @param categoryName The name of the category
   * @param categoryPath The path of the category relative to the given category
   * @param folksonomyTree The folksonomy tree root node
   * @throws RepositoryException if the category cannot be added
   */
	public void addCategory(Node node, String categoryName, String categoryPath, Node folksonomyTree)
	throws RepositoryException;
	
  /**
   * Removes a category to the given node
   * 
   * @param node The node for which we remove the category
   * @param categoryName The name of the category
   * @param categoryPath The path of the category relative to the given category
   * @param folksonomyTree The folksonomy tree root node 
   * @throws RepositoryException if the category cannot be removed
   */
  public void removeCategory(Node node, String categoryName, String categoryPath, Node folksonomyTree)
      throws RepositoryException;
  
  /**
   * Adds a new category node at the given location
   * 
   * @param repository The name of the repository
   * @param workspace The name of the workspace
   * @param parentPath The place where the category node will be added
   * @param categoryNodeName The name of the category node
   * @throws CategoryNodeAlreadyExistsException if a category node with the same
   *           name has already been added
   * @throws RepositoryException if the category node could not be added
   */
  public void addCategoryNode(String repository, String workspace, String parentPath,
      String categoryNodeName, Node folksonomyTree) throws RepositoryException;
//      , CategoryNodeAlreadyExistsException;

  /**
   * Removes the category node located at the given absolute path
   * 
   * @param repository The name of the repository
   * @param workspace The name of the workspace
   * @param absPath The absolute path of the category node to remove
   * @param folksonomyTree The folksonomy tree root node 
   * @throws RepositoryException if the category node could not be removed
   */
  public void removeCategoryNode(String repository, String workspace, String absPath, 
  		Node folksonomyTree) throws RepositoryException;
  
  /**
   * Returns all the nodes related to the given category node (was tagged by this category)
   * @param categoryNode The category node
   * @throws RepositoryException if the node list can not be retrieved
   */  
  public List<Node> getAllRelatedNodes(Node categoryNode) throws RepositoryException;

  /**
   * Returns all the category nodes in the given folksonomy tree
   * @param folksonomyTree The folksonomy tree root node 
   * @throws RepositoryException if the node list can not be retrieved
   */  
	public List<Node> getAllCategoriesInTree(Node folksonomyTree) throws RepositoryException;   
	
  /**
   * Copies the category node from source path to destination path
   * 
   * @param repository The name of the repository
   * @param workspace The name of the workspace
   * @param srcPath The source path of this category
   * @param destPath The destination path of the category
   * @param folksonomyTree The folksonomy tree root node   * 
   * @throws RepositoryException if the category node could not be copy
   */
  public void copyTaxonomyNode(String repository, String workspace, String srcPath,
      String destPath, Node folksonomyTree) throws RepositoryException;

  /**
   * Moves the category node from source path to destination path
   * 
   * @param repository The name of the repository
   * @param workspace The name of the workspace
   * @param srcPath The source path of this category
   * @param destPath The destination path of the category
   * @param folksonomyTree The folksonomy tree root node 
   * @throws RepositoryException if the category node could not be copy
   */
  public void moveTaxonomyNode(String repository, String workspace, String srcPath,
      String destPath, Node folksonomyTree) throws RepositoryException;
	
	/**
	 * Checks if the expected folksonomy exists
	 *   
	 * @param folksonomyTreeType Type of the folksonomy tree : public, site, group or user
	 * @param repositoryName The name of the repository
	 * @param ownerName			 The folksonomy tree owner name (site name, group name or user name)
	 * @return							 true if the folksonomy tree exists, false in opposite case
	 * @throws RepositoryException if some error occurs
	 */
	public boolean hasFolksonomyTree(int folksonomyTreeType, String repositoryName, 
			String ownerName) throws RepositoryException;

	/**
	 * Gets the expected folksonomy tree
	 *   
	 * @param folksonomyTreeType Type of the folksonomy tree : public, site, group or user
	 * @param repositoryName The name of the repository
	 * @param ownerName			 The folksonomy tree owner name (site name, group name or user name)
	 * @throws RepositoryException if the tree can't be retrieved
	 */
	public Node getFolksonomyTree(int folksonomyTreeType, String repositoryName, 
			String ownerName) throws RepositoryException;
	
	/**
	 * Adds a folksonomy tree
	 *
   * @param folksonomyTree The folksonomy tree root node   
	 * @param folksonomyTreeType Type of the folksonomy tree : public, site, group or user
	 * @param repositoryName The name of the repository
	 * @param ownerName			 The folksonomy tree owner name (site name, group name or user name)
	 * @throws RepositoryException if some error occurs
	 */
	public void addFolksonomyTree(Node folksonomyTree, int folksonomyTreeType, String repositoryName, 
			String ownerName) throws RepositoryException;
	
	/**
	 * Removes a folksonomy tree
	 *
   * @param folksonomyTree The folksonomy tree root node   
	 * @param folksonomyTreeType Type of the folksonomy tree : public, site, group or user
	 * @param repositoryName The name of the repository
	 * @param ownerName			 The folksonomy tree owner name (site name, group name or user name)
	 * @throws RepositoryException if some error occurs
	 */
	public void removeFolksonomyTree(Node folksonomyTree, int folksonomyTreeType, String repositoryName, 
			String ownerName) throws RepositoryException;
	
}
