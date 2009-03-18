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
      throws PathNotFoundException, RepositoryException {
    try {
      Session session = getSession(repository, workspace);
      return session.getItem(getPath(session, absPath));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return null;
  }

  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException {
    Session session = ancestorNode.getSession();
    String path = ancestorNode.getPath() + "/" + relativePath; 
    String absPath = getPath(session, path);
    return (Node)session.getItem(absPath);
  }

  public void start() {
    // TODO Auto-generated method stub
  }

  public void stop() {
    // TODO Auto-generated method stub

  }

  public String getPath(Session session, String absPath) {
    try {
      int idxSlash = absPath.substring(1).indexOf("/");
      String path = absPath.substring(0, idxSlash + 2);
      List<String> listPath = this.queryPath(session, path);
      String partPath = "";
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
       // absPath.replace(partPath, realPath);
        realPath = getRealPath(session, absPath);
        if (realPath!= null && realPath.length() > 0)
          return realPath;
        return getPath(session, absPath);
      } 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }


  /**
   * Get path to exo:symlink node
   * @param session
   * @param statement
   * @return
   * @throws RepositoryException
   */
  public List<String> queryPath(Session session, String path) throws RepositoryException {
    String statement = "SELECT * FROM exo:symlink WHERE jcr:path LIKE '" + path + "%'";
    List<String> listPath = new ArrayList<String>(); 
    System.out.println("Query node");
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(statement, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator iterNode = result.getNodes();
    if (iterNode.hasNext()) {
      while (iterNode.hasNext()) {
        Node nodeResult = iterNode.nextNode();
        listPath.add(nodeResult.getPath());
      }
    } else {
      System.out.println("\n\n khong co data");
    }
    return listPath;
  }
  
  private Session getSession(String repository, String workspace) throws Exception{
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    return SessionProviderFactory.createSessionProvider().getSession(workspace, manageableRepository);
  }
  
  /**
   * Get absolute path base on given symlinkPath
   * if path could be a link then 
   * @param session
   * @param symlinkPath
   * @return absolute path
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
  
  /*private String getPath(String fromPath, String destPath) {
    String midPath = destPath.substring(0, fromPath.length() + (destPath.length() - fromPath.length())
        / 2);
    midPath = midPath.substring(0, midPath.lastIndexOf("/"));
    if (checkLink(midPath)) {
      int midPathLen = fromPath.length();
      fromPath = getLink(midPath);
      destPath = fromPath + destPath.substring(midPathLen);
    } else {
      destPath = midPath;
    }
    if (destPath.equals(fromPath))
      return midPath;
    midPath = getPath(fromPath, destPath);
    return midPath;
  }*/
}
