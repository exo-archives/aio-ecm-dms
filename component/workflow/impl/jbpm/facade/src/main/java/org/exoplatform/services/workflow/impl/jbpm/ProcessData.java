package org.exoplatform.services.workflow.impl.jbpm;

import org.exoplatform.services.workflow.Process;
import org.jbpm.graph.def.ProcessDefinition;

public class ProcessData implements Process {
  
  private ProcessDefinition def_;

  public ProcessData(ProcessDefinition def) {
    def_ = def;
  }

  public String getId() {
    return ""+def_.getId();
  }

  public String getName() {
    return ""+def_.getName();
  }

  public int getVersion() {
    return def_.getVersion();
  }

  public String getStartStateName() {
    return def_.getStartState().getName();
  }

}
