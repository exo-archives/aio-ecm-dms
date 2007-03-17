package org.jbpm.module.def;

import java.io.*;

import org.jbpm.graph.def.*;
import org.jbpm.module.exe.*;

public abstract class ModuleDefinition implements Serializable {

  long id = 0;
  protected String name = null;
  protected ProcessDefinition processDefinition = null;
  
  public ModuleDefinition() {
  }
  
  public abstract ModuleInstance createInstance();

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
}
