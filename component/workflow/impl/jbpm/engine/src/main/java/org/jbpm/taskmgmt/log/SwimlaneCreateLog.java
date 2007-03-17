package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.SwimlaneInstance;

public class SwimlaneCreateLog extends SwimlaneLog {

  private static final long serialVersionUID = 1L;

  protected String swimlaneActorId = null;

  public SwimlaneCreateLog() {
  }

  public SwimlaneCreateLog(SwimlaneInstance swimlaneInstance, String swimlaneActorId) {
    super(swimlaneInstance);
    this.swimlaneActorId = swimlaneActorId;
  }

  public String toString() {
    return "swimlane-create["+swimlaneInstance+"]";
  }
  
  public String getSwimlaneActorId() {
    return swimlaneActorId;
  }
}
