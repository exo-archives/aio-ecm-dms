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
