package org.jbpm.security.permission;

import java.security.BasicPermission;

public class EndTaskPermission extends BasicPermission {

  private static final long serialVersionUID = 1L;

  public EndTaskPermission(String name, String actions) {
    super(name, actions);
    
  }

}
