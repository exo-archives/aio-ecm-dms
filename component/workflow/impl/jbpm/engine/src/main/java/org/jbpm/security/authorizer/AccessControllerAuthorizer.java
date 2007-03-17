package org.jbpm.security.authorizer;

import java.security.AccessController;
import java.security.Permission;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Token;


public class AccessControllerAuthorizer implements Authorizer {

  public void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token) {
    AccessController.checkPermission(permission);
  }

}
