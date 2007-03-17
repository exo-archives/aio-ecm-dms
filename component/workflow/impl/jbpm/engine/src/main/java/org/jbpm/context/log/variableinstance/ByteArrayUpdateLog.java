package org.jbpm.context.log.variableinstance;

import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.context.log.VariableUpdateLog;

public class ByteArrayUpdateLog extends VariableUpdateLog {
  
  private static final long serialVersionUID = 1L;
  
  ByteArray oldValue = null;
  ByteArray newValue = null;

  public ByteArrayUpdateLog() {
  }

  public ByteArrayUpdateLog(VariableInstance variableInstance, ByteArray oldValue, ByteArray newValue) {
    super(variableInstance);
    this.oldValue = (oldValue!=null ? new ByteArray(oldValue) : null );
    this.newValue = (newValue!=null ? new ByteArray(newValue) : null );
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public String toString() {
    String toString = null;
    if ( (oldValue==null)
         && (newValue==null) ) {
      toString = variableInstance+" remained null";
    } else if ( (oldValue!=null)
                && (oldValue.equals(newValue) )
              ) {
      toString = variableInstance+" unchanged";
    } else {
      toString = variableInstance+" binary content differs";
    }
    return toString;
  }
}
