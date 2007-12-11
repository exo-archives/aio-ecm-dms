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
package org.jbpm.taskmgmt.exe;

import java.io.*;
import java.util.Set;

import org.jbpm.taskmgmt.def.*;

/**
 * is a process role for a one process instance.
 */
public class SwimlaneInstance implements Serializable, Assignable {

  private static final long serialVersionUID = 1L;

  long id = 0;
  protected String name = null;
  protected String actorId = null;
  protected Set pooledActors = null;
  protected Swimlane swimlane = null;
  protected TaskMgmtInstance taskMgmtInstance = null;
  
  public SwimlaneInstance() {
  }
  
  public SwimlaneInstance(Swimlane swimlane) {
    this.name = swimlane.getName();
    this.swimlane = swimlane;
  }

  public void setPooledActors(String[] actorIds) {
    this.pooledActors = PooledActor.createPool(actorIds);
  }

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Swimlane getSwimlane() {
    return swimlane;
  }
  public String getActorId() {
    return actorId;
  }
  public void setActorId(String actorId) {
    this.actorId = actorId;
  }
  public TaskMgmtInstance getTaskMgmtInstance() {
    return taskMgmtInstance;
  }
  public void setTaskMgmtInstance(TaskMgmtInstance taskMgmtInstance) {
    this.taskMgmtInstance = taskMgmtInstance;
  }
  public Set getPooledActors() {
    return pooledActors;
  }
  public void setPooledActors(Set pooledActors) {
    this.pooledActors = pooledActors;
  }
  
}
