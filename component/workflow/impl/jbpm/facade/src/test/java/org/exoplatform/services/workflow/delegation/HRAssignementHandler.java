/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.workflow.delegation;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 13 mai 2004
 */
public class HRAssignementHandler implements AssignmentHandler{


  public void assign(Assignable arg0, ExecutionContext assignmentContext) throws Exception {
    System.out.println("In selectActor of HRAssignementHandler : ");
    TaskInstance taskInstance = assignmentContext.getTaskInstance();
    System.out.println("  --> Previous actor : " + taskInstance.getPreviousActorId());
    if("bossOfBenj".equals(taskInstance.getPreviousActorId())){
      System.out.println("  --> Next actor : Hrofbenj");
      arg0.setActorId("Hrofbenj");
    }
  }

}