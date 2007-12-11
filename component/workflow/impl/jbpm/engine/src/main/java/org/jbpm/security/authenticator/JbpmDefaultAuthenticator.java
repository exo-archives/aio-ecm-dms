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
