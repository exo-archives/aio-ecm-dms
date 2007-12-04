/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.processes.holidays;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;


/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 12 mai 2004
 */
public class BossAssignementHandler implements AssignmentHandler{

  public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception {
    assignable.setActorId("member:/organization/management/executive-board");
  }
}
