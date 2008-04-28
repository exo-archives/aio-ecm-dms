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
