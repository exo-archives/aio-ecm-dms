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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.scheduler.exe.SchedulerInstance;
import org.jbpm.scheduler.exe.Timer;

public class SchedulerSession {

  JbpmSession jbpmSession = null;
  Session session = null;
  
  public SchedulerSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }

  public void saveTimer(Timer timer) {
    try {
      session.save(timer);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't save timer '"+timer+"' to the database", e);
    }
  }

  public void deleteTimer(Timer timer) {
    try {
      session.delete(timer);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't delete timer '"+timer+"' from the database", e);
    }
  }

  private static final String findTimersByDueDate = 
    "select ti " +
    "from org.jbpm.scheduler.exe.Timer as ti " +
    "where ti.exception is null " +
    "order by ti.dueDate asc";
  public Iterator findTimersByDueDate() {
    try {
      return session.createQuery(findTimersByDueDate).iterate();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't find timers from the database", e);
    }
  }

  private static final String findFailedTimers = 
    "select ti " +
    "from org.jbpm.scheduler.exe.Timer as ti " +
    "where ti.exception is not null " +
    "order by ti.dueDate asc";
  public Iterator findFailedTimers() {
    try {
      return session.createQuery(findFailedTimers).iterate();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't find failed timers from the database", e);
    }
  }

  void saveTimers(ProcessInstance processInstance) {
    SchedulerInstance schedulerInstance = processInstance.getSchedulerInstance();
    
    // if the process instance was ended,
    if (schedulerInstance.isProcessEnded()) {
      // delete all the timers for this process instance
      cancelTimersForProcessInstance(processInstance);
      
    } else {
      // save the scheduled timers
      Iterator iter = schedulerInstance.getScheduledTimers().iterator();
      while (iter.hasNext()) {
        Timer timer = (Timer) iter.next();
        log.debug("saving timer "+timer);
        session.save(timer);
      }
      
      // delete the cancelled timers
      iter = schedulerInstance.getCancelledTimerNames().iterator();
      while (iter.hasNext()) {
        SchedulerInstance.CancelledTimer cancelledTimer = (SchedulerInstance.CancelledTimer) iter.next();
        cancelTimers(cancelledTimer);
      }
    }
  }
  
  private static final String deleteTimersQuery = 
    "delete from org.jbpm.scheduler.exe.Timer " +
    "where name = :timerName" +
    "  and token = :token";
  public void cancelTimers(SchedulerInstance.CancelledTimer cancelledTimer) {
    try {
      Query query = session.createQuery(deleteTimersQuery);
      query.setString("timerName", cancelledTimer.getTimerName());
      query.setEntity("token", cancelledTimer.getToken());
      query.executeUpdate();
      
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't delete timers '"+cancelledTimer.getTimerName()+"' on token '"+cancelledTimer.getToken().getId()+"' from the database", e);
    }
  }

  private static final String deleteTimersForProcessInstanceQuery = 
    "delete from org.jbpm.scheduler.exe.Timer " +
    "where processInstance = :processInstance";
  public void cancelTimersForProcessInstance(ProcessInstance processInstance) {
    try {
      Query query = session.createQuery(deleteTimersForProcessInstanceQuery);
      query.setEntity("processInstance", processInstance);
      query.executeUpdate();
      
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't delete timers for process instance '"+processInstance+"'", e);
    }
  }

  private static final Log log = LogFactory.getLog(SchedulerSession.class);
}
