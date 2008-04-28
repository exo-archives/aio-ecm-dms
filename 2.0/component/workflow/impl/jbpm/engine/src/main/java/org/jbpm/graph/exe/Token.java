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
package org.jbpm.graph.exe;

import java.io.*;
import java.util.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.log.*;
import org.jbpm.logging.exe.*;
import org.jbpm.logging.log.*;

/**
 * represents one path of execution and maintains a pointer to a node 
 * in the {@link org.jbpm.graph.def.ProcessDefinition}.  Most common 
 * way to get a hold of the token objects is with {@link ProcessInstance#getRootToken()}
 * or {@link org.jbpm.graph.exe.ProcessInstance#findToken(String)}.
 */
public class Token implements Serializable {

  private static final long serialVersionUID = 1L;

  long id = 0;
  protected String name = null;
  protected Date start = null;
  protected Date end = null;
  protected Node node = null;
  protected Date nodeEnter = null;
  protected ProcessInstance processInstance = null;
  protected Token parent = null;
  protected Map children = null;
  protected List comments = null;
  protected ProcessInstance subProcessInstance = null;
  protected int nextLogIndex = 0;
  boolean isAbleToReactivateParent = true;
  boolean isTerminationImplicit = false;
  
  // logs should not be retrieved from the database to perform a workflow 
  // operation on a token.  therefor, the logs here are transient.  all 
  // workflow logs are just added.  when this token is saved to the database,
  // these logs should just be appended to the logs in the database.
  transient List logs = null;
  transient CompositeLog currentOperationLog = null;

  // constructors
  /////////////////////////////////////////////////////////////////////////////
  
  public Token() {
  }

  /**
   * creates a root token.
   */
  public Token(ProcessInstance processInstance) {
    this.start = new Date();
    this.processInstance = processInstance;
    this.node = processInstance.getProcessDefinition().getStartState();
    this.isTerminationImplicit = processInstance.getProcessDefinition().isTerminationImplicit();
  }

  /**
   * creates a child token.
   */
  public Token(Token parent, String name) {
    this.start = new Date();
    this.processInstance = parent.getProcessInstance();
    this.name = name;
    this.node = parent.node;
    this.parent = parent;
    parent.addChild(this);
    this.isTerminationImplicit = parent.isTerminationImplicit;
    this.currentOperationLog = parent.currentOperationLog;
    
    parent.addLog(new TokenCreateLog(this));
  }

  // operations
  /////////////////////////////////////////////////////////////////////////////

  private void addChild(Token token) {
    if (children==null) {
      children = new HashMap();
    }
    children.put(token.getName(), token);
  }

  /**
   * provides a signal to the token. this method activates this token and leaves
   * the current state over the default transition.
   */
  public void signal() {
    if ( (node == null) || (node.getDefaultLeavingTransition() == null)) {
      throw new IllegalStateException("couldn't signal token '" + this + "' : couldn't leave node '" + node + "' over its default transition");
    }
    signal(node.getDefaultLeavingTransition());
  }

  /**
   * provides a signal to the token. this leave the current state over the given
   * transition name.
   */
  public void signal(String transitionName) {
    if ((node == null) || (node.getLeavingTransition(transitionName) == null)) {
      throw new IllegalStateException("couldn't signal token '" + this + "' : couldn't leave node '" + node + "' over the its transition '" + transitionName
              + "'");
    }
    signal(node.getLeavingTransition(transitionName));
  }
  
  /**
   * provides a signal to the token. this leave the current state over the given
   * transition name.
   */
  public void signal(Transition transition) {
    if (transition == null) {
      throw new IllegalArgumentException("couldn't signal without specifying  a leaving transition : transition is null");
    }

    startCompositeLog(new SignalLog(transition));
    try {
      
      // create the execution context
      ExecutionContext executionContext = new ExecutionContext(this);
      
      // fire the event before-signal
      Node signalNode = node;
      signalNode.fireEvent(Event.EVENTTYPE_BEFORE_SIGNAL, executionContext);
      
      // start calculating the next state
      node.leave(executionContext, transition);
      
      // if required, check if this token is implicitly terminated
      checkImplicitTermination();
      
      // fire the event after-signal
      signalNode.fireEvent(Event.EVENTTYPE_AFTER_SIGNAL, executionContext);
      
    } finally {
      endCompositeLog();
    }
  }

