/*
 * Created on Mar 2, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;

/**
 * @author benjaminmestrallet
 */
public class Utils {

  public static Map<String, String[]> getReadPermissions() {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put("any", new String[] {PermissionType.READ});
    return permissions;
  }
  
  public static Node makePath(Node rootNode, String path, String nodetype)
      throws PathNotFoundException, RepositoryException {
    return makePath(rootNode, path, nodetype, null);
  }

  public static Node makePath(Node rootNode, String path, String nodetype, Map permissions)
      throws PathNotFoundException, RepositoryException {
    String[] tokens = StringUtils.split(path, '/');
    Node node = rootNode;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      if (!node.hasNode(token)) {
        node = node.addNode(token, nodetype);
        if(permissions != null){
          if (node.canAddMixin("exo:privilegeable")){
            node.addMixin("exo:privilegeable");
          }
          ((ExtendedNode)node).setPermissions(permissions);
        }
      } else {
        node = node.getNode(token);
      }
    }
    return node;
  }

}