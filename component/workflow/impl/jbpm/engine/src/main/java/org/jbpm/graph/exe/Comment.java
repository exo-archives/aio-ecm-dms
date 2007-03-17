package org.jbpm.graph.exe;

import java.io.Serializable;
import java.util.Date;

import org.jbpm.security.Authentication;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class Comment implements Serializable {

  private static final long serialVersionUID = 1L;

  protected long id = 0;
  protected String actorId = null;
  protected Date time = null;
  protected String message = null;
  protected Token token = null;
  protected TaskInstance taskInstance = null;

  public Comment() {
  }
  
  public Comment(String message) {
    this.actorId = Authentication.getAuthenticatedActorId();
    this.time = new Date();
    this.message = message;
  }
  
  public Comment(String actorId, String message) {
    this.actorId = actorId;
    this.time = new Date();
    this.message = message;
  }

  public String getActorId() {
    return actorId;
  }
  public long getId() {
    return id;
  }
  public String getMessage() {
    return message;
  }
  public Date getTime() {
    return time;
  }
  public TaskInstance getTaskInstance() {
    return taskInstance;
  }
  public Token getToken() {
    return token;
  }
  public void setTaskInstance(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
  }
  public void setToken(Token token) {
    this.token = token;
  }
}
