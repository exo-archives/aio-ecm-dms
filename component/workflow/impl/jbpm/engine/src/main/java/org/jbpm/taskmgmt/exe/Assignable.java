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

/**
 * common superclass for {@link org.jbpm.taskmgmt.exe.TaskInstance}s and 
 * {@link org.jbpm.taskmgmt.exe.SwimlaneInstance}s used by 
 * the {@link org.jbpm.taskmgmt.def.AssignmentHandler} interface.
 */
public interface Assignable extends Serializable {

  /**
   * sets the responsible for this assignable object.
   * Use this method to assign the task into a user's personal 
   * task list.
   */
  public void setActorId(String actorId);
  
  /**
   * sets the resource pool for this assignable as a set of {@link PooledActor}s.
   * Use this method to offer the task to a group of users.  Each user in the group
   * can then take the task by calling the {@link #setActorId(String)}.
   */
  public void setPooledActors(String[] pooledActors);
}
