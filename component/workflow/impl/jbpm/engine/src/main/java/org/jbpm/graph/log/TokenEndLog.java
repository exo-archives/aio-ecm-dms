package org.jbpm.graph.log;

import org.jbpm.graph.exe.*;
import org.jbpm.logging.log.*;

public class TokenEndLog extends ProcessLog {

  private static final long serialVersionUID = 1L;

  protected Token child = null;
  
  public TokenEndLog() {
  }

  public TokenEndLog(Token child) {
    this.child = child;
  }

  public String toString() {
    return "tokenend["+child.getFullName()+"]";
  }

  public Token getChild() {
    return child;
  }
}
