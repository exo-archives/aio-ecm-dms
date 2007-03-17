package org.jbpm.taskmgmt.def;

import java.io.*;
import java.util.*;

import org.jbpm.instantiation.*;

/**
 * is a process role (aka participant).
 */
public class Swimlane implements Serializable {
  
  private static final long serialVersionUID = 1L;

  long id = 0;
  private String name = null;  
  private Delegation assignmentDelegation = null;
  private TaskMgmtDefinition taskMgmtDefinition = null;
  private Set tasks = null;
  
  public Swimlane() {
  }

  public Swimlane(String name) {
    this.name = name;
  }

  /**
   * sets the taskMgmtDefinition unidirectionally.  use TaskMgmtDefinition.addSwimlane to create 
   * a bidirectional relation.
   */
  public void setTaskMgmtDefinition(TaskMgmtDefinition taskMgmtDefinition) {
    this.taskMgmtDefinition = taskMgmtDefinition;
  }

  // tasks
  /////////////////////////////////////////////////////////////////////////////

  public void addTask( Task task ) {
    if (tasks==null) tasks = new HashSet();
    tasks.add(task);
    task.setSwimlane(this);
  }

  public Set getTasks() {
    return tasks;
  }

  // other getters and setters
  /////////////////////////////////////////////////////////////////////////////

  public TaskMgmtDefinition getTaskMgmtDefinition() {
    return taskMgmtDefinition;
  }
  public Delegation getAssignmentDelegation() {
    return assignmentDelegation;
  }
  public void setAssignmentDelegation(Delegation assignmentDelegation) {
    this.assignmentDelegation = assignmentDelegation;
  }
  public String getName() {
    return name;
  }
  public long getId() {
    return id;
  }
}
