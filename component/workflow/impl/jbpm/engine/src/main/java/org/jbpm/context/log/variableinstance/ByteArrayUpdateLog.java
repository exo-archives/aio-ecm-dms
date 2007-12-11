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
package org.jbpm.context.log.variableinstance;

import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.context.log.VariableUpdateLog;

public class ByteArrayUpdateLog extends VariableUpdateLog {
  
  private static final long serialVersionUID = 1L;
  
  ByteArray oldValue = null;
  ByteArray newValue = null;

  public ByteArrayUpdateLog() {
  }

  public ByteArrayUpdateLog(VariableInstance variableInstance, ByteArray oldValue, ByteArray newValue) {
    super(variableInstance);
    this.oldValue = (oldValue!=null ? new ByteArray(oldValue) : null );
    this.newValue = (newValue!=null ? new ByteArray(newValue) : null );
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public String toString() {
    String toString = null;
    if ( (oldValue==null)
         && (newValue==null) ) {
      toString = variableInstance+" remained null";
    } else if ( (oldValue!=null)
                && (oldValue.equals(newValue) )
              ) {
      toString = variableInstance+" unchanged";
    } else {
      toString = variableInstance+" binary content differs";
    }
    return toString;
  }
}
