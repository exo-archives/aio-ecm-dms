package org.jbpm.logging.log;

public class MessageLog extends ProcessLog {

  private static final long serialVersionUID = 1L;
  
  String message = null;
  
  public MessageLog() {
  }

  public MessageLog(String message) {
    this.message = message;
  }

  public String toString() {
    return "message["+message+"]";
  }

  public String getMessage() {
    return message;
  }
}
