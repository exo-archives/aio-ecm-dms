package org.jbpm.taskmgmt.def;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jbpm.taskmgmt.exe.TaskInstance;

public interface TaskControllerHandler extends Serializable {

  /**
   * is called to prepare a task view.  this method extracts all information 
   * from the process context (optionally indirect) and returns a set of 
   * variables.  the task form is specified in terms of parameters.
   * @return a List of {@link org.jbpm.taskmgmt.exe.TaskFormParameter}s.
   */
  List getTaskFormParameters(TaskInstance taskInstance);
  
  /**
   * is called when a user submits parameters for a given task.  optionally 
   * this method can mark completion of the task.
   */
  void submitParameters(Map parameters, TaskInstance taskInstance);

}
