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

import java.io.*;
import java.util.*;

import org.dom4j.*;
import org.jbpm.graph.exe.*;
import org.jbpm.instantiation.*;
import org.jbpm.jpdl.xml.*;

public class Action implements Parsable, Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String name = null;
  protected boolean isPropagationAllowed = true;
  protected Action referencedAction = null;
  protected Delegation actionDelegation  = null;
  protected Event event = null;
  protected ProcessDefinition processDefinition = null;

  public Action() {
  }

  public Action(Delegation actionDelegate) {
    this.actionDelegation = actionDelegate;
  }
  
  public String toString() {
    String toString = null;
    if (name!=null) {
      toString = "action["+name+"]";
    } else if ( (actionDelegation!=null)
                && (actionDelegation.getClassName()!=null)
              ) {
      String className = actionDelegation.getClassName();
      toString = className.substring(className.lastIndexOf('.')+1);
    } else {
      String className = getClass().getName(); 
      className = className.substring(className.lastIndexOf('.')+1);
      if (name!=null) {
        toString = className+"("+name+")";
      } else {
        toString = className+"("+Integer.toHexString(System.identityHashCode(this))+")";
      }
    }
    return toString;
  }

  public void read(Element actionElement, JpdlXmlReader jpdlReader) {
    if (actionElement.attribute("ref-name")!=null) {
      jpdlReader.addUnresolvedActionReference(actionElement, this);
    } else if (actionElement.attribute("class")!=null) {
      actionDelegation = new Delegation();
      actionDelegation.read(actionElement, jpdlReader);
      
      String acceptPropagatedEvents = actionElement.attributeValue("accept-propagated-events");
      if ("false".equalsIgnoreCase(acceptPropagatedEvents)
          || "no".equalsIgnoreCase(acceptPropagatedEvents)) {
        isPropagationAllowed = false;
      }
    } else {
      jpdlReader.addWarning("action does not have class nor ref-name attribute "+actionElement.asXML());
    }
  }

  public void write(Element actionElement) {
    if (actionDelegation!=null) {
      actionDelegation.write(actionElement);
    }
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    if (referencedAction!=null) {
      referencedAction.execute(executionContext);
    } else {
      ActionHandler actionHandler = (ActionHandler)actionDelegation.getInstance();
      actionHandler.execute(executionContext);
    }
  }

  public void setName(String name) {
    // if the process definition is already set
    if (processDefinition!=null) {
      // update the process definition action map
      Map actionMap = processDefinition.getActions();
      // the != string comparison is to avoid null pointer checks.  it is no problem if the body is executed a few times too much :-)
      if ( (this.name != name)
           && (actionMap!=null) ) {
        actionMap.remove(this.name);
        actionMap.put(name, this);
      }
    }

    // then update the name
    this.name = name;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public boolean acceptsPropagatedEvents() {
    return isPropagationAllowed;
  }

  public boolean isPropagationAllowed() {
    return isPropagationAllowed;
  }
  public void setPropagationAllowed(boolean isPropagationAllowed) {
    this.isPropagationAllowed = isPropagationAllowed;
  }

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Event getEvent() {
    return event;
  }
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  public Delegation getActionDelegation() {
    return actionDelegation;
  }
  public void setActionDelegation(Delegation instantiatableDelegate) {
    this.actionDelegation = instantiatableDelegate;
  }
  public Action getReferencedAction() {
    return referencedAction;
  }
  public void setReferencedAction(Action referencedAction) {
    this.referencedAction = referencedAction;
  }
}
