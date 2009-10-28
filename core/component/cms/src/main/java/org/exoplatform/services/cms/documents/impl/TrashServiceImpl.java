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
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh
 * minh.dang@exoplatform.com Oct 6, 2009 3:39:53 AM
 */
public class TrashServiceImpl implements TrashService {

	private RepositoryService repositoryService;

	public TrashServiceImpl(RepositoryService repositoryService)
			throws Exception {
		this.repositoryService = repositoryService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveToTrash(Node node, String trashPath, String trashWorkspace,
			String repository, SessionProvider sessionProvider)
			throws Exception {

		String nodeName = node.getName();
		Session nodeSession = node.getSession();
		String nodeWorkspaceName = nodeSession.getWorkspace().getName();

		if (!node.isNodeType(EXO_RESTORE_LOCATION)) {
			node.addMixin(EXO_RESTORE_LOCATION);
			node.setProperty(RESTORE_PATH, node.getPath());
			node.setProperty(RESTORE_WORKSPACE, nodeWorkspaceName);
			nodeSession.save();

			ManageableRepository manageableRepository 
									= repositoryService.getRepository(repository);
			Session trashSession = sessionProvider.getSession(trashWorkspace,
									manageableRepository);
			String actualTrashPath = trashPath	+ (trashPath.endsWith("/") ? "" : "/") + nodeName;
			if (trashSession.getWorkspace().getName().equals(
					nodeSession.getWorkspace().getName())) {
				trashSession.getWorkspace().move(node.getPath(),
						actualTrashPath);
			} else {
				trashSession.getWorkspace().clone(nodeWorkspaceName,
						node.getPath(), actualTrashPath, true);
				node.remove();
			}
			nodeSession.save();
			trashSession.save();
			trashSession.logout();
		} 
		nodeSession.logout();
	}

	/**
	 * {@inheritDoc}
	 */
	//parameter:restorePath->trashNodePath
	public void restoreFromTrash(Node trashHomeNode, String trashNodePath, 
								String repository,
								SessionProvider sessionProvider) throws Exception {

		Session trashNodeSession = trashHomeNode.getSession();
		Node trashNode = (Node)trashNodeSession.getItem(trashNodePath);
		String trashWorkspace = trashNodeSession.getWorkspace().getName();
		String restoreWorkspace = trashNode.getProperty(RESTORE_WORKSPACE).getString();
		String restorePath = trashNode.getProperty(RESTORE_PATH).getString();

		ManageableRepository manageableRepository = repositoryService
				.getRepository(repository);
		Session restoreSession 
					= sessionProvider.
							getSession(restoreWorkspace,
							  		   manageableRepository);

		if (restoreWorkspace.equals(trashWorkspace)) {
			trashNodeSession.getWorkspace().move(trashNodePath, restorePath);
		} else {
			restoreSession.getWorkspace().clone(
					trashWorkspace, trashNodePath, restorePath, true);
			trashNodeSession.getItem(trashNodePath).remove();
		}

		((Node) restoreSession.getItem(restorePath))
				.removeMixin(EXO_RESTORE_LOCATION);
		trashNodeSession.save();
		restoreSession.save();
		
		trashNodeSession.logout();
		if (!restoreWorkspace.equals(trashWorkspace)) 		
			restoreSession.logout();
	}

	
	private List<Node> selectNodesByQuery(String trashPath, String trashWorkspace,
								   		  String repository, SessionProvider sessionProvider,
								   		  String queryString, String language) throws Exception {
		List<Node> ret = new ArrayList<Node>();
		ManageableRepository manageableRepository 
								= repositoryService.getRepository(repository);
		Session session = sessionProvider.getSession(trashWorkspace, manageableRepository);
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(queryString, language);
		QueryResult queryResult = query.execute();
	
		NodeIterator iter = queryResult.getNodes();
	 	System.out.println(iter.getSize());
	 	while (iter.hasNext()) {
	 		ret.add(iter.nextNode());
	 	}
	
	 	return ret;
	 }
	
	 public List<Node> getAllNodeInTrash(String trashPath,
										 String trashWorkspace, String repository,
										 SessionProvider sessionProvider) throws Exception {
	
	 // String trashPathTail = (trashPath.endsWith("/"))? "" : "/";
	 StringBuilder query = new StringBuilder("SELECT * FROM nt:base WHERE exo:restorePath IS NOT NULL");
	
	 // System.out.println(query);
	 return selectNodesByQuery(trashPath, trashWorkspace, repository,
			 				   sessionProvider, query.toString(), Query.SQL);
	 }
	
	 public List<Node> getAllNodeInTrashByUser(String trashPath, String trashWorkspace, String repository,
		 								   	   SessionProvider sessionProvider, String userName) 
		 								   	   throws Exception {
	
	 StringBuilder query = new StringBuilder("SELECT * FROM nt:base WHERE exo:restorePath IS NOT NULL AND exo:owner='").
	 								 append(userName).
	 								 append("'");
	 return selectNodesByQuery(trashPath, trashWorkspace, repository,
			 				   sessionProvider, query.toString(), Query.SQL);
	 }
	
	
	
//	/**
//	 * {@inheritDoc}
//	 */
//	// use query , jcr:mixinTypes contains 
//	public List<Node> getAllNodeInTrash(String trashPath,
//			String trashWorkspace, String repository,
//			SessionProvider sessionProvider) throws Exception {
//
//		ManageableRepository manageableRepository 
//								= repositoryService.getRepository(repository);
//		Session trashSession = sessionProvider.
//								  getSession(trashWorkspace, manageableRepository);
//
//		// String trashPathTail = (trashPath.endsWith("/"))? "" : "/";
//		Node trashHomeNode = (Node) trashSession.getItem(trashPath);
//		NodeIterator iter = trashHomeNode.getNodes();
//		
//		List<Node> ret = new ArrayList<Node>();
//		while (iter.hasNext())
//			ret.add(iter.nextNode());
//
//		trashSession.logout();
//		return ret;
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public List<Node> getAllNodeInTrashByUser(String trashPath,
//			String trashWorkspace, String repository,
//			SessionProvider sessionProvider, String userName) throws Exception {
//
//		ManageableRepository manageableRepository = repositoryService
//				.getRepository(repository);
//		Session trashSession = sessionProvider.getSession(trashWorkspace,
//				manageableRepository);
//
//		// String trashPathTail = (trashPath.endsWith("/"))? "" : "/";
//		Node trashHomeNode = (Node) trashSession.getItem(trashPath);
//		NodeIterator iter = trashHomeNode.getNodes();
//		List<Node> ret = new ArrayList<Node>();
//		while (iter.hasNext()) {
//			Node node = iter.nextNode();
//			if (node.getProperty("exo:owner").getString().equals(userName))
//				ret.add(node);
//		}
//
//		trashSession.logout();
//		return ret;
//	}	
}
