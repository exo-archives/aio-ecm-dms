/*
 * Created on Apr 22, 2005
 */
package org.exoplatform.processes.contentvalidation;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;

/**
 * @author benjaminmestrallet
 */
public class EvaluatorDecisionHandler implements DecisionHandler{

  public String decide(ExecutionContext context) {
    String requester = (String) context.getVariable("requester");
    if(requester == null || "".equals(requester) || "anonymous".equals(requester))
      return "end";
    return "change request";
  }

}
