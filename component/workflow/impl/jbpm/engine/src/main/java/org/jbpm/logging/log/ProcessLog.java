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
