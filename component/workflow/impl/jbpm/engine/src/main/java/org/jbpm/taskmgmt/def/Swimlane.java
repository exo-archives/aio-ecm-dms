/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
