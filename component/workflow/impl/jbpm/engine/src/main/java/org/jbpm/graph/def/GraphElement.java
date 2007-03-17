package org.jbpm.graph.def;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;
import org.jbpm.graph.exe.*;
import org.jbpm.graph.log.ActionLog;

public abstract class GraphElement implements Serializable {
  
  private static final long serialVersionUID = 1L;

  long id = 0;
  protected String name = null;
  protected ProcessDefinition processDefinition = null;
  protected Map events = null;
  protected List exceptionHandlers = null;

  public GraphElement() {
  }
  
  public GraphElement( String name ) {
    setName( name );
  }

  // events ///////////////////////////////////////////////////////////////////

  /**
   * indicative set of event types supported by this graph element.
   * this is currently only used by the process designer to know which 
   * event types to show on a given graph element.  in process definitions 
   * and at runtime, there are no contstraints on the event-types.
   */
  public abstract String[] getSupportedEventTypes();

  /**
   * gets the events, keyd by eventType (java.lang.String).
   */
  public Map getEvents() {
    return events;
  }

  public boolean hasEvents() {
    return ( (events!=null)
             && (events.size()>0) );
  }

  public Event getEvent(String eventType) {
    Event event = null;
    if (events!=null) {
      event = (Event) events.get(eventType);
    }
    return event;
  }
  
  public boolean hasEvent(String eventType) {
    boolean hasEvent = false;
    if (events!=null) {
      hasEvent = events.containsKey(eventType);
    }
    return hasEvent;
  }
  
  public Event addEvent(Event event) {
    if (event == null) throw new IllegalArgumentException("can't add a null event to a graph element");
    if (event.getEventType() == null) throw new IllegalArgumentException("can't add an event without an eventType to a graph element");
    if (events == null) events = new HashMap();
    events.put(event.getEventType(), event);
    event.graphElement = this;
    return event;
  }
  
  public Event removeEvent(Event event) {
    Event removedEvent = null;
    if (event == null) throw new IllegalArgumentException("can't remove a null event from a graph element");
    if (event.getEventType() == null) throw new IllegalArgumentException("can't remove an event without an eventType from a graph element");
    if (events != null) {
      removedEvent = (Event) events.remove(event.getEventType());
      if (removedEvent!=null) {
        event.graphElement = null;
      }
    }
    return removedEvent;
  }

  // exception handlers ///////////////////////////////////////////////////////

  /**
   * is the list of exception handlers associated to this graph element.
   */
  public List getExceptionHandlers() {
    return exceptionHandlers;
  }
  
  public ExceptionHandler addExceptionHandler(ExceptionHandler exceptionHandler) {
    if (exceptionHandler == null) throw new IllegalArgumentException("can't add a null exceptionHandler to a graph element");
    if (exceptionHandlers == null) exceptionHandlers = new ArrayList();
    exceptionHandlers.add(exceptionHandler);
    exceptionHandler.graphElement = this;
    return exceptionHandler;
  }
  
  public void removeExceptionHandler(ExceptionHandler exceptionHandler) {
    if (exceptionHandler == null) throw new IllegalArgumentException("can't remove a null exceptionHandler from an graph element");
    if (exceptionHandlers != null) {
      if (exceptionHandlers.remove(exceptionHandler)) {
        exceptionHandler.graphElement = null;
      }
    }
  }

  public void reorderExceptionHandler(int oldIndex, int newIndex) {
    if ( (exceptionHandlers!=null)
         && (Math.min(oldIndex, newIndex)>=0)
         && (Math.max(oldIndex, newIndex)<exceptionHandlers.size()) ) {
      Object o = exceptionHandlers.remove(oldIndex);
      exceptionHandlers.add(newIndex, o);
    } else {
      throw new IndexOutOfBoundsException("couldn't reorder element from index '"+oldIndex+"' to index '"+newIndex+"' in exceptionHandler-list '"+exceptionHandlers+"'");
    }
  }

  // event handling ///////////////////////////////////////////////////////////

  public void fireEvent(String eventType, ExecutionContext executionContext) {
    Token token = executionContext.getToken();

    log.debug( "event '"+eventType+"' on '"+this+"' for '"+token+"'" );

    try {
      executionContext.setEventSource(this);
      fireAndPropagateEvent(eventType, executionContext);
    } finally {
      executionContext.setEventSource(null);
    }
  }

  public void fireAndPropagateEvent(String eventType, ExecutionContext executionContext) {
    // calculate if the event was fired on this element or if it was a propagated event
    boolean isPropagated = (executionContext.getEventSource()!=this);
    
    // execute static actions 
    Event event = getEvent(eventType);
    if (event!=null) {
      // update the context
      executionContext.setEvent(event);
      // execute the static actions specified in the process definition
      executeActions(event.getActions(), executionContext, isPropagated);
    }
    
    // execute the runtime actions
    List runtimeActions = getRuntimeActionsForEvent(executionContext, eventType);
    executeActions(runtimeActions, executionContext, isPropagated);

    // remove the event from the context
    executionContext.setEvent(null);
    
    // propagate the event to the parent element
    GraphElement parent = getParent();
    if (parent!=null) {
      parent.fireAndPropagateEvent(eventType, executionContext);
    }
  }

