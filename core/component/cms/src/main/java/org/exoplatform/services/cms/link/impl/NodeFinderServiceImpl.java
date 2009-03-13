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

import org.exoplatform.services.cms.link.NodeFinderService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL Author : Hoang Van Hung hunghvit@gmail.com
 * Mar 14, 2009
 */
public class NodeFinderServiceImpl implements NodeFinderService, Startable {

  public Item getItem(String repository, String workspace, String absPath)
      throws PathNotFoundException, RepositoryException {

    return null;
  }

  public Node getNode(Node ancestorNode, String relativePath) throws PathNotFoundException,
      RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public void start() {
    // TODO Auto-generated method stub
  }

  public void stop() {
    // TODO Auto-generated method stub

  }

  private String getPath(String fromPath, String destPath) {
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
  }

  /**
   * Check the node corresponding to the absolute path could be a link or not
   * 
   * @param absPath path to node
   * @return true if corresponding node could be a link false if corresponding
   *         node could'n be a link
   * @throws ItemNotFoundException if the target node cannot be found
   * @throws RepositoryException if an unexpected error occurs while retrieving
   *           the target node
   */
  private boolean checkLink(String absPath) {
    // / for testing
    if (!absPath.contains("link") || (absPath.contains("link") && absPath.indexOf("link/") == absPath.length() - 4))
      return true;
    return false;
  }

  private String getLink(String path) {
    // for testing
    return path.replace("link", "REALLink");
  }

  private int getMidSlash(String path) {
    
  }
  public static void main(String[] args) {
    NodeFinderServiceImpl nodeFinder = new NodeFinderServiceImpl();
    
    //String path = "/aaa/1link/bb/cc/2link/eee/3link/4link/ffff/5link/g";
    String path = "/aaa/link1/bb/cc";
    System.out.println(nodeFinder.getPath("",path));
  }
}
