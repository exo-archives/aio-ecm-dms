package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.SwimlaneInstance;

public class SwimlaneAssignLog extends SwimlaneLog {

  private static final long serialVersionUID = 1L;

  protected String swimlaneOldActorId = null;
  protected String swimlaneNewActorId = null;

  public SwimlaneAssignLog() {
  }

  public SwimlaneAssignLog(SwimlaneInstance swimlaneInstance, String swimlaneOldActorId, String swimlaneNewActorId) {
    super(swimlaneInstance);
    this.swimlaneOldActorId = swimlaneOldActorId;
    this.swimlaneNewActorId = swimlaneNewActorId;
  }

  public String toString() {
    return "swimlane-assign["+swimlaneNewActorId+","+swimlaneInstance+"]";
  }

  public String getSwimlaneNewActorId() {
    return swimlaneNewActorId;
  }
  public String getSwimlaneOldActorId() {
    return swimlaneOldActorId;
  }
}
