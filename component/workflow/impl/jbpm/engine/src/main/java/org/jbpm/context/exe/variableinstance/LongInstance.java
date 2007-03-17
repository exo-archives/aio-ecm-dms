package org.jbpm.context.exe.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class LongInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Long value = null;

  protected boolean supports(Class clazz) {
    return (Long.class==clazz);
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new LongUpdateLog(this, this.value, (Long)value));
    this.value = (Long) value;
  }

}
