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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL Author : Hoang Van Hung hunghvit@gmail.com
 * Mar 14, 2009
 */
public class NodeFinderImpl implements NodeFinder, Startable {
  
  public static final String NODETYPE_SYMLINK = "exo:symlink";
  
  public static final String EXO_UUID         = "exo:uuid";
  public static final String EXO_WORKSPACE    = "exo:workspace";
  public static final String EXO_PRIMARY      = "exo:primaryType";
  
  private RepositoryService repositoryService_;
  
  public NodeFinderImpl(RepositoryService repositoryService){
    this.repositoryService_ = repositoryService;
  }

  public Item getItem(String repository, String workspace, String absPath)
      throws PathNotFoundException, RepositoryException, RepositoryConfigurationException{
      Session session = getSession(repository, workspace);
      String absrealPath = getRealPath(session, absPath);
      if (absrealPath != null && absrealPath.length() > 0)
        return session.getItem(absrealPath);
      return session.getItem(getPath(session, absPath, ""));
  }

  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException, RepositoryConfigurationException {
    Session session = ancestorNode.getSession();
    String path = ancestorNode.getPath() + "/" + relativePath; 
    String absPath = getPath(session, path, "");
    return (Node)session.getItem(absPath);
  }

  public void start() {
    // TODO Auto-generated method stub
  }

  public void stop() {
    // TODO Auto-generated method stub
  }

  /**
   * Get actual absolute path to Node
   * @param session       Session of user
   * @param absPath       input absolute path to node
   * @param partPath      Absolute path to ancestor node of finding node
   * @return              absolute path to non-symlink node
   */
  public String getPath(Session session, String absPath, String partPath) throws PathNotFoundException, RepositoryException {
      if (partPath == null || partPath.length() == 0) {
        int idxSlash = absPath.substring(1).indexOf("/");
        partPath = absPath.substring(0, idxSlash + 1);
      }
      List<String> listPath = this.queryPath(session, partPath + "/");
      partPath = "";
      if (!listPath.isEmpty()) {
        for(String tempPath : listPath) {
          if (absPath.indexOf(tempPath) != -1) {
            partPath = tempPath;
             break;
          }
        }
      }
      if (partPath.length() != 0) {
        String realPath = getRealPath(session, partPath);
        absPath = realPath + absPath.substring(partPath.length());
        partPath = realPath;
        realPath = getRealPath(session, absPath);
        if (realPath!= null && realPath.length() > 0)
          return realPath;
        return getPath(session, absPath, partPath);
      } 
    return "";
  }


  /**
   * Get absolute path of all exo:symlink node
   * @param session      Session of user
   * @param path         path to lookup exo:symlink node
   * @throws RepositoryException
   */
  public List<String> queryPath(Session session, String path) throws RepositoryException {
    String statement = "SELECT * FROM exo:symlink WHERE jcr:path LIKE '" + path + "%'";
    List<String> listPath = new ArrayList<String>(); 
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(statement, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator iterNode = result.getNodes();
    if (iterNode.hasNext()) {
      while (iterNode.hasNext()) {
        Node nodeResult = iterNode.nextNode();
        listPath.add(nodeResult.getPath());
      }
    }
    return listPath;
  }
  
  /**
   * Get session of user in given workspace and repository
   * @param repository
   * @param workspace
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  
  private Session getSession(String repository, String workspace) throws RepositoryException, RepositoryConfigurationException{
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    return SessionProviderFactory.createSessionProvider().getSession(workspace, manageableRepository);
  }
  
  /**
   * Get absolute path base on given symlinkPath
   * if path could be a link then 
   * @param session
   * @param symlinkPath
   * @throws RepositoryException
   */
  private String getRealPath(Session session, String symlinkPath) throws RepositoryException {
    if (session.itemExists(symlinkPath)) {
      Node node = (Node) session.getItem(symlinkPath);
      if (node.isNodeType(NODETYPE_SYMLINK)) {
        String uuid = node.getProperty(EXO_UUID).getString();
        return session.getNodeByUUID(uuid).getPath();
      }
      return symlinkPath;
    }
    return "";
  }
}
