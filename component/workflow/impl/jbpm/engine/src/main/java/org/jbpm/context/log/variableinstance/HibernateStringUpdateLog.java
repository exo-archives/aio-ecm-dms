package org.jbpm.context.log.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.*;

public class HibernateStringUpdateLog extends VariableUpdateLog {

  private static final long serialVersionUID = 1L;

  Object oldValue = null;
  Object newValue = null;

  public HibernateStringUpdateLog() {
  }

  public HibernateStringUpdateLog(VariableInstance variableInstance, Object oldValue, Object newValue) {
    super(variableInstance);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public Object getOldValue() {
    return null;
  }

  public Object getNewValue() {
    return null;
  }

}
