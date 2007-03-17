package org.jbpm.security.permission;

import java.security.BasicPermission;

public class ViewTaskParametersPermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public ViewTaskParametersPermission(String name, String actions) {
    super(name, actions);
  }
}
