package org.jbpm.context.exe.variableinstance;

import java.util.*;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class DateInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Date value = null;

  protected boolean supports(Class clazz) {
    return (Date.class==clazz);
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new DateUpdateLog(this, this.value, (Date) value));
    this.value = (Date) value;
  }
}
