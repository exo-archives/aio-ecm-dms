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
  
  private SessionProviderService   sessionProviderService_;
  
  private LinkManager   linkManager;
  
  private Session session;
  
  public NodeFinderImpl(RepositoryService repositoryService, SessionProviderService   sessionProviderService, LinkManager linkManager){
    this.repositoryService_ = repositoryService;
    this.sessionProviderService_ = sessionProviderService;
    this.linkManager = linkManager;
  }

  public Item getItem(String repository, String workspace, String absPath)
      throws PathNotFoundException, RepositoryException, RepositoryConfigurationException{
      this.session = null;
      Session session = getSession(repository, workspace);
      if (absPath.indexOf("/") != 0) throw new PathNotFoundException(absPath + " isn't absolute path");
      if (absPath.substring(1) == null || absPath.substring(1).length() == 0) return session.getItem(absPath);
      String[] split = absPath.substring(1).split("/");
      if (split == null && split.length == 0) throw new PathNotFoundException("Cant not find path: " + absPath);
      String realPath = getPath(session, absPath.substring(1), 0, split.length);
      if (this.session != null) return this.session.getItem(realPath); 
      return session.getItem(realPath);
  }

  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException {
    this.session = null;
    Session session = ancestorNode.getSession();
    String path = ancestorNode.getPath() + "/" + relativePath; 
    if (path.substring(1) == null || path.substring(1).length() == 0) return (Node)session.getItem(path);
    String[] split = path.substring(1).split("/");
    if (split == null && split.length == 0) throw new PathNotFoundException("Cant not find path: " + path);
    String realPath = getPath(session, path, 0, path.length());
    if (this.session != null) return (Node)this.session.getItem(realPath); 
    return (Node)session.getItem(realPath);
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
  public String getPath(Session session, String absPath, int fromIdx, int toIdx) throws PathNotFoundException, RepositoryException {
    if (fromIdx > toIdx) return "/" + absPath;
    String[] splitPath = absPath.split("/");
    int middIdx = (fromIdx + toIdx) >>> 1;
    String partPath = this.makePath(splitPath, 0, middIdx);
    Node realNodeLink = getNodeLink(session, partPath);
    String realPath = "";
    int variantIdx = 0;
    if (realNodeLink != null) {
      realPath = realNodeLink.getPath();
      absPath = realPath + absPath.substring(partPath.length() - 1);
      variantIdx = realPath.split("/").length - partPath.split("/").length;
      toIdx += variantIdx;
      middIdx += (variantIdx+1);
      this.session = realNodeLink.getSession();
      return getPath(this.session, absPath.substring(1), middIdx, toIdx);
    }
    if (this.session != null) session = this.session;
    realPath = getPath(session, absPath, fromIdx, middIdx-1);
    middIdx += (realPath.split("/").length - toIdx);
    toIdx = realPath.split("/").length - 1;
    absPath = realPath;
    return getPath(session, absPath.substring(1), middIdx, toIdx);
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
    return sessionProviderService_.getSessionProvider(null).getSession(workspace, manageableRepository);
  }
  
  
  /**
   * Get absolute path base on given symlinkPath
   * if path could be a link then 
   * @param session
   * @param symlinkPath
   * @throws RepositoryException
   */
  private String makePath(String[] splitString, int fromIdx, int toIdx) {
    String temp = "";
    if (splitString == null || splitString.length < toIdx)
      return temp;
    for(int i = fromIdx; i < toIdx; i++) {
      temp = temp + "/" + splitString[i];
    } 
    return temp;
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
    Node nodeLink = null;
    if (session.itemExists(path)) {
      nodeLink = (Node)session.getItem(path);
      if (nodeLink.isNodeType(NODETYPE_SYMLINK))
        return linkManager.getTarget(nodeLink);
      return nodeLink;
    }
    return null;
  }
}
