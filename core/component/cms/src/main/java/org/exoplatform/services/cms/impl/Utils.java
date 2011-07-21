/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.cms.impl;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * @author benjaminmestrallet
 */
public class Utils {

  public static final String EXO_SYMLINK = "exo:symlink";
  public static final String SYSTEM_NAME = "System";
  
  public static Node makePath(Node rootNode, String path, String nodetype)
  throws PathNotFoundException, RepositoryException {
    return makePath(rootNode, path, nodetype, null);
  }

  @SuppressWarnings("unchecked")
  public static Node makePath(Node rootNode, String path, String nodetype, Map permissions)
  throws PathNotFoundException, RepositoryException {    
    String[] tokens = path.split("/") ;
    Node node = rootNode;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      if(token.length() > 0) {
        if(node.hasNode(token)) {
          node = node.getNode(token) ;
        } else {
          node = node.addNode(token, nodetype);
          if (node.canAddMixin("exo:privilegeable")){
            node.addMixin("exo:privilegeable");
          }
          if(permissions != null){          
            ((ExtendedNode)node).setPermissions(permissions);
          }
        }      
      }
    }
    return node;
  }
  
  public static String getRealNodeOwner(Node node) {
    try {
      if (node.isNodeType(EXO_SYMLINK)) {
        LinkManager linkManager = getService(LinkManager.class);
        node = linkManager.getTarget(node, true);
      }
      if(node.hasProperty("exo:owner")) {
        String userName =  node.getProperty("exo:owner").getString();
        return getUserFullName(userName);
      }
    } catch (Exception e) {}
    return SystemIdentity.ANONIM;
  }
  
  public static String getUserFullName(String userId) {
    if (SystemIdentity.SYSTEM.equals(userId)) {
      return SYSTEM_NAME;
    }
    try {
      OrganizationService service = getService(OrganizationService.class);
      User userAccount = service.getUserHandler().findUserByName(userId);
      return userAccount.getFullName();
    } catch (Exception e) {}
    return SystemIdentity.ANONIM;    
  }
  
  public static <T> T getService(Class<T> clazz) {
    ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
    return clazz.cast(myContainer.getComponentInstanceOfType(clazz));
  }

}