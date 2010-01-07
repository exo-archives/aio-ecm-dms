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
package org.exoplatform.services.cms.link.impl;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeLinkAware;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 * Mar 13, 2009
 */
public class LinkManagerImpl implements LinkManager {

  private final static String    SYMLINK      = "exo:symlink";

  private final static String    WORKSPACE    = "exo:workspace";

  private final static String    UUID         = "exo:uuid";

  private final static String    PRIMARY_TYPE = "exo:primaryType";

  private final static Log       LOG  = LogFactory.getLog(LinkManagerImpl.class);

  private final SessionProviderService providerService_;
  
  public LinkManagerImpl(SessionProviderService providerService) throws Exception {
    providerService_ = providerService;
  }

  /**
   * {@inheritDoc}
   */
  public Node createLink(Node parent, String linkType, Node target) throws RepositoryException {
    return createLink(parent, linkType, target, null);
  }

  /**
   * {@inheritDoc}
   */
  public Node createLink(Node parent, Node target) throws RepositoryException {
    return createLink(parent, null, target, null);
  }

  /**
   * {@inheritDoc}
   */
  public Node createLink(Node parent, String linkType, Node target, String linkName)
      throws RepositoryException {
    if (!target.isNodeType(SYMLINK)) {
      if (target.canAddMixin("mix:referenceable")) {
        target.addMixin("mix:referenceable");
        target.getSession().save();
      }
      if (linkType == null || linkType.trim().length() == 0)
        linkType = SYMLINK;
      if (linkName == null || linkName.trim().length() == 0)
        linkName = target.getName();
      Node linkNode = parent.addNode(linkName, linkType);
      try {
        updateAccessPermissionToLink(linkNode, target);
      } catch(Exception e) {
        LOG.error("CAN NOT UPDATE ACCESS PERMISSIONS FROM TARGET NODE TO LINK NODE", e);
      }
      linkNode.setProperty(WORKSPACE, target.getSession().getWorkspace().getName());
      linkNode.setProperty(UUID, target.getUUID());
      linkNode.setProperty(PRIMARY_TYPE, target.getPrimaryNodeType().getName());
      linkNode.getSession().save();
      return linkNode;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Node getTarget(Node link, boolean system) throws ItemNotFoundException,
      RepositoryException {
    String uuid = link.getProperty(UUID).getString();
    Session session = getSession(link, system);
    Node targetNode = session.getNodeByUUID(uuid);
    session.logout();
    return targetNode;
  }

  private Session getSession(Node link, boolean system) throws RepositoryException {
    String workspaceLink = link.getSession().getWorkspace().getName();
    String workspaceTarget = link.getProperty(WORKSPACE).getString();
    if (workspaceLink.equals(workspaceTarget))
      return link.getSession();
    return getSession((ManageableRepository) link.getSession().getRepository(), workspaceTarget,
        system);
  }

  private Session getSession(ManageableRepository manageRepository, String workspaceName,
      boolean system) throws RepositoryException {
    if (system)
      return providerService_.getSystemSessionProvider(null).getSession(workspaceName, manageRepository);
    return providerService_.getSessionProvider(null).getSession(workspaceName, manageRepository);
  }

  /**
   * {@inheritDoc}
   */
  public Node getTarget(Node link) throws ItemNotFoundException, RepositoryException {
    return getTarget(link, false);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTargetReachable(Node link) throws RepositoryException {
    Session session = null;
    try {
      session = getSession(link, false);
      session.getNodeByUUID(link.getProperty(UUID).getString());
    } catch (ItemNotFoundException e) {
      return false;
    } finally {
      if(session != null) session.logout();
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public Node updateLink(Node linkNode, Node targetNode) throws RepositoryException {
    if (targetNode.canAddMixin("mix:referenceable")) {
      targetNode.addMixin("mix:referenceable");
      targetNode.getSession().save();
    }
    try {
      updateAccessPermissionToLink(linkNode, targetNode);
    } catch(Exception e) {
      LOG.error("CAN NOT UPDATE ACCESS PERMISSIONS FROM TARGET NODE TO LINK NODE", e);
    }
    linkNode.setProperty(UUID, targetNode.getUUID());
    linkNode.setProperty(PRIMARY_TYPE, targetNode.getPrimaryNodeType().getName());
    linkNode.setProperty(WORKSPACE, targetNode.getSession().getWorkspace().getName());
    linkNode.getSession().save();
    return linkNode;
  }

  /**
   * {@inheritDoc}
   */  
  public boolean isLink(Item item) throws RepositoryException {
    if (item instanceof Node) {
      Node node = (Node) item;
      if (node instanceof NodeLinkAware) {
        node = ((NodeLinkAware) node).getRealNode();
      }
      return node.isNodeType(SYMLINK);
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */  
  public String getTargetPrimaryNodeType(Node link) throws RepositoryException {
    return link.getProperty(PRIMARY_TYPE).getString();
  }
  
  /**
   * Update the permission between two given node  
   * @param linkNode    The node to update permission
   * @param targetNode  The target node to get permission 
   * @throws Exception
   */
  private void updateAccessPermissionToLink(Node linkNode, Node targetNode) throws Exception {
    if(canChangePermission(linkNode)) {
      if(linkNode.canAddMixin("exo:privilegeable")) {
        linkNode.addMixin("exo:privilegeable");
        ((ExtendedNode)linkNode).setPermission(getNodeOwner(linkNode),PermissionType.ALL);
      }
      removeCurrentIdentites(linkNode);
      Map<String, String[]> perMap = new HashMap<String, String[]>();
      List<String> permsList = new ArrayList<String>();
      List<String> idList = new ArrayList<String>();
      for(AccessControlEntry accessEntry : ((ExtendedNode)targetNode).getACL().getPermissionEntries()) {
        if(!idList.contains(accessEntry.getIdentity())) {
          idList.add(accessEntry.getIdentity());
          permsList = ((ExtendedNode)targetNode).getACL().getPermissions(accessEntry.getIdentity());
          perMap.put(accessEntry.getIdentity(), permsList.toArray(new String[permsList.size()]));
        }
      }
      ((ExtendedNode)linkNode).setPermissions(perMap);
    }
  }
  
  /**
   * Remove all identity of the given node
   * @param linkNode  The node to remove all identity
   * @throws AccessControlException
   * @throws RepositoryException
   */
  private void removeCurrentIdentites(Node linkNode) throws AccessControlException, RepositoryException {
    for(AccessControlEntry accessEntry : ((ExtendedNode)linkNode).getACL().getPermissionEntries()) {
      if(canRemovePermission(linkNode, accessEntry.getIdentity())) {
        ((ExtendedNode) linkNode).removePermission(accessEntry.getIdentity());
      }
    }
  }
  
  /**
   * Remove the permission from the given node
   * @param node      The node to remove permission
   * @param identity  The identity of the permission
   * @return
   * @throws ValueFormatException
   * @throws PathNotFoundException
   * @throws RepositoryException
   */
  private boolean canRemovePermission(Node node, String identity) throws ValueFormatException, 
        PathNotFoundException, RepositoryException {
    String owner = getNodeOwner(node);
    if(identity.equals(SystemIdentity.SYSTEM)) return false;
    if(owner != null && owner.equals(identity)) return false;
    return true;
  }
  
  /**
   * Get the owner of the given node
   * @param node      The node to get owner
   * @return
   * @throws ValueFormatException
   * @throws PathNotFoundException
   * @throws RepositoryException
   */
  private String getNodeOwner(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException {
    if(node.hasProperty("exo:owner")) {
      return node.getProperty("exo:owner").getString();
    }
    return null;
  }
  
  /**
   * Check permission of the given node
   * @param node      The Node to check permission
   * @return
   * @throws RepositoryException
   */
  private boolean canChangePermission(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.CHANGE_PERMISSION);
      return true;
    } catch(AccessControlException e) {
      return false;
    }
  }  
}