  private void executeActions(List actions, ExecutionContext executionContext, boolean isPropagated) {
    if (actions!=null) {
      Iterator iter = actions.iterator();
      while (iter.hasNext()) {
        Action action = (Action) iter.next();
        if ( action.acceptsPropagatedEvents()
             || (!isPropagated)
           ) {
          Token token = executionContext.getToken();

          // create action log
          ActionLog actionLog = new ActionLog(action);
          token.startCompositeLog(actionLog);

          try {
            // update the execution context
            executionContext.setAction(action);

            // execute the action
            log.debug("executing action '"+action+"'");
            action.execute(executionContext);
      
          } catch (Throwable exception) {
            log.error("action threw exception: "+exception.getMessage(), exception);
            
            // log the action exception 
            actionLog.setException(exception);
      
            // if an exception handler is available
            raiseException(exception, executionContext);
            
          } finally {
            executionContext.setAction(null);
            token.endCompositeLog();
          }
        }
      }
    }
  }

  private List getRuntimeActionsForEvent(ExecutionContext executionContext, String eventType) {
    List runtimeActionsForEvent = null;
    List runtimeActions = executionContext.getProcessInstance().getRuntimeActions();
    if (runtimeActions!=null) {
      Iterator iter = runtimeActions.iterator();
      while (iter.hasNext()) {
        RuntimeAction runtimeAction = (RuntimeAction) iter.next();
        // if the runtime-action action is registered on this element and this eventType
        if ( (this==runtimeAction.getGraphElement())
             && (eventType.equals(runtimeAction.getEventType()))
           ) {
          // ... add its action to the list of runtime actions 
          if (runtimeActionsForEvent==null) runtimeActionsForEvent = new ArrayList();
          runtimeActionsForEvent.add(runtimeAction.getAction());
        }
      }
    }
    return runtimeActionsForEvent;
  }

/*    
      // the next instruction merges the actions specified in the process definition with the runtime actions
      List actions = event.collectActions(executionContext);
      
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
            Event.log.error("action threw exception: "+exception.getMessage(), exception);
            
            // log the action exception 
            actionLog.setException(exception);
      
            // if an exception handler is available
            event.graphElement.raiseException(exception, executionContext);
          } finally {
            executionContext.getToken().endCompositeLog();
          }
        }
      }
    }
*/

  /**
   * throws an ActionException if no applicable exception handler is found.
   * An ExceptionHandler is searched for in this graph element and then recursively up the 
   * parent hierarchy.  
   * If an exception handler is found, it is applied.  If the exception handler does not 
   * throw an exception, the exception is considered handled.  Otherwise the search for 
   * an applicable exception handler continues where it left of with the newly thrown 
   * exception.
   */
  public void raiseException(Throwable exception, ExecutionContext executionContext) throws DelegationException {
    boolean isHandled = false;
    if (exceptionHandlers!=null) {
      try {
        ExceptionHandler exceptionHandler = findExceptionHandler(exception);
        if (exceptionHandler!=null) {
          executionContext.setException(exception);
          exceptionHandler.handleException(executionContext);
          isHandled = true;
        }
      } catch (Throwable t) {
        exception = t;
      }
    }

    if (!isHandled) {
      GraphElement parent = getParent();
      // if this graph element has a parent
      if ( (parent!=null)
           && (parent!=this) ){
        // action to the parent
        parent.raiseException(exception, executionContext);
      } else {
        // rollback the actions
        // rollbackActions(executionContext);
        
        // if there is no parent we need to throw an action exception to the client
        throw new DelegationException(exception, executionContext);
      }
    }
  }

  protected ExceptionHandler findExceptionHandler(Throwable exception) {
    ExceptionHandler exceptionHandler = null;
    
    if (exceptionHandlers!=null) {
      Iterator iter = exceptionHandlers.iterator();
      while (iter.hasNext() && (exceptionHandler==null)) {
        ExceptionHandler candidate = (ExceptionHandler) iter.next();
        if (candidate.matches(exception)) {
          exceptionHandler = candidate;
        }
      }
    }
    
    return exceptionHandler;
  }

  public GraphElement getParent() {
    return processDefinition;
  }

  /**
   * @return all the parents of this graph element ordered by age.
   */
  public List getParents() {
    List parents = new ArrayList();
    GraphElement parent = getParent(); 
    if (parent!=null) {
      parent.addParentChain(parents);
    }
    return parents;
  }

  /**
   * @return this graph element plus all the parents ordered by age.
   */
  public List getParentChain() {
    List parents = new ArrayList();
    this.addParentChain(parents);
    return parents;
  }

  private void addParentChain(List parentChain) {
    parentChain.add(this);
    GraphElement parent = getParent();
    if (parent!=null) {
      parent.addParentChain(parentChain);
    }
  }
  
  public String toString() {
    String className = getClass().getName(); 
    className = className.substring(className.lastIndexOf('.')+1);
    if (name!=null) {
      className = className+"("+name+")";
    } else {
      className = className+"("+Integer.toHexString(System.identityHashCode(this))+")";
    }
    return className;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public void setName( String name ) {
    this.name = name;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  // logger ///////////////////////////////////////////////////////////////////
  private static final Log log = LogFactory.getLog(GraphElement.class);
}
