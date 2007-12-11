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
package org.jbpm.module.def;

import java.io.*;

import org.jbpm.graph.def.*;
import org.jbpm.module.exe.*;

public abstract class ModuleDefinition implements Serializable {

  long id = 0;
  protected String name = null;
  protected ProcessDefinition processDefinition = null;
  
  public ModuleDefinition() {
  }
  
  public abstract ModuleInstance createInstance();

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
}
