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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.JbpmSession;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.log.ProcessInstanceCreateLog;
import org.jbpm.graph.log.ProcessInstanceEndLog;
import org.jbpm.graph.node.ProcessState;
import org.jbpm.logging.exe.LoggingInstance;
import org.jbpm.module.def.ModuleDefinition;
import org.jbpm.module.exe.ModuleInstance;
import org.jbpm.scheduler.exe.SchedulerInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;


/**
 * is one execution of a {@link org.jbpm.graph.def.ProcessDefinition}.
 * To create a new process execution of a process definition, just use the 
 * {@link #ProcessInstance(ProcessDefinition)}.
 * 
 */
public class ProcessInstance  implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected Date start = null;
  protected Date end = null;
  protected ProcessDefinition processDefinition = null;
  protected Token rootToken = null;
  protected Token superProcessToken = null;
  protected Map instances = null;
  protected Map transientInstances = null;
  protected List runtimeActions = null;

  // constructors /////////////////////////////////////////////////////////////

  public ProcessInstance() {
  }

  /**
   * creates a new process instance for the given process definition and 
   * puts the root-token (=main path of execution) in the start state.
   * For each of the optional module definitions contained in the 
   * {@link ProcessDefinition}, the corresponding module instance 
   * will be created. 
   * @throws NullPointerException if processDefinition is null.
   */
  public ProcessInstance( ProcessDefinition processDefinition ) {
    if (processDefinition==null) throw new NullPointerException("can't create a process instance when processDefinition is null");
    
    // initialize the members
    this.processDefinition = processDefinition;
    this.rootToken = new Token(this);
    this.start = new Date();

    // create the optional definitions
    Map definitions = processDefinition.getDefinitions();
    // if the state-definition has optional definitions
    if ( definitions != null ) {
      instances = new HashMap();
      // loop over each optional definition
      Iterator iter = definitions.values().iterator();
      while (iter.hasNext()) {
        ModuleDefinition definition = (ModuleDefinition) iter.next();
        // and create the corresponding optional instance
        ModuleInstance instance = definition.createInstance();
        if (instance != null) {
          addInstance( instance );
        }
      }
    }
    
    // add the creation log
    rootToken.addLog(new ProcessInstanceCreateLog());

    // if this process instance is created in the context of a persistent operation
    JbpmSession jbpmSession = JbpmSession.getCurrentJbpmSession();
    if (jbpmSession!=null) {
      // give this process instance an id
      jbpmSession.getSession().save(this);
    }

    // fire the process start event
    if (rootToken.getNode()!=null) {
      ExecutionContext executionContext = new ExecutionContext(rootToken);
      processDefinition.fireEvent(Event.EVENTTYPE_PROCESS_START, executionContext);
    }
  }

  // optional module instances
  /////////////////////////////////////////////////////////////////////////////
  
  /**
   * adds the given optional moduleinstance (bidirectional).
   */
  public ModuleInstance addInstance(ModuleInstance moduleInstance) {
    if (moduleInstance == null) throw new IllegalArgumentException("can't add a null moduleInstance to a process instance");
    if (instances == null) instances = new HashMap();
    instances.put(moduleInstance.getClass().getName(), moduleInstance);
    moduleInstance.setProcessInstance(this);
    return moduleInstance;
  }

  /**
   * removes the given optional moduleinstance (bidirectional). 
   */
  public ModuleInstance removeInstance(ModuleInstance moduleInstance) {
    ModuleInstance removedModuleInstance = null;
    if (moduleInstance == null) throw new IllegalArgumentException("can't remove a null moduleInstance from a process instance");
    if (instances != null) {
      removedModuleInstance = (ModuleInstance) instances.remove(moduleInstance.getClass().getName());
      if (removedModuleInstance!=null) {
        moduleInstance.setProcessInstance(null);
      }
    }
    return removedModuleInstance;
  }

  /**
   * looks up an optional module instance by its class.    
   */
  public ModuleInstance getInstance(Class clazz) {
    ModuleInstance moduleInstance = null;
    if ( instances != null ) {
      moduleInstance = (ModuleInstance) instances.get( clazz.getName() );
    }
    
    if (moduleInstance==null) {
      if (transientInstances==null) transientInstances = new HashMap();
      
      // client requested an instance that is not in the map of instances.
      // so we can safely assume that the client wants a transient instance
      moduleInstance = (ModuleInstance) transientInstances.get( clazz.getName() );
      if (moduleInstance==null) {
        try {
          moduleInstance = (ModuleInstance) clazz.newInstance();
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("couldn't instantiate transient module '"+clazz.getName()+"' with the default constructor");
        }
        transientInstances.put(clazz.getName(), moduleInstance);
      }
    }

    return moduleInstance;
  }

  /**
   * process instance extension for process variables.
   */
  public ContextInstance getContextInstance() {
    return (ContextInstance) getInstance(ContextInstance.class);
  }

  /**
   * process instance extension for managing the tasks and actors.
   */
  public TaskMgmtInstance getTaskMgmtInstance() {
    return (TaskMgmtInstance) getInstance(TaskMgmtInstance.class);
  }

  /**
   * process instance extension for logging. Probably you don't need to access 
   * the logging instance directly.  Mostly, {@link Token#addLog(ProcessLog)} is 
   * sufficient and more convenient. 
   */
  public LoggingInstance getLoggingInstance() {
    return (LoggingInstance) getInstance(LoggingInstance.class);
  }

  /**
   * process instance extension for timers. 
   */
  public SchedulerInstance getSchedulerInstance() {
    return (SchedulerInstance) getInstance(SchedulerInstance.class);
  }


  // operations ///////////////////////////////////////////////////////////////

  /**
   * instructs the main path of execution to continue by taking the default 
   * transition on the current node.
   * @throws IllegalStateException if the token is not active.
   */
  public void signal() {
    if ( hasEnded() ) {
      throw new IllegalStateException("couldn't signal token : token has ended");
    }
    rootToken.signal();
  }

  /**
   * instructs the main path of execution to continue by taking the specified 
   * transition on the current node.
   * @throws IllegalStateException if the token is not active.
   */
  public void signal(String transitionName) {
    if ( hasEnded() ) {
      throw new IllegalStateException("couldn't signal token : token has ended");
    }
    rootToken.signal(transitionName);
  }

  /**
   * instructs the main path of execution to continue by taking the specified 
   * transition on the current node.
   * @throws IllegalStateException if the token is not active.
   */
  public void signal( Transition transition ) {
    if ( hasEnded() ) {
      throw new IllegalStateException("couldn't signal token : token has ended");
    }
    rootToken.signal(transition);
  }

  /**
   * ends (=cancels) this process instance and all the tokens in it.
   */
  public void end() {
    // end the main path of execution
    rootToken.end();
    
    if (end==null) {
      // mark this process instance as ended
      end = new Date();
      
      // fire the process-end event
      ExecutionContext executionContext = new ExecutionContext(rootToken);
      processDefinition.fireEvent(Event.EVENTTYPE_PROCESS_END, executionContext);
      
      // add the process instance end log
      rootToken.addLog(new ProcessInstanceEndLog());

      // check if this process was started as a subprocess of a super process
      if (superProcessToken!=null) {
        ProcessState processState = (ProcessState) superProcessToken.getNode();
        processState.notifySubProcessEnd(this);
      }

      // make sure all the timers for this process instance are cancelled when the process end updates get saved in the database.
      // see also org.jbpm.db.SchedulerSession.saveTimers(ProcessInstance);
      getSchedulerInstance().setProcessEnded(true);
    }
  }

  // runtime actions //////////////////////////////////////////////////////////

  /**
   * adds an action to be executed upon a process event in the future.
   */
  public RuntimeAction addRuntimeAction( RuntimeAction runtimeAction ) {
    if (runtimeAction == null) throw new IllegalArgumentException("can't add a null runtimeAction to a process instance");
    if (runtimeActions == null) runtimeActions = new ArrayList();
    runtimeActions.add(runtimeAction);
    runtimeAction.processInstance = this;
    return runtimeAction;
  }

  /**
   * removes a runtime action.
   */
  public RuntimeAction removeRuntimeAction( RuntimeAction runtimeAction ) {
    RuntimeAction removedRuntimeAction = null;
    if (runtimeAction == null)
      throw new IllegalArgumentException("can't remove a null runtimeAction from an process instance");
    if (runtimeActions != null) {
      if (runtimeActions.remove(runtimeAction)) {
        removedRuntimeAction = runtimeAction;
        runtimeAction.processInstance = null;
      }
    }
    return removedRuntimeAction;
  }

  /**
   * is the list of all runtime actions.
   */
  public List getRuntimeActions() {
    return runtimeActions;
  }

  // various information retrieval methods ////////////////////////////////////

  /**
   * tells if this process instance is still active or not.
   */
  public boolean hasEnded() {
    return ( end != null );
  }
  
  /**
   * calculates if this process instance has still options to continue. 
   */
  public boolean isTerminatedImplicitly() {
    boolean isTerminatedImplicitly = true;
    if ( end == null ) {
      isTerminatedImplicitly = rootToken.isTerminatedImplicitly();
    }
    return isTerminatedImplicitly;
  }
  
  /**
   * looks up the token in the tree, specified by the slash-separated token path.
   * @param tokenPath is a slash-separated name that specifies a token in the tree.
   * @return the specified token or null if the token is not found.
   */
  public Token findToken(String tokenPath) {
    return ( rootToken!=null ? rootToken.findToken(tokenPath) : null );
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public long getId() {
    return id;
  }
  public Token getRootToken() {
    return rootToken;
  }
  public Date getStart() {
    return start;
  }
  public Date getEnd() {
    return end;
  }
  public Map getInstances() {
    return instances;
  }
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public Token getSuperProcessToken() {
    return superProcessToken;
  }
  public void setSuperProcessToken(Token superProcessToken) {
    this.superProcessToken = superProcessToken;
  }
}
