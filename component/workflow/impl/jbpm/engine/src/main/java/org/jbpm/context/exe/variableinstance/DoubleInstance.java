package org.jbpm.context.exe.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class DoubleInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Double value = null;

  protected boolean supports(Class clazz) {
    return (Double.class==clazz);
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new DoubleUpdateLog(this, this.value, (Double) value));
    this.value = (Double) value;
  }
}
