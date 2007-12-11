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
