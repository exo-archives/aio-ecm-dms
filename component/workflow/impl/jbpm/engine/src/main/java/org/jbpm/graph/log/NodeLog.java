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

import java.util.Date;

import org.jbpm.graph.def.Node;
import org.jbpm.logging.log.ProcessLog;

public class NodeLog extends ProcessLog {
  
  private static final long serialVersionUID = 1L;
  
  protected Node node = null;
  protected Date enter = null;
  protected Date leave = null;
  protected long duration = -1;

  // constructors /////////////////////////////////////////////////////////////

  public NodeLog() {
  }
  
  public NodeLog(Node node, Date enter, Date leave) {
    this.node = node;
    this.enter = enter;
    this.leave = leave;
    this.duration = leave.getTime()-enter.getTime();
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public String toString() {
    return "node["+node.getName()+"]";
  }
  public long getDuration() {
    return duration;
  }
  public Date getEnter() {
    return enter;
  }
  public Date getLeave() {
    return leave;
  }
  public Node getNode() {
    return node;
  }
}
