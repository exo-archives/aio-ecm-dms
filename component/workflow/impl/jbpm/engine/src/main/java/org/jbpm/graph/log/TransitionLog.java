package org.jbpm.graph.log;

import org.jbpm.graph.def.*;
import org.jbpm.logging.log.*;

public class TransitionLog extends CompositeLog {

  private static final long serialVersionUID = 1L;
  
  protected Transition transition = null;
  protected Node sourceNode = null;
  protected Node destinationNode = null;

  public TransitionLog() {
  }

  public TransitionLog(Transition transition, Node source) {
    this.transition = transition;
    this.sourceNode = source;
  }

  public Node getDestinationNode() {
    return destinationNode;
  }
  public void setDestinationNode(Node destination) {
    this.destinationNode = destination;
  }
  public Node getSourceNode() {
    return sourceNode;
  }
  public Transition getTransition() {
    return transition;
  }

  public String toString() {
    String from = (sourceNode!=null ? sourceNode.getName() : "unnamed-node");
    String to = (destinationNode!=null ? destinationNode.getName() : "unnamed-node");
    return "transition["+from+"-->"+to+"]";
  }
}
