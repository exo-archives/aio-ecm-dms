package org.jbpm.security.authorizer;

import java.security.*;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

/**
 * defines the interface for plugging in authorization mechanisms into jBPM.
 * 
 * <p>
 * If Authroization implementations need the current authenticated user, they 
 * can look that up via {@link org.jbpm.security.Authentication#getAuthenticatedActorId()}.
 * </p>
 */
public interface Authorizer {

  /**
   * verify if the currently authenticated has the given permission.
   * @throws AccessControlException if the current authenticated actor is not 
   * authorized.  
   */
  void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token)
  throws AccessControlException;
}
