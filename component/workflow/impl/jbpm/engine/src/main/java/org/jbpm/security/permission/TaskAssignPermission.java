package org.jbpm.security.permission;

import java.security.BasicPermission;

public class TaskAssignPermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public TaskAssignPermission(String name, String actions) {
    super(name, actions);
  }
}
