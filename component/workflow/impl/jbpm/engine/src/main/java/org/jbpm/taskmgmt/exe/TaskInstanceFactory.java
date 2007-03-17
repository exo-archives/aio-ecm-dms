package org.jbpm.taskmgmt.exe;

import java.io.Serializable;

public interface TaskInstanceFactory extends Serializable {
  
  TaskInstance createTaskInstance();

}
