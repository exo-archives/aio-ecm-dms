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
package org.exoplatform.services.ecm.taxonomy;

import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 6, 2008  
 */
public interface TaxonomyManagerService {
  
  /**
   * Adding a new taxonomy to the specific node
   * <p>If the taxonomy has already existed in this specific node then the method will thrown
   * <code>ItemExistsException</code>
   * @param parentPath            Specify the parent path which the taxonomy will be added into
   * @param childName             The name of the taxonomy
   * @param repository            The name of the repository
   * @param sessionProvider       The sessionProvider object is used to managed Sessions 
   * @see                         SessionProvider   
   * @throws ItemExistsException
   * @throws Exception
   */
  public void addTaxonomy(String parentPath, String childName, String repository, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Remove the taxonomy that is specified by real path
   * @param realPath              Specify the real path of the taxonomy which is removed 
   * @param repository            The name of the repository
   * @param sessionProvider       The sessionProvider object is used to managed Sessions
   * @see                         SessionProvider
   * @throws Exception
   */
  public void removeTaxonomy(String realPath, String repository, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Copy taxonomy from the location is specified by source patch to the destination path
   * @param srcPath               The source path of the taxonomy which wanted to copy
   * @param destPath              The destination path of the taxonomy 
   * @param repository            The name of the repository
   * @param sessionProvider       The sessionProvider object is used to managed Sessions
   * @see                         SessionProvider
   * @throws Exception
   */
  public void copyTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Copy taxonomy from the location is specified by source patch to the destination path
   * @param srcPath               The source path of the taxonomy which wanted to cut
   * @param destPath              The destination path of the taxonomy 
   * @param repository            The name of the repository
   * @param sessionProvider       The sessionProvider object is used to managed Sessions
   * @see                         SessionProvider
   * @throws Exception
   */
  public void cutTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception;    
  
  /**
   * Returns list of node that are categoried in the specific node
   * @param node                  The node category to get all nodes from it
   * @param sessionProvider       The sessionProvider object is used to managed Sessions
   * @return                      list of node
   * @see                         Node
   * @see                         SessionProvider
   * @throws Exception
   */
  public List<Node> getCategories(Node node, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Remove the category that is specified by the real path from the node
   * @param node
   * @param categoryPath
   * @param sessionProvider
   * @throws Exception
   */
  public void removeCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Adding a new category to the specific node
   * @param node              Specify the node wants to add category into
   * @param categoryPath      The name of category path
   * @param sessionProvider   The sessionProvider object is userd to managed Sessions
   * @see                     Node
   * @see                     SessionProvider
   * @throws Exception
   */
  public void addCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception;
  
  /**
   * Adding a new category to the specific node. 
   * @param node              Specify the node wants to add category into
   * @param categoryPath      The name of category path
   * @param replaceAll        The parameter is specify in order to affect this method
   *                          if this parameter is <code>true</code>, this method will remove all
   *                          category in this node and then add a new category to this node
   * @param sessionProvider   The sessionProvider object is userd to managed Sessions
   * @see                     Node
   * @see                     SessionProvider
   * @throws Exception
   */
  public void addCategory(Node node, String categoryPath, boolean replaceAll, SessionProvider sessionProvider) throws Exception;
  
}
