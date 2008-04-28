/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
