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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.instantiation.Delegation;
import org.jbpm.taskmgmt.exe.TaskFormParameter;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * is a controller for one task. this object either delegates to a custom
 * {@link org.jbpm.taskmgmt.def.TaskControllerHandler} or it is configured
 * with {@link org.jbpm.context.def.VariableAccess}s to perform the default 
 * behaviour of the controller functionality for a task.
 */
public class TaskController implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private long id = 0;

  /**
   * allows the user to specify a custom task controller handler. if this is
   * specified, the other member variables are ignored. so either a
   * taskControllerDelegation is specified or the variable- and signalMappings
   * are specified, but not both.
   */
  private Delegation taskControllerDelegation = null;

  /**
   * maps process variable names (java.lang.String) to VariableAccess objects.
   */
  private List variableAccesses = null;
  
  public TaskController() {
  }
  
  /**
   * extract the list of TaskFormParameter's from the process variables. 
   */
  public List getTaskFormParameters(TaskInstance taskInstance) {
    List taskFormParameters = null;
    
    if (taskControllerDelegation != null) {
      TaskControllerHandler taskControllerHandler = (TaskControllerHandler) taskControllerDelegation.instantiate();
      taskFormParameters = taskControllerHandler.getTaskFormParameters(taskInstance);

    } else {
      taskFormParameters = new ArrayList();

      Token token = taskInstance.getToken();
      ProcessInstance processInstance = token.getProcessInstance();
      ContextInstance contextInstance = (ContextInstance) processInstance.getInstance(ContextInstance.class);

      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.isReadable()) {
          Object value = contextInstance.getVariable(variableAccess.getVariableName(), token);
          taskFormParameters.add(new TaskFormParameter(variableAccess, value));
        }
      }
    }
    return taskFormParameters;
  }

  /**
   * update the process variables from the the task-parameter input. 
   */
  public void submitParameters(Map parameters, TaskInstance taskInstance) {
    if (taskControllerDelegation != null) {
      TaskControllerHandler taskControllerHandler = (TaskControllerHandler) taskControllerDelegation.instantiate();
      taskControllerHandler.submitParameters(parameters, taskInstance);

    } else {

      Token token = taskInstance.getToken();
      ProcessInstance processInstance = token.getProcessInstance();
      ContextInstance contextInstance = (ContextInstance) processInstance.getInstance(ContextInstance.class);

      // loop over all the variableAccesses elements
      String missingParameters = null;
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        String parameterName = variableAccess.getMappedName();

        // first check if the required variables are present
        if ( (variableAccess.isRequired())
             && (parameters.get(parameterName)==null)
           ) {
          if (missingParameters==null) {
            missingParameters = parameterName;
          } else {
            missingParameters += ", "+parameterName;
          }
        }
        
        // then do the update
        if ( (variableAccess.isWritable())
             && (parameters.get(parameterName)!=null) 
           ) {
          contextInstance.setVariable(variableAccess.getVariableName(), parameters.get(parameterName), token);
        }
      }

      // if there are missing, required parameters, throw an IllegalArgumentException
      if (missingParameters!=null) {
        throw new IllegalArgumentException("missing parameters: "+missingParameters);
      }
    }
  }
  
  public boolean hasVariableAccess(String variableName) {
    if (variableAccesses!=null) {
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.getVariableName().equals(variableName)) {
          return true;
        }
      }
    }
    return false;
  }

  public List getVariableAccesses() {
    return variableAccesses;
  }
  public Delegation getTaskControllerDelegation() {
    return taskControllerDelegation;
  }
  public void setTaskControllerDelegation(Delegation taskControllerDelegation) {
    this.taskControllerDelegation = taskControllerDelegation;
  }
  public long getId() {
    return id;
  }

  public void setVariableAccesses(List variableAccesses) {
    this.variableAccesses = variableAccesses;
  }
}
