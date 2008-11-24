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

import java.util.ArrayList;
import java.util.List;

public class CompositeLog extends ProcessLog {
  
  private static final long serialVersionUID = 1L;
  
  private List children = null;
  
  public CompositeLog() {
  }

  public List getChildren() {
    return children;
  }
  public void setChildren(List children) {
    this.children = children;
  }

  public String toString() {
    return "composite";
  }

  public void addChild(ProcessLog processLog) {
    if (children==null) children = new ArrayList();
    children.add(processLog);
  }
}