  /**
   * ends this token and all of its children (if any). this is the last active (=not-ended) child of a parent token, 
   * the parent token will be ended as well and that verification will continue to 
   * propagate.
   */
  public void end() {
    end(true);
  }
  
  /**
   * ends this token with optional parent ending verification.
   * @param verifyParentTermination specifies if the parent token should be checked for termination.
   * if verifyParentTermination is set to true and this is the last non-ended child of a parent token, 
   * the parent token will be ended as well and the verification will continue to propagate.
   */
  public void end(boolean verifyParentTermination) {
    // if not already ended
    if (end==null) {

      // ended tokens cannot reactivate parents
      isAbleToReactivateParent = false;
      
      // set the end date
      // the end date is also the flag that indicates that this token has ended.
      this.end = new Date();
      
      // end all this token's children
      if (children != null) {
        Iterator iter = children.values().iterator();
        while (iter.hasNext()) {
          Token child = (Token) iter.next();
          if (!child.hasEnded()) {
            child.end();
          }
        }
      }
      
      // only log the end of child-tokens.  the process instance logs replace the root token logs.
      if (parent!=null) {
        // add a log
        parent.addLog(new TokenEndLog(this));
      }
      
      if (verifyParentTermination) {
        // if this is the last active token of the parent, 
        // the parent needs to be ended as well
        notifyParentOfTokenEnd();
      }
    }
  }
  
  // comments /////////////////////////////////////////////////////////////////

  public void addComment(String message) {
    addComment(new Comment(message));
  }

  public void addComment(Comment comment) {
    if (comments==null) comments = new ArrayList();
    comments.add(comment);
    comment.setToken(this);
  }
  
  public List getComments() {
    return comments;
  }
 
  // operations helper methods ////////////////////////////////////////////////

  /**
   * notifies a parent that one of its nodeMap has ended.
   */
  private void notifyParentOfTokenEnd() {
    if (isRoot()) {
      processInstance.end();
    } else {
      
      if (!parent.hasActiveChildren()) {
        parent.end();
      }
    }
  }

  /**
   * tells if this token has child tokens that have not yet ended.
   */
  public boolean hasActiveChildren() {
    boolean foundActiveChildToken = false;
    // try and find at least one child token that is
    // still active (= not ended)
    Iterator iter = children.values().iterator();
    while ((iter.hasNext()) && (!foundActiveChildToken)) {
      Token child = (Token) iter.next();
      if (!child.hasEnded()) {
        foundActiveChildToken = true;
      }
    }
    return foundActiveChildToken;
  }

  // log convenience methods //////////////////////////////////////////////////

  /**
   * convenience method for adding a process log.
   */
  public void addLog(ProcessLog processLog) {
    LoggingInstance li = (LoggingInstance) processInstance.getInstance(LoggingInstance.class);
    if (li != null) {
      processLog.setToken(this);
      li.addLog(processLog);
    }
  }
  /**
   * convenience method for starting a composite log.  When you add composite logs,
   * make sure you put the {@link #endCompositeLog()} in a finally block.
   */
  public void startCompositeLog(CompositeLog compositeLog) {
    LoggingInstance li = (LoggingInstance) processInstance.getInstance(LoggingInstance.class);
    if (li != null) {
      compositeLog.setToken(this);
      li.startCompositeLog(compositeLog);
    }
  }
  /**
   * convenience method for ending a composite log.  Make sure you put this in a finally block.
   */
  public void endCompositeLog() {
    LoggingInstance li = (LoggingInstance) processInstance.getInstance(LoggingInstance.class);
    if (li != null) {
      li.endCompositeLog();
    }
  }

  // various information extraction methods ///////////////////////////////////

  public String toString() {
    return "Token("+getFullName()+")";
  }

  public boolean hasEnded() {
    return (end != null);
  }

  public boolean isRoot() {
    return (parent == null);
  }

  public boolean hasParent() {
    return (parent != null);
  }

  public boolean hasChild(String name) {
    return (children != null ? children.containsKey(name) : false);
  }

  public Token getChild(String name) {
    Token child = null;
    if (children != null) {
      child = (Token) children.get(name);
    }
    return child;
  }

  public String getFullName() {
    if (parent==null) return "/";
    if (parent.getParent()==null) return "/"+name;
    return parent.getFullName()+"/"+name;
  }

