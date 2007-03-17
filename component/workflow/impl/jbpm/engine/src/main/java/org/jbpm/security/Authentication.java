package org.jbpm.security;

import org.jbpm.JbpmConfiguration;
import org.jbpm.security.authenticator.Authenticator;
import org.jbpm.security.authenticator.JbpmDefaultAuthenticator;

/**
 * provides central access point to the configurable authentication.
 * 
 * <p>
 * Authentication (knowing which user is running this code) is outside the 
 * scope of jBPM.  It is assumed that the environment in which jBPM executes
 * has already performed authentication and hence knows who is running the 
 * code.
 * </p>
 * <p>
 * The implementations that can be plugged into this by configuring 
 * an {@link org.jbpm.security.authenticator.Authenticator} implementation 
 * in the jbpm.properties.  To configure another authenticator then the default 
 * one, specify the fully qualified class name in the property 'jbpm.authenticator'.
 * </p>
 * <p>The default is 
 * {@link org.jbpm.security.authenticator.JbpmDefaultAuthenticator}.  That 
 * authenticator assumes that the user calls {@link #pushAuthenticatedActorId(String)}
 * and {@link #popAuthenticatedActorId()} in a try-finally block around the 
 * the jbpm API invocations.  For web applications, there is a filter 
 * ({@link org.jbpm.security.filter.JbpmAuthenticationFilter})that calls these 
 * methods based on the httpServletRequest.getUserPrincipal().getName();  
 * </p>
 * <p>Other authentication implementation include : 
 * <ul>
 *   <li>{@link org.jbpm.security.authenticator.JBossAuthenticator} that looks 
 *   up the currently authenticated user from the JBoss SecurityAssociation.</li>
 *   <li>{@link org.jbpm.security.authenticator.SubjectAuthenticator} that looks 
 *   up the currently authenticated user from the current subject with 
 *   Subject.getSubject(...).</li>
 * </ul>
 * </p>
 */
public abstract class Authentication {
  
  // pluggable authentication /////////////////////////////////////////////////

  protected static Authenticator authenticator = (Authenticator) JbpmConfiguration.getObject("jbpm.authenticator", new JbpmDefaultAuthenticator());

  /**
   * central method to look up the currently authenticated swimlaneActorId.
   * (currently means relative to the current thread).
   * This method delegates the current swimlaneActorId lookup to a configurable 
   * {@link Authenticator}.   
   */
  public static String getAuthenticatedActorId() {
    return authenticator.getAuthenticatedActorId();
  }
  
  // jbpm's default authentication convenience methods ////////////////////////

  /**
   * conventience method for setting the authenticated swimlaneActorId for the 
   * jbpm default authentication mechanism.  Always use this method in 
   * a try-finally block with {@link #popAuthenticatedActorId()} in the 
   * finally block. 
   * @see JbpmDefaultAuthenticator
   */
  public static void pushAuthenticatedActorId(String actorId) {
    JbpmDefaultAuthenticator.pushAuthenticatedActorId(actorId);
  }
  
  /**
   * conventience method for ending the authenticated section for the 
   * jbpm default authentication mechanism.  Always use this method in 
   * a finally block with {@link #pushAuthenticatedActorId(String)} in the 
   * try block. 
   * @see JbpmDefaultAuthenticator
   */
  public static void popAuthenticatedActorId() {
    JbpmDefaultAuthenticator.popAuthenticatedActorId();
  }
}
