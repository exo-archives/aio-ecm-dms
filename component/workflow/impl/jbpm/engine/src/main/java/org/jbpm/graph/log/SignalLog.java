package org.jbpm.graph.log;

import org.jbpm.graph.def.*;
import org.jbpm.logging.log.*;

public class SignalLog extends CompositeLog {

  private static final long serialVersionUID = 1L;
  
  protected Transition transition = null;
  
  public SignalLog() {
  }
    
  public SignalLog(Transition transition) {
    this.transition = transition;
  }
    
  public String toString() {
    String toString = "defaultsignal";
    if (transition.getName()!=null) {
      toString = "signal["+transition.getName()+"]";
    }
    return toString; 
  }
  
  public Transition getTransition() {
    return transition;
  }
}
