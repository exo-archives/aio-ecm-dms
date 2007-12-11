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
package org.jbpm.graph.node;

import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.*;

public class EndState extends Node {

  private static final long serialVersionUID = 1L;

  public EndState() {
  }
  
  public static final String[] supportedEventTypes = new String[]{Event.EVENTTYPE_NODE_ENTER};
  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  public EndState(String name) {
    super(name);
  }
  
  public void execute(ExecutionContext executionContext) {
    executionContext.getToken().end();
  }
  
  public Transition addLeavingTransition(Transition t) {
    throw new UnsupportedOperationException("can't add a leaving transition to an end-state");
  }
}
