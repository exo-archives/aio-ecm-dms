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
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Mar 13, 2009
 */
public class LinkManagerImpl implements LinkManager {
  final static private String SYMLINK      = "exo:symlink";

  final static private String WORKSPACE    = "exo:workspace";

  final static private String UUID         = "exo:uuid";

  final static private String PRIMARY_TYPE = "exo:primaryType";

  private RepositoryService   repositoryService_;

  public LinkManagerImpl(RepositoryService repositoryService) throws Exception {
    repositoryService_ = repositoryService;
  }

  public Node createLink(Node parent, String linkType, Node target) throws RepositoryException {
    if (!target.isNodeType(SYMLINK)) {
      if (target.canAddMixin("mix:referenceable")) {
        target.addMixin("mix:referenceable");
        target.getSession().save();
      }
      if (linkType == null) linkType = SYMLINK;
      Node nodeLink = parent.addNode(SYMLINK, linkType);
      nodeLink.setProperty(WORKSPACE, target.getSession().getWorkspace().getName());
      nodeLink.setProperty(UUID, target.getUUID());
      nodeLink.setProperty(PRIMARY_TYPE, target.getPrimaryNodeType().getName());
      nodeLink.getSession().save();
      return nodeLink;
    }
    return null;
  }

  public Node createLink(Node parent, Node target) throws RepositoryException {
    return createLink(parent, null, target);
  }

  public Node getTarget(Node link, boolean system) throws ItemNotFoundException,
      RepositoryException, Exception {
    Session session = null;
    if (system) session = getSystemSession();
    else session = link.getSession();
    String uuid = link.getProperty(UUID).getString();
    try {
      return session.getNodeByUUID(uuid);
    } catch (ItemNotFoundException e) {
      e.printStackTrace();
      try {
        session = getSystemSession();
        return session.getNodeByUUID(uuid);
      } catch (ItemNotFoundException e1) {
        e1.printStackTrace();
        link.remove();
        link.getSession().save();
      } finally {
        session.logout();
      }
    } finally {
      if (system) session.logout();
    }
    return null;
  }

  public Node getTarget(Node link) throws ItemNotFoundException, RepositoryException, Exception {
    return getTarget(link, false);
  }

  public boolean isTargetReachable(Node link) throws RepositoryException {
    try {
      link.getSession().getNodeByUUID(link.getProperty(UUID).getString());
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

  private Session getSystemSession() throws Exception {
    String repositoryName = repositoryService_.getCurrentRepository().getConfiguration().getName();
    ManageableRepository manageableRepository = repositoryService_.getRepository(repositoryName);
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return manageableRepository.getSystemSession(workspace);
  }
}
