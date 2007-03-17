package org.jbpm.security.permission;

import java.security.BasicPermission;

public class SubmitTaskParametersPermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public SubmitTaskParametersPermission(String name, String actions) {
    super(name, actions);
  }
}
