package org.jbpm.db;

import org.hibernate.*;

/**
 * contains queries to search the database for process instances and tokens
 * based on process variables.
 * <p><b>NOTE: TODO</b></p>
 */
public class ContextSession {

  JbpmSession jbpmSession = null;
  Session session = null;
  
  public ContextSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }

}
