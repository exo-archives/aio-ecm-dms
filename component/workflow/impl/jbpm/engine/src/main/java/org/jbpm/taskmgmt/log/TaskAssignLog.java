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
package org.jbpm.taskmgmt.log;

import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskAssignLog extends TaskLog {

  private static final long serialVersionUID = 1L;

  protected String taskOldActorId = null;
  protected String taskNewActorId = null;

  public TaskAssignLog() {
  }

  public TaskAssignLog(TaskInstance taskInstance, String taskOldActorId, String taskNewActorId) {
    super(taskInstance);
    this.taskOldActorId = taskOldActorId;
    this.taskNewActorId = taskNewActorId;
  }

  public String toString() {
    return "task-assign["+taskNewActorId+","+taskInstance+"]";
  }
  
  public String getTaskNewActorId() {
    return taskNewActorId;
  }
  public String getTaskOldActorId() {
    return taskOldActorId;
  }
}
