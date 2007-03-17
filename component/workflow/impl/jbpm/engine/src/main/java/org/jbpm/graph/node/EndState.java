package org.jbpm.graph.node;

import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.*;

public class EndState extends Node {

  private static final long serialVersionUID = 1L;

  public EndState() {
  }
  
  public static final String[] supportedEventTypes = new String[]{Event.EVENTTYPE_NODE_ENTER};
  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  public EndState(String name) {
    super(name);
  }
  
  public void execute(ExecutionContext executionContext) {
    executionContext.getToken().end();
  }
  
  public Transition addLeavingTransition(Transition t) {
    throw new UnsupportedOperationException("can't add a leaving transition to an end-state");
  }
}
