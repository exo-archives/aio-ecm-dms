package org.jbpm.security.authorizer;

import java.security.Permission;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Token;

public class RolesAuthorizer implements Authorizer {

  public void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token) {
    /*
    String role = processDefinition.getSecurityDefinition().mapToRole(permission);
    
    // check if swimlaneActorId has role.
    SecurityAssociation.getSubject().getPrincipals(null);
    */
  }

}
