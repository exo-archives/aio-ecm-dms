package org.jbpm.context.log;

import org.jbpm.context.exe.*;

public abstract class VariableUpdateLog extends VariableLog {
  
  public VariableUpdateLog() {
  }

  public VariableUpdateLog(VariableInstance variableInstance) {
    super(variableInstance);
  }

  public abstract Object getOldValue();
  public abstract Object getNewValue();
  
  public String toString() {
    return "varupdate["+variableInstance+"="+getNewValue()+"]";
  }

  public void undo() {
    variableInstance.setValue( getOldValue() );
  }
}
