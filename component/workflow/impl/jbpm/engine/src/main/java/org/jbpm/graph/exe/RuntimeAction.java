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
