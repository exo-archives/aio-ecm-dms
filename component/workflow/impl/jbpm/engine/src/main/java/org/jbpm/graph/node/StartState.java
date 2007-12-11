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
package org.jbpm.graph.node;

import java.util.Map;

import org.dom4j.Element;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.security.Authentication;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.TaskMgmtDefinition;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

public class StartState extends Node {
  
  private static final long serialVersionUID = 1L;
  
  // initiatorSwimlane of a start-state serves to capture the authenticate user 
  // that starts the process as the initiator.  
  public Swimlane initiatorSwimlane = null;

  public StartState() {
  }

  public StartState(String name) {
    super(name);
  }

  // event types //////////////////////////////////////////////////////////////

  public static final String[] supportedEventTypes = new String[]{
    Event.EVENTTYPE_NODE_LEAVE,
    Event.EVENTTYPE_AFTER_SIGNAL
  };
  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  // xml //////////////////////////////////////////////////////////////////////

  public void read(Element startStateElement, JpdlXmlReader jpdlReader) {
    String swimlaneName = startStateElement.attributeValue("swimlane");
    if (swimlaneName!=null) {
      TaskMgmtDefinition taskMgmtDefinition = jpdlReader.getProcessDefinition().getTaskMgmtDefinition();
      initiatorSwimlane = taskMgmtDefinition.getSwimlane(swimlaneName);
    }
    
    // if the start-state has a task specified,
    Element startTaskElement = startStateElement.element("task");
    if (startTaskElement!=null) {
      // delegate the parsing of the start-state task to the jpdlReader
      jpdlReader.readStartStateTask(startTaskElement, this);
    }
  }

  public void write(Element nodeElement) {
  }
  
  public void leave(ExecutionContext executionContext, Transition transition) {
    // if this start-state is associated with a initiatorSwimlane
    if (initiatorSwimlane!=null) {
      // get the initiator swimlane instance
      SwimlaneInstance initiatorSwimlaneInstance = executionContext.getTaskMgmtInstance().getSwimlaneInstance(initiatorSwimlane.getName());
      // then capture the authenticated user
      String initiatorActorId = Authentication.getAuthenticatedActorId();
      // then store the currently authenticated user as the actor in the initiator swimlane instance
      initiatorSwimlaneInstance.setActorId(initiatorActorId);
    }
    
    // leave this node as usual
    super.leave(executionContext, transition);
  }
  
  public void execute(ExecutionContext executionContext) {
    throw new UnsupportedOperationException( "illegal operation : its not possible to arrive in a start-state" );
  }
  
  public Transition addArrivingTransition(Transition t) {
    throw new UnsupportedOperationException( "illegal operation : its not possible to add a transition that is arriving in a start state" );
  }
  
  public void setArrivingTransitions(Map arrivingTransitions) {
    if ( (arrivingTransitions!=null)
         && (arrivingTransitions.size()>0)) {
      throw new UnsupportedOperationException( "illegal operation : its not possible to set a non-empty map in the arriving transitions of a start state" );
    }
  }
}
