/*
 * Created on Mar 24, 2005
 */
package org.exoplatform.processes.contentbackup;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.jbpm.scheduler.exe.Timer;

/**
 * @author benjaminmestrallet
 */
public class ScheduleBackupTimerActionHandler implements ActionHandler {

  private static final long serialVersionUID = 1L;

  public void execute(ExecutionContext context) {
    Session session = null ;
    try {
      String nodePath = (String) context.getVariable("nodePath");
      String srcWorkspace = (String) context.getVariable("srcWorkspace");
      String repository = (String) context.getVariable("repository");
      PortalContainer container = PortalContainer.getInstance();
      RepositoryService repositoryService = 
        (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      session = repositoryService.getRepository(repository).getSystemSession(srcWorkspace);
      Node srcNode = (Node) session.getItem(nodePath);      
      if(!srcNode.isNodeType("exo:published")) {
        context.getToken().signal("backup-done");
        session.logout();
        return;
      }

      Date endDate = srcNode.getProperty("exo:endPublication").getDate().getTime();
      Date currentDate = new Date();
      if (endDate.after(currentDate)) {
        // Create and save the Action object
        Delegation delegation = new Delegation();
        delegation.setClassName("org.exoplatform.processes.contentbackup.BackupNodeActionHandler");
        delegation.setProcessDefinition(context.getProcessDefinition());

        Action moveAction = new Action();
        moveAction.setName("backupAction");
        moveAction.setActionDelegation(delegation);
        context.getProcessDefinition().addAction(moveAction);

        // create the timer
        
        Timer timer = new Timer(context.getToken());
        timer.setName("backupTimer");
        timer.setDueDate(endDate);
        timer.setGraphElement(context.getEventSource());
        timer.setTaskInstance(context.getTaskInstance());
        timer.setAction(moveAction);
        context.getSchedulerInstance().schedule(timer);
      } else {
        context.getToken().signal("backup-done");
      }
      session.logout() ;
    } catch (Exception ex) {
      if(session != null && !session.isLive()) {
        session.logout();
      }
      ex.printStackTrace();
    }
  }

}