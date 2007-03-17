package org.jbpm.context.log.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;

public class StringUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  String oldValue = null;
  String newValue = null;

  public StringUpdateLog() {
  }

  public StringUpdateLog(VariableInstance variableInstance, String oldValue, String newValue) {
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
