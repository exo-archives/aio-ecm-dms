package org.jbpm.security.permission;

import java.security.BasicPermission;

public class CreateProcessInstancePermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public CreateProcessInstancePermission(String name, String actions) {
    super(name, actions);
  }
}
