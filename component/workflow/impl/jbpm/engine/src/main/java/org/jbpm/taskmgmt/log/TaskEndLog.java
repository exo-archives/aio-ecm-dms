package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskEndLog extends TaskLog {

  private static final long serialVersionUID = 1L;
  
  public TaskEndLog() {
  }

  public TaskEndLog(TaskInstance taskInstance) {
    super(taskInstance);
  }

  public String toString() {
    return "task-end["+taskInstance+"]";
  }
}
