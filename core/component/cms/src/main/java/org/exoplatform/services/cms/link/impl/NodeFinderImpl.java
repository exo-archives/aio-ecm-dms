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
package org.exoplatform.services.cms.link.impl;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SARL Author : Hoang Van Hung hunghvit@gmail.com
 * Mar 14, 2009
 */
public class NodeFinderImpl implements NodeFinder {
  
  private final RepositoryService repositoryService_;
  
  private final SessionProviderService sessionProviderService_;
  
  private final LinkManager linkManager_;
  
  public NodeFinderImpl(RepositoryService repositoryService, SessionProviderService   sessionProviderService, LinkManager linkManager){
    this.repositoryService_ = repositoryService;
    this.sessionProviderService_ = sessionProviderService;
    this.linkManager_ = linkManager;
  }

  /**
   * {@inheritDoc}
   */
  public Item getItem(String repository, String workspace, String absPath)
      throws PathNotFoundException, RepositoryException {
    if (!absPath.startsWith("/")) throw new IllegalArgumentException(absPath + " isn't absolute path");
	try {
	    Session session = getSession(repositoryService_.getRepository(repository), workspace);
	    return getItem(session, absPath);
	} catch (RepositoryConfigurationException e) {
	    throw new RepositoryException(e);
	}
  }

  /**
   * {@inheritDoc}
   */
  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException {
    if (relativePath.startsWith("/")) throw new IllegalArgumentException("Invalid relative path: " + relativePath);
    String absPath = "";
    if (ancestorNode.getPath().equals("/")) absPath = "/" + relativePath;
    else absPath = ancestorNode.getPath() + "/" + relativePath;
    Session session = ancestorNode.getSession();
    return (Node) getItem(session, absPath);
  }
  
  /**
   * Get item by absolute path
   * @param session The user session
   * @param absPath The absolute path to node
   * @return the item corresponding to the path
   */
  public Item getItem(Session session, String absPath) throws PathNotFoundException, RepositoryException {
	  return getItem(session, absPath, 0);
  }
	  
  /**
   * Get item by absolute path
   * @param session The user session
   * @param absPath The absolute path to node
   * @param fromIdx The start index used to find the link
   * @return the item corresponding to the path
   */
  private Item getItem(Session session, String absPath, int fromIdx) throws PathNotFoundException, RepositoryException {
    if (session.itemExists(absPath)) {
        // The item corresponding to absPath can be found 
    	return session.getItem(absPath);
    }
    // The item corresponding to absPath can not be found so we split absPath and check
    String[] splitPath = absPath.substring(1).split("/");
	int low = fromIdx;
	int high = splitPath.length - 1;    
    while (low <= high) {
        int mid = (low + high) >>> 1;
        String partPath = makePath(splitPath, mid);

        if (session.itemExists(partPath)) {
        	// The item can be found
        	Item item = session.getItem(partPath);
        	if (linkManager_.isLink(item)) {
        		// The item is a link
        		Node link = (Node) item;
        		if (linkManager_.isTargetReachable(link)) {
        			// The target can be reached
            		Node target = linkManager_.getTarget(link);
            		String targetPath = target.getPath();
            		return getItem(target.getSession(), targetPath + absPath.substring(partPath.length()), targetPath.substring(1).split("/").length);        			
        		} else {
        			// The target cannot be found
        			throw new PathNotFoundException("Can't reach the target of the link: " + link.getPath());
        		}
        	} else {
        		// The item is not a link so we need
            	low = mid + 1;        		
        	}
        } else {
        	// The item doesn't exist
        	high = mid - 1;
        }
    }
    throw new PathNotFoundException("Can't find path: " + absPath);
  }


  /**
   * Get session of user in given workspace and repository
   * @param repository
   * @param workspace
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  
  private Session getSession(ManageableRepository manageableRepository, String workspace) throws RepositoryException{
    return sessionProviderService_.getSessionProvider(null).getSession(workspace, manageableRepository);
  }
  
  /**
   * Make sub path of absolute path from 0 to toIdx index
   * 
   * @param splitString
   * @param toIdx
   * @throws NullPointerException, ArrayIndexOutOfBoundsException
   */
  private String makePath(String[] splitString, int toIdx) {
    StringBuilder buffer = new StringBuilder(1024);
    for(int i = 0; i <= toIdx; i++) {
    	buffer.append('/').append(splitString[i]);
    } 
    return buffer.toString();
  }
}
