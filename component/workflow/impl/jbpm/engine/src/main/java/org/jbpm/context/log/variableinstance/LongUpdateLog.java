package org.jbpm.context.log.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;


public class LongUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  Long oldValue = null;
  Long newValue = null;

  public LongUpdateLog() {
  }

  public LongUpdateLog(VariableInstance variableInstance, Long oldValue, Long newValue) {
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
