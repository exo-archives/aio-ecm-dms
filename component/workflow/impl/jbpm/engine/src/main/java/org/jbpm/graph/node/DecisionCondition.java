package org.jbpm.graph.node;

import java.io.Serializable;

public class DecisionCondition implements Serializable {

  private static final long serialVersionUID = 1L;
  
  String transitionName;
  String expression;
  
  public DecisionCondition() {
  }

  public DecisionCondition(String transitionName, String expression) {
    this.transitionName = transitionName;
    this.expression = expression;
  }
}
