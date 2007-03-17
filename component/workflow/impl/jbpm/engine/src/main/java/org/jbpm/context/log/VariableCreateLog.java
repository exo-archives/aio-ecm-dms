package org.jbpm.context.log;

import org.jbpm.context.exe.*;

public class VariableCreateLog extends VariableLog {

  private static final long serialVersionUID = 1L;

  public VariableCreateLog() {
  }
  
  public VariableCreateLog(VariableInstance variableInstance) {
    super(variableInstance);
  }
  
  public String toString() {
    return "varcreate["+variableInstance+"]";
  }
}
