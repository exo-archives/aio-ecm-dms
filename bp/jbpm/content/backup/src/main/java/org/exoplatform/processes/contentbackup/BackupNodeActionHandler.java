/*
 * Created on Mar 24, 2005
 */
package org.exoplatform.processes.contentbackup;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
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
    PortalContainer container = PortalContainer.getInstance();
    CmsService cmsService = (CmsService) container
        .getComponentInstanceOfType(CmsService.class);
    ActionServiceContainer actionServiceContainer = (ActionServiceContainer) container
        .getComponentInstanceOfType(ActionServiceContainer.class);
    RepositoryService repositoryService = (RepositoryService) container
         .getComponentInstanceOfType(RepositoryService.class);
    Session session = repositoryService.getRepository().getSystemSession(srcWorkspace);
    Node actionnableNode = (Node) session.getItem(srcPath);
    Node actionNode = actionServiceContainer.getAction(actionnableNode,
        actionName);

    String destWorkspace = actionNode.getProperty("exo:destWorkspace")
        .getString();
    String destPath = actionNode.getProperty("exo:destPath").getString();

    
    String relPath = nodePath.substring(srcPath.length() + 1);
    if (!relPath.startsWith("/"))
      relPath = "/" + relPath;
    relPath = relPath.replaceAll("\\[\\d*\\]", "");
    cmsService.moveNode(nodePath, srcWorkspace, destWorkspace, destPath
        + relPath);
  }

}