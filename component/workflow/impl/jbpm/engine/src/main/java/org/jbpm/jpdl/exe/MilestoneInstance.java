package org.jbpm.jpdl.exe;

import java.io.*;
import java.util.*;

import org.jbpm.context.exe.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

public class MilestoneInstance implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected long id = 0;
  protected String name = null;
  protected boolean reached = false;
  protected Token token = null;
  protected Collection listeners = null;
  
  public MilestoneInstance() {
  }

  public MilestoneInstance(String name) {
    this.name = name;
  }
  
  public static MilestoneInstance getMilestoneInstance(String milestoneName, Token token) {
    ContextInstance ci = (ContextInstance) token.getProcessInstance().getInstance(ContextInstance.class);
    MilestoneInstance mi = (MilestoneInstance) ci.getVariable( milestoneName, token );
    if (mi == null) {
      mi = new MilestoneInstance(milestoneName);
      mi.setToken(token);
      ci.setVariable( milestoneName, mi );
    }
    return mi;
  }

  public void addListener(Token token) {
    if ( listeners == null ) listeners = new HashSet();
    listeners.add( token );
  }
  
  public void notifyListeners() {
    if ( listeners != null ) {
      // for every token that was waiting for this milestone
      Iterator iter = listeners.iterator();
      while (iter.hasNext()) {
        Token token = (Token) iter.next();
        // leave the milestone node
        Node node = token.getNode();
        ExecutionContext executionContext = new ExecutionContext(token);
        node.leave(executionContext);
      }
    }
  }

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public Collection getListeners() {
    return listeners;
  }
  public void setListeners(Collection listeners) {
    this.listeners = listeners;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean isReached() {
    return reached;
  }
  public void setReached(boolean reached) {
    this.reached = reached;
  }
  public Token getToken() {
    return token;
  }
  public void setToken(Token token) {
    this.token = token;
  }
}
