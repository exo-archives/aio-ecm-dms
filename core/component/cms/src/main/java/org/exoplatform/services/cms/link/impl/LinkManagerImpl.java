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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Mar 13, 2009
 */
public class LinkManagerImpl implements LinkManager {
  final static private String    SYMLINK      = "exo:symlink";

  final static private String    WORKSPACE    = "exo:workspace";

  final static private String    UUID         = "exo:uuid";

  final static private String    PRIMARY_TYPE = "exo:primaryType";

  private SessionProviderService providerService_;

  public LinkManagerImpl(SessionProviderService providerService) throws Exception {
    providerService_ = providerService;
  }

  public Node createLink(Node parent, String linkType, Node target) throws RepositoryException {
    return createLink(parent, linkType, target, null);
  }

  public Node createLink(Node parent, Node target) throws RepositoryException {
    return createLink(parent, null, target, null);
  }

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
      Node nodeLink = parent.addNode(linkName, linkType);
      nodeLink.setProperty(WORKSPACE, target.getSession().getWorkspace().getName());
      nodeLink.setProperty(UUID, target.getUUID());
      nodeLink.setProperty(PRIMARY_TYPE, target.getPrimaryNodeType().getName());
      nodeLink.getSession().save();
      return nodeLink;
    }
    return null;
  }

  public Node getTarget(Node link, boolean system) throws ItemNotFoundException,
      RepositoryException {
    Session session = getSession(link, system);
    String uuid = link.getProperty(UUID).getString();
    try {
      return session.getNodeByUUID(uuid);
    } catch (ItemNotFoundException e) {
      // e.printStackTrace();
      Session systemSession = null;
      try {
        systemSession = getSession((ManageableRepository) link.getSession().getRepository(), link
            .getProperty(WORKSPACE).getString(), true, providerService_);
        return systemSession.getNodeByUUID(uuid);
      } catch (ItemNotFoundException e1) {
        // e1.printStackTrace();
        link.remove();
        link.getSession().save();
      } finally {
        systemSession.logout();
      }
    } finally {
      if (session != null)
        session.logout();
    }
    return null;
  }

  private Session getSession(Node link, boolean system) throws RepositoryException {
    String workspaceLink = link.getSession().getWorkspace().getName();
    String workspaceTarget = link.getProperty(WORKSPACE).getString();
    if (workspaceLink.equals(workspaceTarget))
      return link.getSession();
    return getSession((ManageableRepository) link.getSession().getRepository(), workspaceTarget,
        system, providerService_);
  }

  private Session getSession(ManageableRepository manageRepository, String workspaceName,
      boolean system, SessionProviderService service) throws RepositoryException {
    if (system)
      return service.getSystemSessionProvider(null).getSession(workspaceName, manageRepository);
    return service.getSessionProvider(null).getSession(workspaceName, manageRepository);
  }

  public Node getTarget(Node link) throws ItemNotFoundException, RepositoryException {
    return getTarget(link, false);
  }

  public boolean isTargetReachable(Node link) throws RepositoryException {
    Session session = getSession(link, false);
    try {
      session.getNodeByUUID(link.getProperty(UUID).getString());
    } catch (ItemNotFoundException e) {
      return false;
    }
    return true;
  }

  public Node updateLink(Node link, Node target) throws RepositoryException {
    link.setProperty(UUID, target.getUUID());
    link.getSession().save();
    return link;
  }
}
