package org.jbpm.context.log;

import org.jbpm.context.exe.*;
import org.jbpm.logging.log.*;

public abstract class VariableLog extends ProcessLog {

  private static final long serialVersionUID = 1L;
  
  protected VariableInstance variableInstance = null;
  
  public VariableLog() {
  }
  
  public VariableLog(VariableInstance variableInstance) {
    this.variableInstance = variableInstance;
  }

  public VariableInstance getVariableInstance() {
    return variableInstance;
  }
}
