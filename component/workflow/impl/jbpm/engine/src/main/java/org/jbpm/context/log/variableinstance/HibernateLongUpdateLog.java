package org.jbpm.context.log.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;


public class HibernateLongUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  Object oldValue = null;
  Object newValue = null;

  public HibernateLongUpdateLog() {
  }

  public HibernateLongUpdateLog(VariableInstance variableInstance, Object oldValue, Object newValue) {
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
