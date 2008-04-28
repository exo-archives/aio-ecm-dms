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
package org.jbpm.graph.def;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public static final String EVENTTYPE_TRANSITION = "transition";
  public static final String EVENTTYPE_BEFORE_SIGNAL = "before-signal";
  public static final String EVENTTYPE_AFTER_SIGNAL = "after-signal";
  public static final String EVENTTYPE_PROCESS_START = "process-start";
  public static final String EVENTTYPE_PROCESS_END = "process-end";
  public static final String EVENTTYPE_NODE_ENTER = "node-enter";
  public static final String EVENTTYPE_NODE_LEAVE = "node-leave";
  public static final String EVENTTYPE_SUPERSTATE_ENTER = "superstate-enter";
  public static final String EVENTTYPE_SUPERSTATE_LEAVE = "superstate-leave";
  public static final String EVENTTYPE_SUBPROCESS_CREATED = "subprocess-created";
  public static final String EVENTTYPE_SUBPROCESS_END = "subprocess-end";
  public static final String EVENTTYPE_TASK_CREATE = "task-create";
  public static final String EVENTTYPE_TASK_ASSIGN = "task-assign";
  public static final String EVENTTYPE_TASK_START = "task-start";
  public static final String EVENTTYPE_TASK_END = "task-end";
  public static final String EVENTTYPE_TIMER = "timer";

  long id = 0;
  protected String eventType = null;
  protected GraphElement graphElement = null;
  protected List actions = null;
  
  // constructors /////////////////////////////////////////////////////////////

  public Event() {
  }
  
  public Event(String eventType) {
    this.eventType = eventType;
  }

  public Event(GraphElement graphElement, String eventType) {
    this.graphElement = graphElement;
    this.eventType = eventType;
  }

  // actions //////////////////////////////////////////////////////////////////

  /**
   * is the list of actions associated to this event.
   * @return an empty list if no actions are associated.
   */
  public List getActions() {
    return actions;
  }

  public boolean hasActions() {
    return ( (actions!=null)
             && (actions.size()>0) ); 
  }

  public Action addAction(Action action) {
    if (action==null) throw new IllegalArgumentException("can't add a null action to an event");
    if (actions==null) actions = new ArrayList();
    actions.add(action);
    action.event = this;
    return action;
  }

  public void removeAction(Action action) {
    if (action==null) throw new IllegalArgumentException("can't remove a null action from an event");
    if (actions!=null) {
      if (actions.remove(action)) {
        action.event=null;
      }
    }
  }

  // behaviour ////////////////////////////////////////////////////////////////
 /*
  public void fire(ExecutionContext executionContext) {
    // create the action context
    executionContext.setEvent(this);

    // the next instruction merges the actions specified in the process definition with the runtime actions
    List actions = collectActions(executionContext);
    
    // calculate if the event was fired on this element or if it was a propagated event
    boolean isPropagated = (executionContext.getEventSource()!=graphElement);
    
    // loop over all actions of this event
    Iterator iter = actions.iterator();
    while (iter.hasNext()) {
      Action action = (Action) iter.next();
      executionContext.setAction(action);
      
      if ( (!isPropagated)
           || (action.acceptsPropagatedEvents() ) ) {

        // create action log
        ActionLog actionLog = new ActionLog(action);
        executionContext.getToken().startCompositeLog(actionLog);

        try {
          // execute the action
          action.execute(executionContext);

        } catch (Throwable exception) {
          log.error("action threw exception: "+exception.getMessage(), exception);
          
          // log the action exception 
          actionLog.setException(exception);

          // if an exception handler is available
          graphElement.raiseException(exception, executionContext);
        } finally {
          executionContext.getToken().endCompositeLog();
        }
      }
    }
  }
*/
  /**
   * collects static and runtime actions.
  private List collectActions(ExecutionContext executionContext) {
    List mergedActions = new ArrayList();

    // first, add all actions
    if (actions!=null) {
      mergedActions.addAll(actions);
    }
    
    // then add all runtime actions
    List runtimeActions = executionContext.getProcessInstance().getRuntimeActions();
    if (runtimeActions!=null) {
      Iterator iter = runtimeActions.iterator();
      while (iter.hasNext()) {
        RuntimeAction runtimeAction = (RuntimeAction) iter.next();
        if (this==runtimeAction.getEvent()) {
          mergedActions.add(runtimeAction.getAction());
        }
      }
    }
    
    return mergedActions;
  }
   */

  public String toString() {
    return eventType;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getEventType() {
    return eventType;
  }
  public GraphElement getGraphElement() {
    return graphElement;
  }
  public long getId() {
    return id;
  }
  
  // private static final Log log = LogFactory.getLog(Event.class);
}
