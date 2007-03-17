package org.jbpm.module.exe;

import java.io.*;

import org.jbpm.graph.exe.*;

public class ModuleInstance implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected ProcessInstance processInstance = null;
  
  public ModuleInstance() {
  }

  public long getId() {
    return id;
  }
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
}
