package org.jbpm.security.authenticator;

/**
 * interface for plugging in different ways of retrieving the currently
 * authenticated actorId.
 */
public interface Authenticator {
  
  /**
   * get the actorId of the currently authenticated user.
   */
  String getAuthenticatedActorId();
}
