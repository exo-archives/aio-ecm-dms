package org.jbpm.graph.log;

import org.jbpm.graph.exe.*;
import org.jbpm.logging.log.*;

public class TokenCreateLog extends ProcessLog {

  private static final long serialVersionUID = 1L;
  
  protected Token child = null;
  
  public TokenCreateLog() {
  }

  public TokenCreateLog(Token child) {
    this.child = child;
  }

  public String toString() {
    return "tokencreate["+child.getFullName()+"]";
  }
  
  public Token getChild() {
    return child;
  }
}
