package org.jbpm.security;

import java.security.*;

import org.jbpm.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.security.authorizer.*;

/**
 * provides central access point to the configurable authorization.
 * <p>
 * By default, jBPM does not do authorization checking.  To activate 
 * authorization checking, set the property 'jbpm.authorizer' to the 
 * fully qualified class name of an {@link org.jbpm.security.authorizer.Authorizer}
 * implementation.
 * </p>
 * <p>
 * When an {@link org.jbpm.security.authorizer.Authorizer} is configured, 
 * the jBPM code will call its checkPermission method for security sensitive 
 * workflow operations.  The workflow operation being performed will be indicated 
 * with a java.security.Permission.
 * </p>
 * <p>
 * Package org.jbpm.security.authorizer contains a number of {@link org.jbpm.security.authorizer.Authorizer}
 * implementations and package org.jbpm.security.permission contains the 
 * permissions that are checked by the jBPM code.
 * </p>
 */ 
public class Authorization {
  
  protected static Authorizer authorizer = (Authorizer) JbpmConfiguration.getObject("jbpm.authorizer", null);

  /**
   * central method called by the jBPM code to check authorization.  This method
   * will delegate to the configured {@link Authorizer}
   * @throws AccessControlException if the current authenticated actor is not 
   * authorized.  
   */
  public static void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token) {
    if (authorizer!=null) {
      authorizer.checkPermission(permission, processDefinition, token);
    }
  }
}
