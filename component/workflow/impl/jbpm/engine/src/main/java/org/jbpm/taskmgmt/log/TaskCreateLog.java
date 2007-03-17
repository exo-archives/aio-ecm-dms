package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskCreateLog extends TaskLog {

  private static final long serialVersionUID = 1L;

  protected String taskActorId = null;

  public TaskCreateLog() {
  }

  public TaskCreateLog(TaskInstance taskInstance, String taskActorId) {
    super(taskInstance);
    this.taskActorId = taskActorId;
  }

  public String toString() {
    return "task-create["+taskInstance+"]";
  }
  
  public String getTaskActorId() {
    return taskActorId;
  }
}
