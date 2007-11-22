/*
 * Created on Mar 2, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ExtendedNode;

/**
 * @author benjaminmestrallet
 */
public class Utils {
  
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
      if(node.hasNode(token)) {
        node = node.getNode(token) ;
      }else {
        node = node.addNode(token, nodetype);
        if (node.canAddMixin("exo:privilegeable")){
          node.addMixin("exo:privilegeable");
        }
        if(permissions != null){          
          ((ExtendedNode)node).setPermissions(permissions);
        }
      }      
    }
    return node;
  }

}