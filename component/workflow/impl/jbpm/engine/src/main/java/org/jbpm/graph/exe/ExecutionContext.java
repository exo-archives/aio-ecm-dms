package org.jbpm.graph.exe;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.module.def.ModuleDefinition;
import org.jbpm.module.exe.ModuleInstance;
import org.jbpm.scheduler.exe.SchedulerInstance;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

public class ExecutionContext {

  protected Token token = null;
  protected Event event = null;
  protected GraphElement eventSource = null; 
  protected Action action = null;
  protected Throwable exception = null;
  protected Transition transition = null;
  protected Node transitionSource = null;
  protected Task task = null;
  protected TaskInstance taskInstance = null;

  public ExecutionContext( Token token ) {
    this.token = token;
  }

  public ExecutionContext(ExecutionContext other) {
    this.token = other.token;
    this.event = other.event;
    this.action = other.action;
  }

  public Node getNode() {
    return token.getNode();
  }

  public ProcessDefinition getProcessDefinition() {
    ProcessInstance processInstance = getProcessInstance();
    return ( processInstance!=null ? processInstance.getProcessDefinition() : null );
  }

  public void setAction(Action action) {
    this.action = action;
    if (action!=null) {
      this.event = action.getEvent();
    }
  }
  
  public ProcessInstance getProcessInstance() {
    return token.getProcessInstance();
  }
  
  public String toString() {
    return "ExecutionContext["+ token + "]";
  }

  // convenience methods //////////////////////////////////////////////////////

  /**
   * set a process variable.
   */
  public void setVariable(String name, Object value) {
    getContextInstance().setVariable(name, value, token);
  }
  
  /**
   * get a process variable.
   */
  public Object getVariable(String name) {
    return getContextInstance().getVariable(name, token);
  }
  
  /**
   * leave this node over the default transition.  This method is only available
   * on node actions.  Not on actions that are executed on events.  Actions on 
   * events cannot change the flow of execution.
   */
  public void leaveNode() {
    getNode().leave(this);
  }
  /**
   * leave this node over the given transition.  This method is only available
   * on node actions.  Not on actions that are executed on events.  Actions on 
   * events cannot change the flow of execution.
   */
  public void leaveNode(String transitionName) {
    getNode().leave(this, transitionName);
  }
  /**
   * leave this node over the given transition.  This method is only available
   * on node actions.  Not on actions that are executed on events.  Actions on 
   * events cannot change the flow of execution.
   */
  public void leaveNode(Transition transition) {
    getNode().leave(this, transition);
  }
  
  public ModuleDefinition getDefinition(Class clazz) {
    return getProcessDefinition().getDefinition(clazz);
  }

  public ModuleInstance getInstance(Class clazz) {
    ProcessInstance processInstance = (token!=null ? token.getProcessInstance() : null);
    return (processInstance!=null ? processInstance.getInstance(clazz): null);
  }
  
  public ContextInstance getContextInstance() {
    return (ContextInstance) getInstance(ContextInstance.class);
  }

  public TaskMgmtInstance getTaskMgmtInstance() {
    return (TaskMgmtInstance) getInstance(TaskMgmtInstance.class);
  }

  public SchedulerInstance getSchedulerInstance() {
    return (SchedulerInstance) getInstance(SchedulerInstance.class);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public Token getToken() {
    return token;
  }
  public Action getAction() {
    return action;
  }
  public Event getEvent() {
    return event;
  }
  public void setEvent(Event event) {
    this.event = event;
  }
  public Throwable getException() {
    return exception;
  }
  public void setException(Throwable exception) {
    this.exception = exception;
  }
  public Transition getTransition() {
    return transition;
  }
  public void setTransition(Transition transition) {
    this.transition = transition;
  }
  public Node getTransitionSource() {
    return transitionSource;
  }
  public void setTransitionSource(Node transitionSource) {
    this.transitionSource = transitionSource;
  }
  public GraphElement getEventSource() {
    return eventSource;
  }
  public void setEventSource(GraphElement eventSource) {
    this.eventSource = eventSource;
  }
  public Task getTask() {
    return task;
  }
  public void setTask(Task task) {
    this.task = task;
  }
  public TaskInstance getTaskInstance() {
    return taskInstance;
  }
  public void setTaskInstance(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
  }
}
