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