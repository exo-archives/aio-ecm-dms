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

import java.util.*;
import org.dom4j.Element;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.jpdl.xml.*;

/**
 * a interleaving end node should have 2 leaving transitions.
 * one with the name 'back' that has the interleaving start node as 
 * destinationNode.  and one with the name 'done' that specifies the 
 * destinationNode in case the interleaving is done.   
 * Alternatively, the back and done transitions can be specified 
 * in this interleave handler.
 */
public class InterleaveEnd extends Node implements Parsable {
  
  private static final long serialVersionUID = 1L;
  
  private Transition back = null;
  private Transition done = null;

  public InterleaveEnd() {
  }

  public InterleaveEnd(String name) {
    super(name);
  }

  public void read(Element element, JpdlXmlReader jpdlReader) {
    // TODO
  }

  public void write(Element element) {
    // TODO
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    Node interleaveEndNode = token.getNode();
    Collection transitionNames = getInterleaveStart().retrieveTransitionNames(token);
    // if the set is *not* empty
    if ( ! transitionNames.isEmpty() ) {
      // go back to the interleave start handler
      String backTransitionName = "back";
      if ( back != null ) {
        backTransitionName = back.getName();
      }
      interleaveEndNode.leave(executionContext, backTransitionName);
    } else {
      // leave the to the
      getInterleaveStart().removeTransitionNames(token);
      String doneTransitionName = "done";
      if ( done != null ) {
        doneTransitionName = done.getName();
      }
      interleaveEndNode.leave(executionContext, doneTransitionName);
    }
  }
  
  public InterleaveStart getInterleaveStart() {
    return (InterleaveStart) getLeavingTransition("back").getTo();
  }

  public Transition getBack() {
    return back;
  }
  public void setBack(Transition back) {
    this.back = back;
  }
  public Transition getDone() {
    return done;
  }
  public void setDone(Transition done) {
    this.done = done;
  }
}