  public List getChildrenAtNode(Node aNode) {
    List foundChildren = new ArrayList();
    getChildrenAtNode(aNode, foundChildren);
    return foundChildren;
  }
  
  private void getChildrenAtNode(Node aNode, List foundTokens) {
    if(aNode.equals(node)) {
      foundTokens.add(this);
    }
    else if(children != null && !children.isEmpty()) {
      for(Iterator it = children.values().iterator(); it.hasNext();) {
        Token aChild = (Token)it.next();
        aChild.getChildrenAtNode(aNode, foundTokens);
      }
    }
  }
  
  public Token findToken(String relativeTokenPath) {
    if (relativeTokenPath == null)
      return null;
    String path = relativeTokenPath.trim();
    if (("".equals(path)) || (".".equals(path))) {
      return this;
    }
    if ("..".equals(path)) {
      return parent;
    }
    if (path.startsWith("/")) {
      Token root = processInstance.getRootToken();
      return root.findToken(path.substring(1));
    }
    if (path.startsWith("./")) {
      return findToken(path.substring(2));
    }
    if (path.startsWith("../")) {
      if (parent != null) {
        return parent.findToken(path.substring(3));
      }
      return null;
    }
    int slashIndex = path.indexOf('/');
    if (slashIndex == -1) {
      return (Token) (children != null ? children.get(path) : null);
    }
    Token token = null;
    String name = path.substring(0, slashIndex);
    token = (Token) children.get(name);
    if (token != null) {
      return token.findToken(path.substring(slashIndex + 1));
    }
    return null;
  }

  public Map getActiveChildren() {
    Map activeChildren = new HashMap();
    if (children != null) {
      Iterator iter = children.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry) iter.next();
        Token child = (Token) entry.getValue();
        if (!child.hasEnded()) {
          String childName = (String) entry.getKey();
          activeChildren.put(childName, child);
        }
      }
    }
    return activeChildren;
  }

  public void checkImplicitTermination() {
    if (isTerminationImplicit && node.hasNoLeavingTransitions()) {
      end();
      
      if (processInstance.isTerminatedImplicitly()) {
        processInstance.end();
      }
    }
  }

  public boolean isTerminatedImplicitly() {
    if (end != null) return true;

    Map leavingTransitions = node.getLeavingTransitionsMap();
    if ((leavingTransitions != null) && (leavingTransitions.size() > 0)) {
      // ok: found a non-terminated token
      return false;
    }

    // loop over all active child tokens
    Iterator iter = getActiveChildren().values().iterator();
    while (iter.hasNext()) {
      Token child = (Token) iter.next();
      if (!child.isTerminatedImplicitly()) {
        return false;
      }
    }
    // if none of the above, this token is terminated implicitly
    return true;
  }

  public int nextLogIndex() {
    return nextLogIndex++;
  }

  // getters and setters
  /////////////////////////////////////////////////////////////////////////////

  public long getId() {
    return id;
  }
  public Date getStart() {
    return start;
  }
  public Date getEnd() {
    return end;
  }
  public String getName() {
    return name;
  }
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  public Map getChildren() {
    return children;
  }
  public Node getNode() {
    return node;
  }
  public void setNode(Node node) {
    this.node = node;
  }
  public Token getParent() {
    return parent;
  }
  public void setParent(Token parent) {
    this.parent = parent;
  }
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
  public ProcessInstance getSubProcessInstance() {
    return subProcessInstance;
  }
  public void setSubProcessInstance(ProcessInstance subProcessInstance) {
    this.subProcessInstance = subProcessInstance;
  }
  public Date getNodeEnter() {
    return nodeEnter;
  }
  public void setNodeEnter(Date nodeEnter) {
    this.nodeEnter = nodeEnter;
  }
  public boolean isAbleToReactivateParent() {
    return isAbleToReactivateParent;
  }
  public void setAbleToReactivateParent(boolean isAbleToReactivateParent) {
    this.isAbleToReactivateParent = isAbleToReactivateParent;
  }
  public boolean isTerminationImplicit() {
    return isTerminationImplicit;
  }
  public void setTerminationImplicit(boolean isTerminationImplicit) {
    this.isTerminationImplicit = isTerminationImplicit;
  }
  // private static final Log log = LogFactory.getLog(Token.class);
}
