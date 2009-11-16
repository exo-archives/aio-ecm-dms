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

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.cms.documents.FavoriteService;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
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
  
  public FavoriteServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, LinkManager linkManager, 
      NodeFinder nodeFinder) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.linkManager = linkManager;
    this.nodeFinder = nodeFinder;
  }

  /**
   * {@inheritDoc}
   */
  public void addFavorite(SessionProvider sessionProvider, Node node, String userName) throws Exception {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getAllFavoriteNodesByUser(String workspace, String repository, SessionProvider sessionProvider, String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void removeFavorite(SessionProvider sessionProvider, Node node, String userName) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  private Node getUserFavoriteFolder(SessionProvider sessionProvider, String userName) throws Exception {
    Node userNode = nodeHierarchyCreator.getUserNode(sessionProvider, userName);
    String favoritePath = nodeHierarchyCreator.getJcrPath(FAVORITE_ALIAS);
    return userNode.getNode(favoritePath);
  }
  
}
