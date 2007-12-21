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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PooledActor implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String actorId = null;
  protected Set taskInstances = null;
  protected SwimlaneInstance swimlaneInstance = null;

  public static Set createPool(String[] actorIds) {
    Set pooledActors = new HashSet();
    for (int i=0; i<actorIds.length; i++) {
      pooledActors.add(new PooledActor(actorIds[i]));
    }
    return pooledActors;
  }

  public PooledActor() {
  }

  public PooledActor(String actorId) {
    this.actorId = actorId;
  }
  
  public void addTaskInstance(TaskInstance taskInstance) {
    if (taskInstances==null) taskInstances = new HashSet();
    taskInstances.add(taskInstance);
  }
  public Set getTaskInstances() {
    return taskInstances;
  }
  public void removeTaskInstance(TaskInstance taskInstance) {
    if (taskInstances!=null) {
      taskInstances.remove(taskInstance);
    }
  }

  public String getActorId() {
    return actorId;
  }
  public void setActorId(String actorId) {
    this.actorId = actorId;
  }
  public SwimlaneInstance getSwimlaneInstance() {
    return swimlaneInstance;
  }
  public void setSwimlaneInstance(SwimlaneInstance swimlaneInstance) {
    this.swimlaneInstance = swimlaneInstance;
  }
  public long getId() {
    return id;
  }
}
