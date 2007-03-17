package org.jbpm.context.exe.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class StringInstance extends VariableInstance {
  
  private static final long serialVersionUID = 1L;
  
  protected String value = null;

  protected boolean supports(Class clazz) {
    return (String.class==clazz);
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new StringUpdateLog(this, this.value, (String)value));
    this.value = (String) value;
  }
}
