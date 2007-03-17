package org.jbpm.security.authenticator;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.jbpm.JbpmConfiguration;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * gets the authenticated actor id from the current Subject.
 * This Authenticator requires another configuration parameter 
 * 'jbpm.authenticator.principal.classname'.  This configuration property 
 * specifies the class name of the principal that should be used from 
 * the current subject.  The name of that principal is used as the 
 * currently authenticated actorId. 
 */
public class SubjectAuthenticator implements Authenticator {
  
  private static final String principalClassName = JbpmConfiguration.getString("jbpm.authenticator.principal.classname");
  private static Class principalClass = ClassLoaderUtil.loadClass(principalClassName);

  public String getAuthenticatedActorId() {
    String authenticatedActorId = null;
    Subject subject = Subject.getSubject(AccessController.getContext());
    Set principals = subject.getPrincipals(principalClass);
    if ( (principals!=null)
         && (!principals.isEmpty()) 
       ) {
      Principal principal = (Principal) principals.iterator().next();
      authenticatedActorId = principal.getName();
    }
    return authenticatedActorId;
  }
}
