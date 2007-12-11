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
package org.jbpm.taskmgmt.exe;

import java.io.Serializable;

import org.jbpm.context.def.VariableAccess;

public class TaskFormParameter implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String label = null;
  protected String description = null;
  protected Object value = null;
  protected boolean isReadable = true;
  protected boolean isWritable = true;
  protected boolean isRequired = true;
  
  public TaskFormParameter() {
  }
  
  public TaskFormParameter(VariableAccess variableAccess, Object value) {
    this.label = variableAccess.getMappedName();
    this.value = value;
    this.isReadable = variableAccess.isReadable();
    this.isWritable = variableAccess.isWritable();
    this.isRequired = variableAccess.isRequired();
  }

  public TaskFormParameter(TaskFormParameter other) {
    this.label = other.label;
    this.description = other.description;
    this.value = other.value;
    this.isReadable = other.isReadable;
    this.isWritable = other.isWritable;
    this.isRequired = other.isRequired;
  }

  public String toString() {
    return "("+label+","+value+")";
  }
  
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public boolean isReadable() {
    return isReadable;
  }
  public void setReadable(boolean isReadable) {
    this.isReadable = isReadable;
  }
  public boolean isRequired() {
    return isRequired;
  }
  public void setRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }
  public boolean isWritable() {
    return isWritable;
  }
  public boolean isReadOnly() {
     return !isWritable;
  }
  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
}
