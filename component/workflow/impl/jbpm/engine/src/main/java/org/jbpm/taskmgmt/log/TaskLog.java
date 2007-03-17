package org.jbpm.taskmgmt.log;

import org.jbpm.logging.log.ProcessLog;
import org.jbpm.taskmgmt.exe.TaskInstance;

public abstract class TaskLog extends ProcessLog {

  private static final long serialVersionUID = 1L;

  protected TaskInstance taskInstance = null;

  public TaskLog() {
  }

  public TaskLog(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
  }
  
  public TaskInstance getTaskInstance() {
    return taskInstance;
  }
}
