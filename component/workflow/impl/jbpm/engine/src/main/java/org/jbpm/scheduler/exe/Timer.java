package org.jbpm.scheduler.exe;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * a process timer.
 */
public class Timer implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  String name = null;
  Date dueDate = null;
  String repeat = null;
  String transitionName = null;
  Action action = null;
  Token token = null;
  ProcessInstance processInstance = null;
  TaskInstance taskInstance = null;
  GraphElement graphElement = null;
  String exception = null;

  Timer(){}
  
  public Timer(Token token) {
    this.token = token;
    this.processInstance = token.getProcessInstance();
  }

  public void execute() {
    ExecutionContext executionContext = new ExecutionContext(token);
    if (taskInstance!=null) {
      executionContext.setTaskInstance(taskInstance);
    }

    // first fire the event if there is a graph element specified
    if (graphElement!=null) {
      graphElement.fireAndPropagateEvent(Event.EVENTTYPE_TIMER, executionContext);
    }
    
    // then execute the action if there is one
    if (action!=null) {
      try {
        log.debug("executing timer '"+this+"'");
        action.execute(executionContext);
      } catch (Throwable actionException) {
        log.warn("timer action threw exception", actionException);

        // we put the exception in t
        Throwable t = actionException;
        try {
          // if there is a graphElement connected to this timer...
          if (graphElement != null) {
            // we give that graphElement a chance to catch the exception
            graphElement.raiseException(actionException, executionContext);
            log.debug("timer exception got handled by '"+graphElement+"'");
            t = null;
          }
        } catch (Throwable rethrowOrDelegationException) {
          // if the exception handler rethrows or the original exception results in a DelegationException...
          t = rethrowOrDelegationException;
        }
        
        if (t!=null) {
          log.error("unhandled timer exception", t);
          // this means an unhandled exception occurred in this timer
          StringWriter sw = new StringWriter();
          actionException.printStackTrace(new PrintWriter(sw));
          exception = sw.toString();
          if (exception.length()>4000) exception = exception.substring(0, 4000);
        }
      }
    }

    // then take a transition if one is specified
    if ( (transitionName!=null)
         && (exception==null) // and if no unhandled exception occurred during the action  
       ) {
      if (token.getNode().hasLeavingTransition(transitionName)) {
        token.signal(transitionName);
      }
    }
  }

  public boolean isDue() {
    return (dueDate.getTime()<=System.currentTimeMillis());
  }
  
  private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS"); 
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("timer(");
    buffer.append(name);
    if ( (action!=null)
         && (action.getActionDelegation()!=null)
         && (action.getActionDelegation().getClassName()!=null)) {
      buffer.append(",");
      buffer.append(action.getActionDelegation().getClassName());
    }
    if (dueDate!=null) {
      buffer.append(",");
      buffer.append(dateFormat.format(dueDate));
    }
    buffer.append(")");
    return buffer.toString();
  }
  
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeatDuration) {
    this.repeat = repeatDuration;
  }
  public Token getToken() {
    return token;
  }
  public void setToken(Token token) {
    this.token = token;
  }
  public String getTransitionName() {
    return transitionName;
  }
  public void setTransitionName(String transitionName) {
    this.transitionName = transitionName;
  }
  public long getId() {
    return id;
  }
  public GraphElement getGraphElement() {
    return graphElement;
  }
  public void setGraphElement(GraphElement graphElement) {
    this.graphElement = graphElement;
  }
  public Action getAction() {
    return action;
  }
  public void setAction(Action action) {
    this.action = action;
  }
  public String getException() {
    return exception;
  }
  public TaskInstance getTaskInstance() {
    return taskInstance;
  }
  public void setTaskInstance(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
  }
  
  private static final Log log = LogFactory.getLog(Timer.class);
}
