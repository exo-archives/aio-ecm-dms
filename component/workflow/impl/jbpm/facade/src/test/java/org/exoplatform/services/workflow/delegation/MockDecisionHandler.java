/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.workflow.delegation;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;


/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 12 mai 2004
 */
public class MockDecisionHandler implements DecisionHandler {

  public String decide(ExecutionContext executionContext) {
    System.out.println( "Start Date : " + executionContext.getVariable("start.date"));
    System.out.println("decide in MockDecisionHandler : " + executionContext.getNode().getName());    
    return "enough holidays";
  }

}
