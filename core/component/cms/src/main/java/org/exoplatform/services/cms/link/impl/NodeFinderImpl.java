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
  
  public static final String NODETYPE_SYMLINK = "exo:symlink";
  
  public static final String EXO_UUID         = "exo:uuid";
  public static final String EXO_WORKSPACE    = "exo:workspace";
  public static final String EXO_PRIMARY      = "exo:primaryType";
  
  private RepositoryService repositoryService_;
  
  private SessionProviderService   sessionProviderService_;
  
  private LinkManager   linkManager;
  
  public NodeFinderImpl(RepositoryService repositoryService, SessionProviderService   sessionProviderService, LinkManager linkManager){
    this.repositoryService_ = repositoryService;
    this.sessionProviderService_ = sessionProviderService;
    this.linkManager = linkManager;
  }

  public Item getItem(String repository, String workspace, String absPath)
      throws PathNotFoundException, RepositoryException, RepositoryConfigurationException{
      return getItem(repositoryService_.getRepository(repository), workspace, absPath);
  }

  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException {
    if (relativePath == null || relativePath.startsWith("/")) throw new PathNotFoundException("Invalid relative path: " + relativePath);
    String absPath = ancestorNode.getPath() + "/" + relativePath;
    Session session = ancestorNode.getSession();
    return (Node)getItem((ManageableRepository)session.getRepository(), session.getWorkspace().getName(),absPath);
  }
  
  private Item getItem(ManageableRepository manageableRepository, String workspace, String absPath)
  throws PathNotFoundException, RepositoryException{
    Session session = getSession(manageableRepository, workspace);
    if (!absPath.startsWith("/")) throw new PathNotFoundException(absPath + " isn't absolute path");
    if (absPath.length() == 1) return session.getRootNode();
    return this.getItem(session, absPath, 0, absPath.split("/").length);
  }

  /**
   * Get item by  absolute path
   * @param session       Session of user
   * @param absPath       input absolute path to node
   * @param partPath      Absolute path to ancestor node of finding node
   * @return              absolute path to non-symlink node
   */
  
  private Item getItem(Session session, String absPath, int fromIdx, int toIdx) throws PathNotFoundException, RepositoryException {
    /* if node corresponding to absPath could be a link */
    if (session.itemExists(absPath)) return this.getNodeLink(session, absPath);
    /* if node corresponding to absPath is not a link then spliting absPath and check */
    String[] splitPath = absPath.substring(1).split("/");
    int middIdx = (fromIdx + toIdx) >>> 1;
    String partPath = this.makePath(splitPath, 0, middIdx);
    int variantIdx = 0;
    Node realNodeLink = null;
    String realPath = "";
    /*
     *  Check absolute path from start position to middle position
     *  If path could be a link
     */
    if (session.itemExists(partPath)) {
      realNodeLink = this.getNodeLink(session, partPath);
      if (realNodeLink == null) throw new PathNotFoundException("Can't find path: " + absPath);
      realPath = realNodeLink.getPath();
      absPath = realPath + absPath.substring(partPath.length());
      variantIdx = realPath.split("/").length - partPath.split("/").length;
      toIdx += variantIdx;
      middIdx += (variantIdx+1);
      if (middIdx <= toIdx) return getItem(realNodeLink.getSession(), absPath, middIdx, toIdx);
      return realNodeLink;
    }
    /* If path is not a link then look up the sub path */
    if (fromIdx < middIdx) {
      realNodeLink = (Node) getItem(session, absPath, fromIdx, middIdx - 1);
      realPath = realNodeLink.getPath();
      splitPath = absPath.substring(1).split("/");
      partPath = this.makePath(splitPath, middIdx - 1, splitPath.length);
      variantIdx = realPath.substring(1).split("/").length - middIdx;
      absPath = realPath + partPath;
      middIdx += (variantIdx + 1);
      toIdx = absPath.substring(1).split("/").length;
      //absPath = realPath;
      /* After finding out the real absolute path from start to middle position then check path from middle to end position */
      if (middIdx <= toIdx) return getItem(realNodeLink.getSession(), absPath, middIdx, toIdx);
      return realNodeLink;
    }
    return this.getNodeLink(session, partPath);
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
  private String makePath(String[] splitString, int from, int toIdx) {
    //User StringBuilder
    StringBuilder temp = new StringBuilder();
    for(int i = from; i < toIdx; i++) {
      temp.append("/" + splitString[i]);
    } 
    return temp.toString();
  }
  
  /**
   * Get node by given path: if Node of path is exo:symlink then find out target node
   * if path to node is not exist then return null;                           
   * @param session
   * @param path
   * @return
   * @throws PathNotFoundException
   * @throws RepositoryException
   */
  private Node getNodeLink(Session session, String path) throws PathNotFoundException, RepositoryException {
      Node nodeLink = (Node)session.getItem(path);
      if (nodeLink.isNodeType(NODETYPE_SYMLINK))
        return linkManager.getTarget(nodeLink);
      return nodeLink;
  }
}
