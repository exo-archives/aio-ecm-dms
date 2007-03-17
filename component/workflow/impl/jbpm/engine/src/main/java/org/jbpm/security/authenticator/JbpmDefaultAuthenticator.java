package org.jbpm.security.authenticator;

import java.util.LinkedList;

/**
 * maintains a thread local stack of actorId's, the authenticated 
 * actorId is the first one on the stack.  Use {@link #pushAuthenticatedActorId(String)}
 * and {@link #popAuthenticatedActorId()} to manage the stack in a 
 * try-finally block.
 */
public class JbpmDefaultAuthenticator implements Authenticator {

  static ThreadLocal authenticatedActorIdStack = new ThreadLocal();
  
  public static void pushAuthenticatedActorId(String actorId) {
    LinkedList stack = (LinkedList) authenticatedActorIdStack.get();
    if (stack==null) {
      stack = new LinkedList();
      authenticatedActorIdStack.set(stack);
    }
    stack.addFirst(actorId);
  }
  
  public String getAuthenticatedActorId() {
    String authenticatedActorId = null;
    LinkedList stack = (LinkedList) authenticatedActorIdStack.get();
    if ( (stack!=null)
         && (!stack.isEmpty()) 
       ) {
      authenticatedActorId = (String) stack.getFirst();
    }
    return authenticatedActorId;
  }

  public static void popAuthenticatedActorId() {
    LinkedList stack = (LinkedList) authenticatedActorIdStack.get();
    stack.removeFirst();
  }
}
