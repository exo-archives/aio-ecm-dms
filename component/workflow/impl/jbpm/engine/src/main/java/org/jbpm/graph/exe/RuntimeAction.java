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
package org.jbpm.graph.exe;

import java.io.*;

import org.jbpm.graph.def.*;

/**
 * is an action that can be added at runtime to the execution of one process instance.
 */
public class RuntimeAction implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected ProcessInstance processInstance = null;
  protected GraphElement graphElement = null;
  protected String eventType = null;
  protected Action action = null;
  
  public RuntimeAction() {
  }

  /**
   * creates a runtime action.  Look up the event with {@link GraphElement#getEvent(String)}
   * and the action with {@link ProcessDefinition#getAction(String)}.  You can only 
   * lookup named actions easily.
   */
  public RuntimeAction(Event event, Action action) {
    this.graphElement = event.getGraphElement();
    this.eventType = event.getEventType();
    this.action = action;
  }

  public RuntimeAction(GraphElement graphElement, String eventType, Action action) {
    this.graphElement = graphElement;
    this.eventType = eventType;
    this.action = action;
  }

  public long getId() {
    return id;
  }
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  public Action getAction() {
    return action;
  }
  public String getEventType() {
    return eventType;
  }
  public GraphElement getGraphElement() {
    return graphElement;
  }
}
