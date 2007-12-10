/*
 * Created on Mar 24, 2005
 */
package org.exoplatform.processes.contentvalidation;

import java.util.Date;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.jbpm.scheduler.exe.Timer;

/**
 * @author benjaminmestrallet
 */
public class SchedulePublicationTimerActionHandler extends MoveNodeActionHandler {

  private static final long serialVersionUID = 1L;

  public void execute(ExecutionContext context) {
    Date startDate = (Date) context.getVariable("startDate");
    Date currentDate = new Date();    
    if (startDate.before(currentDate)) {
      try {
        moveNode(context);
      } catch (Exception e) {       
        e.printStackTrace();
      }
      context.getToken().signal("move-done");
    } else {
      //Create and save the Action object 
      Delegation delegation = new Delegation();
      delegation.setClassName("org.exoplatform.processes.contentvalidation.MoveNodeActionHandler");
      delegation.setProcessDefinition(context.getProcessDefinition());
      
      Action moveAction = new Action();
      moveAction.setName("moveAction");
      moveAction.setActionDelegation(delegation);      
      context.getProcessDefinition().addAction(moveAction);      
          
      //create the timer
      Timer timer = new Timer(context.getToken());
      timer.setName("publicationTimer");            
      timer.setDueDate(startDate);
      timer.setGraphElement(context.getEventSource());
      timer.setTaskInstance(context.getTaskInstance());
      timer.setAction(moveAction);
      timer.setTransitionName("end");
      context.getSchedulerInstance().schedule(timer);
    }
  }

}