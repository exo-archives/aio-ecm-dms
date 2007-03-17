package org.jbpm.graph.def;

import org.jbpm.graph.exe.*;

public class DelegationException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  protected Throwable cause = null;
  protected ExecutionContext executionContext = null;

  public DelegationException(Throwable cause, ExecutionContext executionContext) {
    this.cause = cause;
    this.executionContext = executionContext;
  }
  
  public ExecutionContext getExecutionContext() {
    return executionContext;
  }

  public Throwable getCause() {
    return cause;
  }
}
