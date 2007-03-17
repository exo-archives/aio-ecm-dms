package org.jbpm.security.permission;

import java.security.BasicPermission;

public class DeployProcessPermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public DeployProcessPermission(String name, String actions) {
    super(name, actions);
  }
}
