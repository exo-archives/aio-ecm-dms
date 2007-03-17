package org.jbpm.graph.node;

import java.util.*;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

/**
 * TODO is the merge node usefull ? 
 * i don't think the merge node is usefull because every node has an 
 * implicit merge in front of it (= multiple transitions can arrive in 
 * the same node).  maybe we should just leave this in for the sake 
 * of workflow patterns ?
 */
public class Merge extends Node {
  
  private static final long serialVersionUID = 1L;
  
  private boolean isSynchronized = false;
  
  public Merge() {
  }

  public Merge(String name) {
    super(name);
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    Node mergeNode = token.getNode();
    
    // if this is a simple merge
    if ( ! isSynchronized ) {
      mergeNode.leave(executionContext);

    // this is a synchronizing multi merge
    } else {
      
      Collection concurrentTokens = token.getParent().getChildren().values();
      boolean reactivate = true;
      Iterator iter = concurrentTokens.iterator();
      while ( (iter.hasNext())
              && (reactivate) ) {
        Token concurrentToken = (Token) iter.next();
        if ( concurrentToken.getNode() != mergeNode ) {
          reactivate = false;
        }
      }
      
      if ( reactivate ) {
        iter = concurrentTokens.iterator();
        while (iter.hasNext()) {
          Token concurrentToken = (Token) iter.next();
          mergeNode.leave(new ExecutionContext(concurrentToken));
        }
      }
    }
  }
  
  public boolean isSynchronized() {
    return isSynchronized;
  }
  public void setSynchronized(boolean isSynchronized) {
    this.isSynchronized = isSynchronized;
  }
}
