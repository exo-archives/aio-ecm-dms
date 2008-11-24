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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.JbpmSession;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

public class ProcessState extends Node implements Parsable {

  private static final long serialVersionUID = 1L;

  transient protected String subProcessName = null;
  transient protected String subProcessVersion = null;
  protected ProcessDefinition subProcessDefinition = null;
  protected Set variableAccesses = null;

  // event types //////////////////////////////////////////////////////////////

  public static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_SUBPROCESS_CREATED, Event.EVENTTYPE_SUBPROCESS_END,
      Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_BEFORE_SIGNAL, Event.EVENTTYPE_AFTER_SIGNAL };

  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  // xml //////////////////////////////////////////////////////////////////////

  public void read(Element processStateElement, JpdlXmlReader jpdlReader) {
    Element subProcessElement = processStateElement.element("sub-process");
    if (subProcessElement != null) {
      subProcessName = subProcessElement.attributeValue("name");
      subProcessVersion = subProcessElement.attributeValue("version");
    }

    // if this parsing is done in the context of a process deployment, there is
    // a database connection to look up the subprocess.
    // when there is no jbpmSession, the definition will be left null... the
    // testcase can set it as appropriate.
    JbpmSession jbpmSession = JbpmSession.getCurrentJbpmSession();
    if (jbpmSession != null) {
      
      // now, we must be able to find the sub-process
      if (subProcessName != null) {
        
        // if the name and the version are specified
        if (subProcessVersion != null) {
          
          try {
            int version = Integer.parseInt(subProcessVersion);
            // select that exact process definition as the subprocess definition
            subProcessDefinition = jbpmSession.getGraphSession().findProcessDefinition(subProcessName, version);

          } catch (NumberFormatException e) {
            jpdlReader.addWarning("version in process-state was not a number: " + processStateElement.asXML());
          }
          
        } else { // if only the name is specified
          // select the latest version of that process as the subprocess
          // definition
          subProcessDefinition = jbpmSession.getGraphSession().findLatestProcessDefinition(subProcessName);
        }
      } else {
        jpdlReader.addWarning("no sub-process name specified in process-state " + processStateElement.asXML());
      }
    }
    
    // in case this is a self-recursive process invocation...
    if ( (subProcessName!=null)
         && (subProcessDefinition==null)
         && (subProcessVersion==null)
         && (subProcessName.equals(processDefinition.getName()))
       ) {
      subProcessDefinition = processDefinition;
    }

    this.variableAccesses = new HashSet(jpdlReader.readVariableAccesses(processStateElement));
  }

  public void execute(ExecutionContext executionContext) {
    Token superProcessToken = executionContext.getToken();

    // create the subprocess
    ProcessInstance subProcessInstance = new ProcessInstance(subProcessDefinition);
    // bind the subprocess to the super-process-token
    superProcessToken.setSubProcessInstance(subProcessInstance);
    subProcessInstance.setSuperProcessToken(superProcessToken);

    // fire the subprocess created event
    fireEvent(Event.EVENTTYPE_SUBPROCESS_CREATED, executionContext);

    // feed the readable variables
    if ((variableAccesses != null) && (!variableAccesses.isEmpty())) {

      ContextInstance superContextInstance = executionContext.getContextInstance();
      ContextInstance subContextInstance = subProcessInstance.getContextInstance();

      // loop over all the variable accesses
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        // if this variable access is readable
        if (variableAccess.isReadable()) {
          // the variable is copied from the super process variable name
          // to the sub process mapped name
          String variableName = variableAccess.getVariableName();
          Object value = superContextInstance.getVariable(variableName, superProcessToken);
          String mappedName = variableAccess.getMappedName();
          subContextInstance.setVariable(mappedName, value);
        }
      }
    }

    // send the signal to start the subprocess
    subProcessInstance.signal();
  }

  public void notifySubProcessEnd(ProcessInstance subProcessInstance) {
    Token superProcessToken = subProcessInstance.getSuperProcessToken();
    ExecutionContext executionContext = new ExecutionContext(superProcessToken);

    // feed the readable variables
    if ((variableAccesses != null) && (!variableAccesses.isEmpty())) {

      ContextInstance superContextInstance = executionContext.getContextInstance();
      ContextInstance subContextInstance = subProcessInstance.getContextInstance();

      // loop over all the variable accesses
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        // if this variable access is writable
        if (variableAccess.isWritable()) {
          // the variable is copied from the sub process mapped name
          // to the super process variable name
          String mappedName = variableAccess.getMappedName();
          Object value = subContextInstance.getVariable(mappedName);
          String variableName = variableAccess.getVariableName();
          superContextInstance.setVariable(variableName, value, superProcessToken);
        }
      }
    }

    // fire the subprocess ended event
    fireEvent(Event.EVENTTYPE_SUBPROCESS_END, executionContext);

    // remove the subprocess reference
    superProcessToken.setSubProcessInstance(null);

    // call the subProcessEndAction
    super.leave(executionContext, getDefaultLeavingTransition());
  }

  public ProcessDefinition getSubProcessDefinition() {
    return subProcessDefinition;
  }
  public void setSubProcessDefinition(ProcessDefinition subProcessDefinition) {
    this.subProcessDefinition = subProcessDefinition;
  }
}
