package org.jbpm.context.log;

import org.jbpm.context.exe.*;

public class VariableDeleteLog extends VariableLog {

  private static final long serialVersionUID = 1L;

  public VariableDeleteLog() {
  }

  public VariableDeleteLog(VariableInstance variableInstance) {
    super(variableInstance);
  }

  public String toString() {
    return variableInstance+" deleted";
  }

}
