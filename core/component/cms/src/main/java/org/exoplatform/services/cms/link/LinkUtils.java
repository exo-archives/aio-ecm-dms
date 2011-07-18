/*
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
 */
package org.exoplatform.services.cms.link;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 1 avr. 2009  
 */
public final class LinkUtils {

  /**
   * Convert a path of type /my/../folder or /my/./folder to a real path 
   * @param path the path to convert
   * @return the real absolute path
   */
  private static final String EXO_SYMLINK = "exo:symlink";
  
  public static String evaluatePath(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + path +  "' must be an absolute path");
    }
    path = cleanPath(path);
    int index;
    while ((index = path.indexOf("/..")) != -1) {
      if (index == 0) {
        path = path.substring(3);
      } else {
        path = createPath(getParentPath(path.substring(0, index)), path.substring(index + 3));        
      }
    }
    while ((index = path.indexOf("/.")) != -1) {
      if (index == 0) {
        path = path.substring(2);
      } else {
        path = createPath(path.substring(0, index), path.substring(index + 2));
      }
    }
    return path.length() == 0 ? "/" : path;
  }
  
  /**
   * Gives the name of the item according to the given absolute path
   * @param path the absolute of the item
   * @return the item name
   */
  public static String getItemName(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + path +  "' must be an absolute path");
    }
    path = cleanPath(path);
    int index = path.lastIndexOf('/');
    return path.substring(index + 1);
  }
  
  /**
   * Appends the parentPath and the relativePath to give a full absolute path
   */
  public static String createPath(String parentPath, String relativePath) {
    if (!parentPath.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + parentPath +  "' must be an absolute path");
    }
    parentPath = cleanPath(parentPath);
    relativePath = cleanPath(relativePath);
    if (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    StringBuilder path = new StringBuilder(128);
    path.append(parentPath);
    if (relativePath.length() > 0) {
      if (!parentPath.equals("/")) {
        path.append('/');
      }
      path.append(relativePath);      
    }
    return path.toString();
  }
  
  /**
   * Gives the total depth of the given absolute path
   * @param path an absolute path
   * @return the depth of the path
   */
  public static int getDepth(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + path +  "' must be an absolute path");
    }
    path = cleanPath(path);
    if (path.equals("/")) {
      return 0;
    }
    return path.substring(1).split("/").length;
  }

  /**
   * Gives the ancestor of the given path corresponding to the given depth knowing that:
   * <ul>
   * <li><i>depth</i> = 0 returns the root node.
   * <li><i>depth</i> = 1 returns the child of the root node along the path
   * to <i>this</i> <code>Item</code>.
   * <li><i>depth</i> = 2 returns the grandchild of the root node along the
   * path to <i>this</i> <code>Item</code>.
   * <li>And so on to <i>depth</i> = <i>n</i>, where <i>n</i> is the depth
   * of <i>this</i> <code>Item</code>, which returns <i>this</i>
   * <code>Item</code> itself.
   * </ul>
   */
  public static String getAncestorPath(String path, int depth) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + path +  "' must be an absolute path");
    }
    path = cleanPath(path);
    if (depth == 0) {
      return "/";
    }
    String[] subpath = path.substring(1).split("/");
    StringBuilder result = new StringBuilder(128);
    for (int i = 0; i < depth; i++) {
      result.append('/');
      result.append(subpath[i]);
    }
    return result.toString();
  }
  
  /**
   * Gives the parent path of the given path
   * @param path an absolute path
   * @return the parent path
   */
  public static String getParentPath(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path '" + path +  "' must be an absolute path");
    }
    path = cleanPath(path);
    if (path.equals("/")) {
      return "/";
    }
    int index = path.lastIndexOf('/');
    if (index == 0) {
      return "/";      
    }
    return path.substring(0, index);
  }
  
  public static NodeFinder getNodeFinder() {
    ExoContainer context = ExoContainerContext.getCurrentContainer();
    return (NodeFinder) context.getComponentInstance(NodeFinder.class);
  }
  
  public static LinkManager getLinkManager() {
    ExoContainer context = ExoContainerContext.getCurrentContainer();
    return (LinkManager) context.getComponentInstance(LinkManager.class);
  }
  
  private static String cleanPath(String path) {
    // Remove unnecessary '/'
    path = path.replaceAll("/+", "/");
    if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }
  
  public static List<Node> getAllSymlinks(Node targetNode, String repoName) throws Exception {
    List<Node> result = new ArrayList<Node>();
    if(!targetNode.isNodeType("mix:referenceable")) return result;
    ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService =(RepositoryService)myContainer.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository  = repositoryService.getRepository(repoName);
    String[] workspaces = repository.getWorkspaceNames();
    String systemWS = 
      repository.getConfiguration().getSystemWorkspaceName();
    String queryString = new StringBuilder().append("SELECT * FROM ").
                                             append(EXO_SYMLINK).
                                             append(" WHERE exo:uuid='").
                                             append(targetNode.getUUID()).append("'").
                                             append(" AND exo:workspace='").
                                             append(targetNode.getSession().getWorkspace().getName()).
                                             append("'").toString();
    
    for (String workspace : workspaces) {
      SessionProvider sessionProvider = workspace.equals(systemWS) ? SessionProviderFactory.createSystemProvider() 
                                                                   : SessionProviderFactory.createSessionProvider();
      Session session = sessionProvider.getSession(workspace, repository);
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator iter = queryResult.getNodes();
      while (iter.hasNext()) {
        result.add(iter.nextNode());
      }
    }        
    return result;
  }
  
}
