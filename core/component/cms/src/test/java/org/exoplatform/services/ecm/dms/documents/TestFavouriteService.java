package org.exoplatform.services.ecm.dms.documents;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.exoplatform.services.cms.documents.FavouriteService;
import org.exoplatform.services.cms.documents.impl.FavouriteServiceImpl;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class TestFavouriteService extends BaseDMSTestCase {
	
	final static public String EXO_FAVOURITE_NODE = "exo:favourite";
	final static public String EXO_FAVOURITER_PROPERTY = "exo:favouriter";
	
	private FavouriteService favouriteService;
	
	public void setUp() throws Exception {
		super.setUp();
		favouriteService = new FavouriteServiceImpl(repositoryService);
	}
	
	/**
	 * test method addFavourite 
	 * input: 		/testAddFavourite   node 
	 * tested action: add 3 user names 'root', 'demo', 'James' 
	 * into exo:favouriter property of the node above 
	 * expectedValue : 3 (length of exo:favouriter property of /testAddFavourite node)
	 * 
	 * @throws Exception
	 */
	public void testAddFavourite() throws Exception {
		Node rootNode = session.getRootNode();
		Node testAddFavouriteNode = rootNode.addNode("testAddFavourite");
		favouriteService.addFavourite(testAddFavouriteNode, "root");
		favouriteService.addFavourite(testAddFavouriteNode, "demo");
		favouriteService.addFavourite(testAddFavouriteNode, "James");

		Property p = testAddFavouriteNode.getProperty(EXO_FAVOURITER_PROPERTY);
		assertEquals("testAddFavourite failed!", 3, p.getValues().length);
		
		testAddFavouriteNode.remove();
		session.save();
	}
	
	/**
	 * test method removeFavourite 
	 * input: 		/testAddFavourite   node 
	 * tested action: add 4 user names 'root', 'James', 'John' and Marry into exo:favouriter property of the node above
	 * 				  remove 2 user names into exo:favourite property of the node above 
	 * expectedValue : 2 (length of exo:favouriter property of /testAddFavourite node)
	 * 
	 * @throws Exception
	 */
	public void testRemoveFavourite() throws Exception {
		Node rootNode = session.getRootNode();
		Node testRemove = rootNode.addNode("testRemoveFavourite");
		favouriteService.addFavourite(testRemove, "root");
		favouriteService.addFavourite(testRemove, "James");
		favouriteService.addFavourite(testRemove, "John");
		favouriteService.addFavourite(testRemove, "Marry");
		
		favouriteService.removeFavourite(testRemove, "root");
		favouriteService.removeFavourite(testRemove, "James");
		
		Property p = testRemove.getProperty(EXO_FAVOURITER_PROPERTY);
		assertEquals("testRemoveFavourite failed!", 2, p.getValues().length);
		
		boolean found = false;
		for (Value value : p.getValues())
			if ("root".equals(value.getString()) || "James".equals(value.getString()))
				found = true;
		assertEquals("testRemoveFavourite failed!", false, found);
	
		testRemove.remove();
		session.save();
	}
	
	/**
	 * test method getAllFavouriteNodes 
	 * input: 		/node0
	 * 				/node1	
	 * 				/node2
	 * 				/node3
	 * 				/node3/node4
	 * tested action: add user names 'root' into exo:favouriter property of all nodes above
	 * 				   
	 * expectedValue : 5 ( number of favorite nodes)
	 * 
	 * @throws Exception
	 */
	public void testGetAllFavouriteNodes() throws Exception {
		Node rootNode = session.getRootNode();
		Node testNode = rootNode.addNode("testNode");
		
		Node node0 = testNode.addNode("node0");
		Node node1 = testNode.addNode("node1");
		Node node2 = testNode.addNode("node2");
		Node node3 = testNode.addNode("node3");
		Node node4 = node3.addNode("node4");
		
		favouriteService.addFavourite(node0, "root");
		favouriteService.addFavourite(node1, "root");
		favouriteService.addFavourite(node2, "root");
		favouriteService.addFavourite(node3, "root");
		favouriteService.addFavourite(node4, "root");
		
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
		SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);

		assertEquals("testGetAllFavourite failed!", 5, 
				favouriteService.getAllFavouriteNodes(
						rootNode.getSession().getWorkspace().getName(), 
						"repository", 
						sessionProvider).size());
		testNode.remove();
		session.save();
	}
	
	/**
	 * test method getAllFavouriteNodesByUser 
	 * input: 		/node0
	 * 				/node1	
	 * 				/node2
	 * 				/node3
	 * 				/node3/node4
	 * tested action: add user names 'root' into exo:favouriter property of node0, node2, node4
	 * 				  add user names 'demo' into exo:favouriter property of node1,
	 * 				   
	 * expectedValue : 4 ( number of favorite nodes by current user)
	 * 
	 * @throws Exception
	 */	
	public void testGetAllFavouriteNodesByUser() throws Exception {
		Node rootNode = session.getRootNode();
		Node testNode = rootNode.addNode("testNode");
		
		Node node0 = testNode.addNode("node0");
		Node node1 = testNode.addNode("node1");
		Node node2 = testNode.addNode("node2");
		Node node3 = testNode.addNode("node3");
		Node node4 = node3.addNode("node4");
		
		favouriteService.addFavourite(node0, "root");
		favouriteService.addFavourite(node1, "demo");
		favouriteService.addFavourite(node2, "root");
		favouriteService.addFavourite(node4, "root");
		
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
		SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);

		assertEquals("testGetAllFavouriteNodesByUser failed!", 4, 
				favouriteService.getAllFavouriteNodesByUser(
						rootNode.getSession().getWorkspace().getName(), 
						"repository", 
						sessionProvider, session.getUserID()).size());
		testNode.remove();
		session.save();	
	}	
	
}