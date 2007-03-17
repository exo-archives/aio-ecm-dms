/*
 * Created on Mar 4, 2005
 */
package org.exoplatform.workflow.webui.component;

import java.util.Map;

/**
 * @author benjaminmestrallet
 */
public class VariableMaps {

  private Map workflowVariables;
  private Map jcrVariables;
  
  public VariableMaps(Map workflowVariables, Map jcrVariables) {
    this.workflowVariables = workflowVariables;
    this.jcrVariables = jcrVariables;
  }
  
  public Map getJcrVariables() {
    return jcrVariables;
  }
  public Map getWorkflowVariables() {
    return workflowVariables;
  }
}
