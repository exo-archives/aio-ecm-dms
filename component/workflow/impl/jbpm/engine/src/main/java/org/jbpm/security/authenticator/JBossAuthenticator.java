package org.jbpm.security.authenticator;

import org.jboss.security.SecurityAssociation;

/**
 * retrieves the actorId as the name of the principal from the SecurityAssociation.
 */
public class JBossAuthenticator implements Authenticator {

  public String getAuthenticatedActorId() {
    return SecurityAssociation.getPrincipal().getName();
  }
}
