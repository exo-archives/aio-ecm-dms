package org.jbpm.graph.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jbpm.graph.def.Action;
import org.jbpm.logging.log.CompositeLog;

public class ActionLog extends CompositeLog {

  private static final long serialVersionUID = 1L;

  protected Action action = null;
  protected String exception = null;

  public ActionLog() {
  }

  public ActionLog(Action action) {
    this.action = action;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("action[");
    buffer.append(action);
    if (exception!=null) {
      buffer.append(", threw '");
      buffer.append(exception);
      buffer.append("'");
    }
    buffer.append("]");
    return buffer.toString();
  }
  
  public void setException(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    this.exception = stringWriter.toString();
  }
  public Action getAction() {
    return action;
  }
  public void setAction(Action action) {
    this.action = action;
  }
  public String getException() {
    return exception;
  }
}
