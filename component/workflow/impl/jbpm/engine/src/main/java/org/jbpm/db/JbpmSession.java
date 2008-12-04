/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.jbpm.db;

import java.sql.Connection;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * represents the connection to the jbpm database.
 * You can obtain a DbSession with
 * <pre>
 * DbSession dbSession = dbSessionFactory.openDbSession();
 * </pre>
 * or  
 * <pre>
 * Connection jdbcConnection = ...;
 * DbSession dbSession = dbSessionFactory.openDbSession(jdbcConnection);
 * </pre>  
 * The actual database operations are defined in the modules :
 * <ul>
 *   <li>{@link org.jbpm.db.GraphSession}</li>
 *   <li>{@link org.jbpm.db.TaskMgmtSession}</li>
 *   <li>{@link org.jbpm.db.LoggingSession}</li>
 *   <li>{@link org.jbpm.db.FileSession}</li>
 * </ul>  
 * The easiest way to obtain the operations is like this :
 * <ul>
 *   <li><pre>jbpmSession.getGraphSession().someGraphDbMethod(...)</pre></li>
 *   <li><pre>jbpmSession.getTaskMgmtSession().someTaskDbMethod(...)</pre></li>
 *   <li><pre>jbpmSession.getLoggingSession().someLoggingDbMethod(...)</pre></li>
 *   <li><pre>jbpmSession.getContextSession().someFileDbMethod(...)</pre></li>
 *   <li><pre>jbpmSession.getFileSession().someFileDbMethod(...)</pre></li>
 * </ul>  
 */
public class JbpmSession {
  
  static ThreadLocal currentJbpmSessionStack = new ThreadLocal();
  
  private JbpmSessionFactory jbpmSessionFactory = null;
  private Session session = null;
  private Transaction transaction = null;
  
  private GraphSession graphSession = null;
  private ContextSession contextSession = null;
  private TaskMgmtSession taskMgmtSession = null;
  private LoggingSession loggingSession = null;
  private SchedulerSession schedulerSession = null;
  
  public JbpmSession( JbpmSessionFactory jbpmSessionFactory, Session session ) {
    this.jbpmSessionFactory = jbpmSessionFactory;
    this.session = session;
    this.graphSession = new GraphSession(this);
    this.contextSession = new ContextSession(this);
    this.taskMgmtSession = new TaskMgmtSession(this);
    this.loggingSession = new LoggingSession(this);
    this.schedulerSession = new SchedulerSession(this);
    
    pushCurrentSession();
  }
  
  public JbpmSessionFactory getJbpmSessionFactory() {
    return jbpmSessionFactory;
  }

  public Connection getConnection() {
    try {
      return session.connection();
    } catch (Exception e) {
      log.error(e);
      handleException();
      throw new RuntimeException( "couldn't get the jdbc connection from hibernate", e );
    }
  }

  public Session getSession() {
    return session;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void beginTransaction() {
    try {
      transaction = session.beginTransaction();
    } catch (Exception e) {
      log.error(e);
      handleException();
      throw new RuntimeException( "couldn't begin a transaction", e );
    }
  }

  public void commitTransaction() {
    if ( transaction == null ) {
      throw new RuntimeException("can't commit : no transaction started" );
    }
    try {
      session.flush();
      transaction.commit();
    } catch (Exception e) {
      log.error(e);
      handleException();
      throw new RuntimeException( "couldn't commit transaction", e );
    } finally {
      transaction = null;
    }
  }

  public void rollbackTransaction() {
    if ( transaction == null ) {
      throw new RuntimeException("can't rollback : no transaction started" );
    }
    try {
      transaction.rollback();
    } catch (Exception e) {
      log.error(e);
      handleException();
      throw new RuntimeException( "couldn't rollback transaction", e );
    } finally {
      transaction = null;
    }
  }
  
  public void commitTransactionAndClose() {
    try {
  	  commitTransaction();
  	  close();
    } catch(Throwable t) {
      rollbackTransactionAndClose();
    }
  }
  public void rollbackTransactionAndClose() {
    try {
      rollbackTransaction();
      close();
    } catch(Throwable t) {
      t.printStackTrace();
      close();
    }
  }
  
  public GraphSession getGraphSession() {
    return graphSession;
  }
  public ContextSession getContextSession() {
    return contextSession;
  }
  public TaskMgmtSession getTaskMgmtSession() {
    return taskMgmtSession;
  }
  public LoggingSession getLoggingSession() {
    return loggingSession;
  }
  public SchedulerSession getSchedulerSession() {
    return schedulerSession;
  }

  public void close() {
    try {
      if ( (session!=null)
           && (session.isOpen())
         ) {
        session.close();
      }
    } catch (Exception e) {
      log.error(e);
      throw new RuntimeException( "couldn't close the hibernate connection", e );
    } finally {
      popCurrentSession();
      session = null;
    }
  }

  /**
   * handles an exception that is thrown by hibernate.
   */
  void handleException() {
    // if hibernate throws an exception,  
    if (transaction!=null) {
      try {
        // the transaction should be rolled back
        transaction.rollback();
      } catch (HibernateException e) {
        log.error("couldn't rollback hibernate transaction", e);
      }
      // and the hibernate session should be closed.
      close();
    }
  }

  private void pushCurrentSession() {
    LinkedList stack = (LinkedList) currentJbpmSessionStack.get();
    if (stack==null) {
      stack = new LinkedList();
      currentJbpmSessionStack.set(stack);
    }
    stack.addFirst(this);
  }
  
  public static JbpmSession getCurrentJbpmSession() {
    JbpmSession jbpmSession = null;
    LinkedList stack = (LinkedList) currentJbpmSessionStack.get();
    if ( (stack!=null)
         && (! stack.isEmpty()) 
       ) {
      jbpmSession = (JbpmSession) stack.getFirst();
    }
    return jbpmSession;
  }

  private void popCurrentSession() {
    LinkedList stack = (LinkedList) currentJbpmSessionStack.get();
    if ( (stack==null)
         || (stack.isEmpty())
         || (stack.getFirst()!=this)
       ) {
      log.warn("can't pop current session: are you calling JbpmSession.close() multiple times ?");
    } else {
      stack.removeFirst();
    }
  }

  private static final Log log = LogFactory.getLog(JbpmSession.class);
}
