package org.jbpm.context.exe.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class HibernateStringInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Object value = null;

  protected boolean supports(Class clazz) {
    return true;
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new HibernateStringUpdateLog(this, this.value, value));
    this.value = value;
  }
}
