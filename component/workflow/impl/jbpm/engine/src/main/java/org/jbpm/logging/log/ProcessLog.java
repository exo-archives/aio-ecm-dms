package org.jbpm.logging.log;

import java.io.*;
import java.util.*;
import org.jbpm.graph.exe.*;

public abstract class ProcessLog implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private long id = 0;
  protected int index = -1;
  protected Date date = null;
  protected Token token = null;
  protected CompositeLog parent = null;
  
  public ProcessLog() {
  }
  
  /**
   * provides a text description for this update
   * which can be used e.g. in the admin web console.
   */
  public abstract String toString();
  
  public String getActorId() {
    String actorId = null;
    if (parent!=null) {
      // AuthenticationLog overriddes the getActorId
      actorId = parent.getActorId();
    }
    return actorId;
  }
  
  public void setToken(Token token) {
    this.token = token;
    this.index = token.nextLogIndex();
  }

  public void setParent(CompositeLog parent) {
    this.parent = parent;
  }
  public long getId() {
    return id;
  }
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  public CompositeLog getParent() {
    return parent;
  }
  public Token getToken() {
    return token;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
}
