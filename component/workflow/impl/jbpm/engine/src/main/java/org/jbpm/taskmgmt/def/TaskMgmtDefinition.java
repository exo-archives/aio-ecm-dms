package org.jbpm.taskmgmt.def;

import java.util.*;

import org.jbpm.module.def.*;
import org.jbpm.module.exe.*;
import org.jbpm.taskmgmt.exe.*;

/**
 * extends a process definition with information about tasks, swimlanes (for task assignment).
 */
public class TaskMgmtDefinition extends ModuleDefinition {

  private static final long serialVersionUID = 1L;
  
  protected Map swimlanes = null; 
  protected Map tasks = null;
  protected Task startTask = null;

  // constructors /////////////////////////////////////////////////////////////
  
  public TaskMgmtDefinition() {
  }

  public ModuleInstance createInstance() {
    return new TaskMgmtInstance(this);
  }

  // swimlanes ////////////////////////////////////////////////////////////////

  public void addSwimlane( Swimlane swimlane ) {
    if (swimlanes==null) swimlanes = new HashMap();
    swimlanes.put(swimlane.getName(), swimlane);
    swimlane.setTaskMgmtDefinition(this);
  }

  public Map getSwimlanes() {
    return swimlanes;
  }
  
  public Swimlane getSwimlane( String swimlaneName ) {
    if (swimlanes==null) return null;
    return (Swimlane) swimlanes.get( swimlaneName );
  }

  // tasks ////////////////////////////////////////////////////////////////////

  public void addTask( Task task ) {
    if (tasks==null) tasks = new HashMap();
    tasks.put(task.getName(), task);
    task.setTaskMgmtDefinition(this);
  }

  public Map getTasks() {
    return tasks;
  }
  
  public Task getTask( String taskName ) {
    if (tasks==null) return null;
    return (Task) tasks.get( taskName );
  }

  // start task ///////////////////////////////////////////////////////////////

  public Task getStartTask() {
    return startTask;
  }
  public void setStartTask(Task startTask) {
    this.startTask = startTask;
  }
}
