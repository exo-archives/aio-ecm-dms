/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.services.cms.link;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Mar 14, 2009  
 */
public interface NodeFinder {
  /**
   * Returns the node at relPath relative to ancestor node.
   * @param ancestorNode           The ancestor of the node to retrieve from which we start.
   * @param relativePath           The relative path of the node to retrieve.
   *
   * @throws PathNotFoundException If no node exists at the specified path.
   * @throws RepositoryException   if another error occurs.
   */
   public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException, RepositoryException, RepositoryConfigurationException;
   
   /**
   * Returns the item at the specified absolute path.
   * @param repository             The name of repository
   * @param workspace              The name of workspace
   * @param absPath                An absolute path.
   *
   * @throws PathNotFoundException if the specified path cannot be found.
   * @throws RepositoryException   if another error occurs.
   */
   public Item getItem(String repository, String workspace, String absPath) throws PathNotFoundException, RepositoryException, RepositoryConfigurationException;
}
