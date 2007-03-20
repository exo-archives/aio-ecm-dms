/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.processes.contentvalidation;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 */
public class ValidatorAssignementHandler implements AssignmentHandler{

  public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception {
    String validator = (String) executionContext.getVariable("exo:validator");
    assignable.setActorId(validator);    
  }
}
