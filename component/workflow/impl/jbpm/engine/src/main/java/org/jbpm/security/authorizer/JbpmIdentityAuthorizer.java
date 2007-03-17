package org.jbpm.security.authorizer;

import java.security.Permission;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Token;


public class JbpmIdentityAuthorizer implements Authorizer {

  public void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token) {
    /*
    String actorId = Authentication.getAuthenticatedActorId();
    JbpmSession.getCurrentJbpmSession();
    // ...
    */
  }

}
