package org.jbpm.context.log.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;


public class DoubleUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  Double oldValue = null;
  Double newValue = null;

  public DoubleUpdateLog() {
  }

  public DoubleUpdateLog(VariableInstance variableInstance, Double oldValue, Double newValue) {
    super(variableInstance);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
}
