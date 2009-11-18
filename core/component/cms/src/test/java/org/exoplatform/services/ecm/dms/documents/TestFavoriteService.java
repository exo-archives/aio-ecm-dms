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
package org.exoplatform.services.ecm.dms.documents;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.documents.FavoriteService;
import org.exoplatform.services.cms.documents.impl.FavoriteServiceImpl;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL Author : Nguyen Anh Vu anhvurz90@gmail.com
 * Nov 17, 2009 11:14:48 AM
 */
public class TestFavoriteService extends BaseDMSTestCase {

	// final static public String EXO_FAVOURITE_NODE = "exo:favourite";
	// final static public String EXO_FAVOURITER_PROPERTY = "exo:favouriter";

	private FavoriteService favoriteService;

	public void setUp() throws Exception {
		super.setUp();
		ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
		NodeHierarchyCreator nodeHierarchyCreator 
				= (NodeHierarchyCreator) myContainer.getComponentInstanceOfType(NodeHierarchyCreator.class);
		LinkManager linkManager 
				= (LinkManager) myContainer.getComponentInstanceOfType(LinkManager.class);
		NodeFinder nodeFinder 
				= (NodeFinder) myContainer.getComponentInstanceOfType(NodeFinder.class);
		RepositoryService repositoryService 
				= (RepositoryService) myContainer.getComponentInstanceOfType(RepositoryService.class);
		favoriteService = new FavoriteServiceImpl(repositoryService, nodeHierarchyCreator, linkManager, nodeFinder);
		

    SessionProviderService sessionProviderService
		=	(SessionProviderService) myContainer.getComponentInstanceOfType(SessionProviderService.class);
	
	  SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
		
		ManageableRepository manageableRepository = repositoryService.getRepository("repository");
		
  	Session session = sessionProvider.getSession("collaboration", manageableRepository);
  	Node rootNode = session.getRootNode();
  	String[] names = new String[] {"root", "demo", "James", "John", "Marry"};
  	for (String name : names)
  		if (rootNode.hasNode(name)) {
  			rootNode.getNode(name).remove();
  			rootNode.save();
  		}

	}

	/**
	 * test method addFavourite. Input: /testAddFavourite node tested action: add 
	 * favorite for 3 users 'root', 'demo', 'James' to the node
	 * above. Expected value : 1 for favorite node list size of each user
	 * /testAddFavourite node)
	 * 
	 * @throws Exception
	 */
	public void testAddFavorite() throws Exception {
		Node rootNode = session.getRootNode();
		Node testAddFavouriteNode = rootNode.addNode("testAddFavorite");
		session.save();		
		favoriteService.addFavorite(testAddFavouriteNode, "root");
		favoriteService.addFavorite(testAddFavouriteNode, "demo");
		favoriteService.addFavorite(testAddFavouriteNode, "James");

		int rootFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "root").size();
		int demoFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "demo").size();
		int jamesFav = favoriteService.
		getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "James").size();
		
		assertEquals("testAddFavorite failed!", 1, rootFav);
		assertEquals("testAddFavorite failed!", 1, demoFav);
		assertEquals("testAddFavorite failed!", 1, jamesFav);

		testAddFavouriteNode.remove();
		session.save();
	}

	/**
	 * test method removeFavourite. Input: /testAddFavourite node tested action:
	 * add favorite for 4 users 'root', 'James', 'John' and Marry to the node above,
	 * remove favorite from 2 users 'root' and 'James'. Expected value :
	 * 0 for favorite node list size of 'root
	 * 0 for favorite node list size of 'James'
	 * 1 for favorite node list size of 'John'
	 * 1 for favorite node list size of 'Marry'
	 * /testAddFavourite node)
	 * 
	 * @throws Exception
	 */
	public void testRemoveFavorite() throws Exception {
		Node rootNode = session.getRootNode();
		Node testRemove = rootNode.addNode("testRemoveFavourite");
		session.save();
		favoriteService.addFavorite(testRemove, "root");
		favoriteService.addFavorite(testRemove, "James");
		favoriteService.addFavorite(testRemove, "John");
		favoriteService.addFavorite(testRemove, "Marry");

		favoriteService.removeFavorite(testRemove, "root");
		favoriteService.removeFavorite(testRemove, "James");

		int rootFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "root").size();
		int jamesFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "James").size();
		int johnFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "John").size();
		int marryFav = favoriteService.
				getAllFavoriteNodesByUser(session.getWorkspace().getName(), repository.getName(), "Marry").size();
		
		assertEquals("testRemoveFavorite failed!", 0, rootFav);
		assertEquals("testRemoveFavorite failed!", 1, johnFav);
		assertEquals("testRemoveFavorite failed!", 0, jamesFav);
		assertEquals("testRemoveFavorite failed!", 1, marryFav);

		testRemove.remove();
		session.save();
	}

	/**
	 * test method getAllFavouriteNodesByUser. Input: /node0 /node1 /node2 /node3
	 * /node3/node4. Tested action: add favorite for 'root' to node0, node2, node4;
	 * add favorite for  'demo' to property of node1,
	 * 
	 * expectedValue : 4 ( number of favorite nodes by 'root')
	 * 
	 * @throws Exception
	 */
	public void testGetAllFavouriteNodesByUser() throws Exception {
		Node rootNode = session.getRootNode();
		Node testNode = rootNode.addNode("testNode");
		session.save();

		Node node0 = testNode.addNode("node0");
		Node node1 = testNode.addNode("node1");
		Node node2 = testNode.addNode("node2");
		Node node3 = testNode.addNode("node3");
		Node node4 = node3.addNode("node4");

		favoriteService.addFavorite(node0, "root");
		favoriteService.addFavorite(node1, "demo");
		favoriteService.addFavorite(node2, "root");
		favoriteService.addFavorite(node3, "root");
		favoriteService.addFavorite(node4, "root");

		assertEquals("testGetAllFavouriteNodesByUser failed!", 4, favoriteService
				.getAllFavoriteNodesByUser(
						rootNode.getSession().getWorkspace().getName(), "repository",
						"root").size());
		testNode.remove();
		session.save();
	}
}