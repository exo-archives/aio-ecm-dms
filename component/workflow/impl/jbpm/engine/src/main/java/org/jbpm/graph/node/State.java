package org.jbpm.graph.node;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

public class State extends Node {
  
  private static final long serialVersionUID = 1L;

  public State() {
    this(null);
  }
  
  public State(String name) {
    super( name );
  }

  public void execute(ExecutionContext executionContext) {
  }
}
