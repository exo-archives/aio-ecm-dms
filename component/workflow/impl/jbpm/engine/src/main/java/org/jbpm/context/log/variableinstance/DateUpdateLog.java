package org.jbpm.context.log.variableinstance;

import java.util.*;
import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;

public class DateUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  Date oldValue = null;
  Date newValue = null;

  public DateUpdateLog() {
  }

  public DateUpdateLog(VariableInstance variableInstance, Date oldValue, Date newValue) {
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
