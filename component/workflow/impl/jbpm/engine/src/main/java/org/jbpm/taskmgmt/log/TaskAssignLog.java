package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskAssignLog extends TaskLog {

  private static final long serialVersionUID = 1L;

  protected String taskOldActorId = null;
  protected String taskNewActorId = null;

  public TaskAssignLog() {
  }

  public TaskAssignLog(TaskInstance taskInstance, String taskOldActorId, String taskNewActorId) {
    super(taskInstance);
    this.taskOldActorId = taskOldActorId;
    this.taskNewActorId = taskNewActorId;
  }

  public String toString() {
    return "task-assign["+taskNewActorId+","+taskInstance+"]";
  }
  
  public String getTaskNewActorId() {
    return taskNewActorId;
  }
  public String getTaskOldActorId() {
    return taskOldActorId;
  }
}
