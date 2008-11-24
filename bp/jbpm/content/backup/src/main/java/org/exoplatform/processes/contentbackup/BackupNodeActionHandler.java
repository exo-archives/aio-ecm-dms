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
package org.exoplatform.processes.contentbackup;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * @author benjaminmestrallet
 */
public class BackupNodeActionHandler implements ActionHandler {

  private static final long serialVersionUID = 1L;

  private boolean executed = false;

  public void execute(ExecutionContext context) {
    try {
      if (executed)
        return;
      executed = true;
      backupNode(context);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      context.getToken().signal("backup-done");
      context.getSchedulerInstance().cancel("backupTimer", context.getToken());
    }
  }

  protected void backupNode(ExecutionContext context) throws Exception {
    String actionName = (String) context.getVariable("actionName");
    String nodePath = (String) context.getVariable("nodePath");
    String srcPath = (String) context.getVariable("srcPath");
    String srcWorkspace = (String) context.getVariable("srcWorkspace");
    String repository = (String) context.getVariable("repository");
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    CmsService cmsService = (CmsService) container.getComponentInstanceOfType(CmsService.class);
    ActionServiceContainer actionServiceContainer = (ActionServiceContainer) container
    .getComponentInstanceOfType(ActionServiceContainer.class);
    RepositoryService repositoryService = (RepositoryService) container
    .getComponentInstanceOfType(RepositoryService.class);
    Session session = repositoryService.getRepository(repository).getSystemSession(srcWorkspace);
    Node actionnableNode = (Node) session.getItem(srcPath);
    Node actionNode = actionServiceContainer.getAction(actionnableNode,actionName);

    String destWorkspace = actionNode.getProperty("exo:destWorkspace").getString();
    String destPath = actionNode.getProperty("exo:destPath").getString();

    String relPath = nodePath.substring(srcPath.length() + 1);
    if (!relPath.startsWith("/"))
      relPath = "/" + relPath;
    relPath = relPath.replaceAll("\\[\\d*\\]", "");
    cmsService.moveNode(nodePath, srcWorkspace, destWorkspace, destPath
        + relPath, repository);
    session.logout();
  }

}