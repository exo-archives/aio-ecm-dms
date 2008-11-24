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
package org.jbpm.module.exe;

import java.io.*;

import org.jbpm.graph.exe.*;

public class ModuleInstance implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected ProcessInstance processInstance = null;
  
  public ModuleInstance() {
  }

  public long getId() {
    return id;
  }
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
}
