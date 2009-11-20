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
package org.exoplatform.services.cms.documents.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.documents.FavoriteService;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 16, 2009  
 * 10:02:04 AM
 */
public class FavoriteServiceImpl implements FavoriteService {

  private String FAVORITE_ALIAS = "userPrivateFavorites";

  private NodeHierarchyCreator nodeHierarchyCreator;
  private LinkManager linkManager;
  private NodeFinder nodeFinder;
  private SessionProvider sessionProvider;
  private RepositoryService repositoryService;
  
  public FavoriteServiceImpl(RepositoryService repositoryService, NodeHierarchyCreator nodeHierarchyCreator, LinkManager linkManager, 
      NodeFinder nodeFinder) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.linkManager = linkManager;
    this.nodeFinder = nodeFinder;
    ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
    SessionProviderService sessionProviderService
    		=	(SessionProviderService) myContainer.getComponentInstanceOfType(SessionProviderService.class);
    //this.sessionProvider = sessionProviderService.getSessionProvider(null);
    this.sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    this.repositoryService = repositoryService;
  }

  /**
   * {@inheritDoc}
   */
  public void addFavorite(Node node, String userName) throws Exception {
  	// check if node is symlink
  	if (linkManager.isLink(node)) return;
  	// check if node has already been favorite node of current user
    Node userFavoriteNode = getUserFavoriteFolder(userName);
    NodeIterator nodeIter = userFavoriteNode.getNodes();
    while (nodeIter.hasNext()) {
    	Node childNode = nodeIter.nextNode();
    	if (linkManager.isLink(childNode)) {
    		Node targetNode = null;
    		try {
					linkManager.getTarget(childNode);
    		} catch (Exception e) {}
				if (node.isSame(targetNode)) return;
    	}
    }
		// add favorite symlink    
    linkManager.createLink(userFavoriteNode, node);
    userFavoriteNode.getSession().save();
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getAllFavoriteNodesByUser(String workspace, String repository, String userName) throws Exception {
		List<Node> ret = new ArrayList<Node>();
		Node userFavoriteNode = getUserFavoriteFolder(userName);
		NodeIterator nodeIter = userFavoriteNode.getNodes();
		while (nodeIter.hasNext()) {
			Node childNode = nodeIter.nextNode();
			if (linkManager.isLink(childNode)) {
				Node targetNode = null;
				try {
					linkManager.getTarget(childNode);
				} catch (Exception ex) {}
				if (targetNode != null) ret.add(targetNode);
			}
		}
    return ret;
  }

  /**
   * {@inheritDoc}
   */
  public void removeFavorite(Node node, String userName) throws Exception {
  	// check if node is symlink
    if (linkManager.isLink(node)) return;
    // remove favorite
    Node userFavoriteNode = getUserFavoriteFolder(userName);
    NodeIterator nodeIter = userFavoriteNode.getNodes();
    while (nodeIter.hasNext()) {
    	Node childNode = nodeIter.nextNode();
    	if (linkManager.isLink(childNode)) {
    		Node targetNode = null;
    		try {
    			targetNode = linkManager.getTarget(childNode);
    		} catch (Exception e) { }
    		if (node.isSame(targetNode)) {
    			childNode.remove();
    			userFavoriteNode.getSession().save();
    			return;
    		}
    	}
    }
  }
  
  public boolean isFavoriter(String userName, Node node) throws Exception {
    Node userFavoriteNode = getUserFavoriteFolder(userName);
    NodeIterator nodeIter = userFavoriteNode.getNodes();
    while (nodeIter.hasNext()) {
    	Node childNode = nodeIter.nextNode();
    	if (linkManager.isLink(childNode)) {
    		Node targetNode = null;
    		try {
    			targetNode = linkManager.getTarget(childNode);
    		} catch (Exception e) { }
    		if (node.isSame(targetNode)) {
    			return true;
    		}
    	}
    }
    return false;
  }
  
  private Node getUserFavoriteFolder(String userName) throws Exception {
  	
  	// code for running
    Node userNode = nodeHierarchyCreator.getUserNode(sessionProvider, userName);
    String favoritePath = nodeHierarchyCreator.getJcrPath(FAVORITE_ALIAS);
    return userNode.getNode(favoritePath);

  	// code for test
		  	
/*		ManageableRepository manageableRepository = repositoryService.getRepository("repository");
  	Session session = sessionProvider.getSession("collaboration", manageableRepository);
  	Node rootNode = session.getRootNode();
  	if (rootNode.hasNode(userName))
  		return rootNode.getNode(userName);
  	
  	return rootNode.addNode(userName);*/
  	
  }
  
}
