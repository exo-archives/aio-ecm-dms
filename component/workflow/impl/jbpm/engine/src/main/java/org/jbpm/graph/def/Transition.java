package org.jbpm.graph.def;

import java.util.*;

import org.jbpm.graph.exe.*;
import org.jbpm.graph.log.*;

public class Transition extends GraphElement {

  private static final long serialVersionUID = 1L;
  
  protected Node from = null;
  protected Node to = null;

  // event types //////////////////////////////////////////////////////////////

  public static final String[] supportedEventTypes = new String[]{Event.EVENTTYPE_TRANSITION};
  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  // constructors /////////////////////////////////////////////////////////////
  
  public Transition() {
  }

  public Transition(String name) {
    super(name);
  }

  // from /////////////////////////////////////////////////////////////////////
  
  public Node getFrom() {
    return from;
  }

  /**
   * sets the from node unidirectionally.  use {@link Node#addLeavingTransition(Transition)}
   * to get bidirectional relations mgmt.
   */
  public void setFrom(Node from) {
    this.from = from;
  }

  // to ///////////////////////////////////////////////////////////////////////
  
  /**
   * sets the to node unidirectionally.  use {@link Node#addArrivingTransition(Transition)}
   * to get bidirectional relations mgmt.
   */
  public void setTo(Node to) {
    this.to = to;
  }

  public Node getTo() {
    return to;
  }

  // behaviour ////////////////////////////////////////////////////////////////

  /**
   * passes execution over this transition.
   */
  public void take(ExecutionContext executionContext) {
    // update the runtime context information 
    executionContext.getToken().setNode(null);
    
    Token token = executionContext.getToken();
    
    // start the transition log
    TransitionLog transitionLog = new TransitionLog(this, executionContext.getTransitionSource());
    token.startCompositeLog(transitionLog);
    try {
      
      // fire leave events for superstates (if any)
      fireSuperStateLeaveEvents(executionContext);
      
      // fire the transition event (if any)
      fireEvent(Event.EVENTTYPE_TRANSITION, executionContext);
      
      // fire enter events for superstates (if any)
      Node destination = fireSuperStateEnterEvents(executionContext);
      // update the ultimate destinationNode of this transition 
      transitionLog.setDestinationNode(destination);
      
    } finally {
      // end the transition log
      token.endCompositeLog();
    }
    
    // pass the token to the destinationNode node
    to.enter(executionContext);
  }

  private Node fireSuperStateEnterEvents(ExecutionContext executionContext) {
    // calculate the actual destinationNode node
    Node destination = to;
    while (destination instanceof SuperState) {
      destination = (Node) ((SuperState) destination).getNodes().get(0);
    }
    
    // performance optimisation: check if at least there is a candidate superstate to be entered.
    if ( destination.getSuperState()!=null ) {
      // collect all the superstates being left
      List leavingSuperStates = collectAllSuperStates(destination, from);
      // reverse the order so that events are fired from outer to inner superstates
      Collections.reverse(leavingSuperStates);
      // fire a superstate-enter event for all superstates being left
      fireSuperStateEvents(leavingSuperStates, Event.EVENTTYPE_SUPERSTATE_ENTER, executionContext);
    }
    
    return destination;
  }

  private void fireSuperStateLeaveEvents(ExecutionContext executionContext) {
    // performance optimisation: check if at least there is a candidate superstate to be left.
    if (executionContext.getTransitionSource().getSuperState()!=null) {
      // collect all the superstates being left
      List leavingSuperStates = collectAllSuperStates(executionContext.getTransitionSource(), to);
      // fire a node-leave event for all superstates being left
      fireSuperStateEvents(leavingSuperStates, Event.EVENTTYPE_SUPERSTATE_LEAVE, executionContext);
    }
  }

  /**
   * collect all superstates of a that do not contain node b.
   */
  private static List collectAllSuperStates(Node a, Node b) {
    SuperState superState = a.getSuperState();
    List leavingSuperStates = new ArrayList();
    while (superState!=null) {
      if (!superState.containsNode(b)) {
        leavingSuperStates.add(superState);
        superState = superState.getSuperState();
      } else {
        superState = null;
      }
    }
    return leavingSuperStates;
  }

  /**
   * fires the give event on all the superstates in the list.
   */
  private void fireSuperStateEvents(List superStates, String eventType, ExecutionContext executionContext) {
    Iterator iter = superStates.iterator();
    while (iter.hasNext()) {
      SuperState leavingSuperState = (SuperState) iter.next();
      leavingSuperState.fireEvent(eventType, executionContext);
    }
  }

  // other
  /////////////////////////////////////////////////////////////////////////////
  
  public void setName(String name) {
    if (from!=null) {
      if ( from.hasLeavingTransition(name) ) {
        throw new IllegalArgumentException("couldn't set name '"+name+"' on transition '"+this+"'cause the from-node of this transition has already another leaving transition with the same name");
      }
      Map fromLeavingTransitions = from.getLeavingTransitionsMap();
      fromLeavingTransitions.remove(this.name);
      fromLeavingTransitions.put(name,this);
    }
    this.name = name;
  }

  public GraphElement getParent() {
    GraphElement parent = null;
    if ( (from!=null)
         && (to!=null) ) {
      if (from==to) {
        parent = from.getParent();
      } else {
        List fromParentChain = from.getParentChain();
        List toParentChain = to.getParentChain();
        Iterator fromIter = fromParentChain.iterator();
        while ( fromIter.hasNext() && (parent==null) ) {
          GraphElement fromParent = (GraphElement) fromIter.next();
          Iterator toIter = toParentChain.iterator();
          while ( toIter.hasNext() && (parent==null) ) {
            GraphElement toParent = (GraphElement) toIter.next();
            if (fromParent==toParent) {
              parent = fromParent;
            }
          }
        }
      }
    }
    return parent;
  }
}
