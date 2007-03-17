package org.jbpm.context.exe.variableinstance;

import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.context.log.variableinstance.ByteArrayUpdateLog;

public class ByteArrayInstance  extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected ByteArray value = null;

  protected boolean supports(Class clazz) {
    return (ByteArray.class.isAssignableFrom(clazz));
  }

  public Object getObject() {
    return value;
  }

  public void setObject(Object value) {
    token.addLog(new ByteArrayUpdateLog(this, this.value, (ByteArray) value));
    this.value = (ByteArray) value;
  }
}
