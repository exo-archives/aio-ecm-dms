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
package org.jbpm.context.def;

import java.io.Serializable;

/**
 * specifies access to a variable.
 * Variable access is used in 3 situations:
 * 1) process-state 
 * 2) script 
 * 3) task controllers 
 */
public class VariableAccess implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String variableName = null;
  protected String access = null;
  protected String mappedName = null;

  // constructors /////////////////////////////////////////////////////////////

  public VariableAccess() {
  }

  public VariableAccess(String variableName, String access, String mappedName) {
    this.variableName = variableName;
    if (access!=null) access = access.toLowerCase();
    this.access = access;
    this.mappedName = mappedName;
  }

  // getters and setters //////////////////////////////////////////////////////

  /**
   * the mapped name.  The mappedName defaults to the variableName in case 
   * no mapped name is specified.  
   */
  public String getMappedName() {
    if (mappedName==null) {
      return variableName;
    }
    return mappedName;
  }

  /**
   * specifies a comma separated list of access literals {read, write, required}.
   */
  public String getAccess() {
    return access;
  }
  public String getVariableName() {
    return variableName;
  }
  
  public boolean isReadable() {
    return hasAccess("read");
  }

  public boolean isWritable() {
    return hasAccess("write");
  }

  public boolean isRequired() {
    return hasAccess("required");
  }

  /**
   * verifies if the given accessLiteral is included in the access text.
   */
  public boolean hasAccess(String accessLiteral) {
    if (access==null) return false;
    return (access.indexOf(accessLiteral.toLowerCase())!=-1);
  }
}
