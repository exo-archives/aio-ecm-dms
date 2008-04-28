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
package org.jbpm.graph.def;

import java.io.*;
import java.util.*;
import org.jbpm.graph.exe.*;
import org.jbpm.instantiation.*;

public class ExceptionHandler implements Serializable {

  private static final long serialVersionUID = 1L;

  long id = 0;
  protected String exceptionClassName = null;
  protected GraphElement graphElement = null;
  protected List actions = null;

  public ExceptionHandler() {
  }

  public boolean matches( Throwable exception ) {
    boolean matches = true;
    if (exceptionClassName!=null) {
      Class clazz = ClassLoaderUtil.loadClass(exceptionClassName);
      if (! clazz.isAssignableFrom(exception.getClass())) {
        matches = false;
      }
    }
    return matches;
  }

  public void handleException(ExecutionContext executionContext) throws Exception {
    if (actions!=null) {
      Iterator iter = actions.iterator();
      while (iter.hasNext()) {
        Action action = (Action) iter.next();
        action.execute(executionContext);
      }
    }
  }

  // actions
  /////////////////////////////////////////////////////////////////////////////
  public List getActions() {
    return actions;
  }
  
  public void addAction(Action action) {
    if (actions==null) actions = new ArrayList();
    actions.add(action);
  }
  
  public void removeAction(Action action) {
    if (actions!=null) {
      actions.remove(action);
    }
  }

  public void reorderAction(int oldIndex, int newIndex) {
    if (actions!=null) {
      actions.add(newIndex, actions.remove(oldIndex));
    }
  }

  // getters and setters
  /////////////////////////////////////////////////////////////////////////////
  
  public String getExceptionClassName() {
    return exceptionClassName;
  }
  public void setExceptionClassName(String exceptionClassName) {
    this.exceptionClassName = exceptionClassName;
  }
  public GraphElement getGraphElement() {
    return graphElement;
  }
}
