package org.jbpm.taskmgmt.log;

import org.jbpm.logging.log.ProcessLog;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

public abstract class SwimlaneLog extends ProcessLog {

  private static final long serialVersionUID = 1L;

  protected SwimlaneInstance swimlaneInstance = null;

  public SwimlaneLog() {
  }

  public SwimlaneLog(SwimlaneInstance swimlaneInstance) {
    this.swimlaneInstance = swimlaneInstance;
  }
  
  public SwimlaneInstance getSwimlaneInstance() {
    return swimlaneInstance;
  }
}
