package org.jbpm.scheduler.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.db.SchedulerSession;
import org.jbpm.scheduler.exe.Timer;

public class SchedulerThread extends Thread {
  
  static JbpmSessionFactory jbpmSessionFactory = JbpmSessionFactory.getInstance();
  static BusinessCalendar businessCalendar = new BusinessCalendar();
  
  List listeners = new ArrayList();
  boolean keepRunning = true;
  long interval = 5000;
  
  public SchedulerThread() {
    super("jbpm scheduler");
  }

  public void run() {
    while (keepRunning) {
      long millisToWait = interval;
      try {
        millisToWait = executeTimers();

        // calculate the milliseconds to wait...
        if (millisToWait < 0) {
          millisToWait = interval;
        }
        millisToWait = Math.min(millisToWait, interval);

      } catch (RuntimeException e) {
        log.info("runtime exception while executing timers", e);
      } finally {
        try {
          Thread.sleep(millisToWait);
        } catch (InterruptedException e) {
          log.info("waiting for timers got interuppted");
        }
      }
    }
    log.info("ending scheduler thread");
  }
  
  /**
   * executes due timers and calculates the time before the next timer is due.
   * @return the number of milliseconds till the next job is due or -1 if no timer is 
   * schedulerd in the future.
   */
  public long executeTimers() {
    long millisTillNextTimerIsDue = -1;
    boolean isDueDateInPast = true;
    
    JbpmSession jbpmSession = jbpmSessionFactory.openJbpmSessionAndBeginTransaction();
    try {
      SchedulerSession schedulerSession = new SchedulerSession(jbpmSession);
      
      log.debug("checking for timers");
      Iterator iter = schedulerSession.findTimersByDueDate();
      while( (iter.hasNext())
             && (isDueDateInPast)
           ) {
        Timer timer = (Timer) iter.next();
        log.debug("found timer "+timer);
        
        // if this timer is due
        if (timer.isDue()) {
          log.debug("executing timer '"+timer+"'");
            
          // execute
          timer.execute();
            
          // notify the listeners (e.g. the scheduler servlet)
          notifyListeners(timer);
            
          // if there was an exception, just save the timer
          if (timer.getException()!=null) {
            schedulerSession.saveTimer(timer);
            
          // if repeat is specified
          } else if (timer.getRepeat()!=null) {
            // update timer by adding the repeat duration
            Date dueDate = timer.getDueDate();
            
            // suppose that it took the timer runner thread a 
            // very long time to execute the timers.
            // then the repeat action dueDate could already have passed.
            while (dueDate.getTime()<=System.currentTimeMillis()) {
              dueDate = businessCalendar
                    .add(dueDate, 
                      new Duration(timer.getRepeat()));
            }
            timer.setDueDate( dueDate );
            // save the updated timer in the database
            log.debug("saving updated timer for repetition '"+timer+"' in '"+(dueDate.getTime()-System.currentTimeMillis())+"' millis");
            schedulerSession.saveTimer(timer);
            
          } else {
            // delete this timer
            log.debug("deleting timer '"+timer+"'");
            schedulerSession.deleteTimer(timer);
          }

        } else { // this is the first timer that is not yet due
          isDueDateInPast = false;
          millisTillNextTimerIsDue = timer.getDueDate().getTime() - System.currentTimeMillis();
        }
      }
      
    } finally {
      jbpmSession.commitTransactionAndClose();
    }
    
    return millisTillNextTimerIsDue;
  }

  // listeners ////////////////////////////////////////////////////////////////

  public void addListener(SchedulerListener listener) {
    if (listeners==null) listeners = new ArrayList();
    listeners.add(listener);
  }

  public void removeListener(SchedulerListener listener) {
    listeners.remove(listener);
    if (listeners.isEmpty()) {
      listeners = null;
    }
  }

  private void notifyListeners(Timer timer) {
    if (listeners!=null) {
      Date now = new Date();
      Iterator iter = new ArrayList(listeners).iterator();
      while (iter.hasNext()) {
        SchedulerListener timerRunnerListener = (SchedulerListener) iter.next();
        timerRunnerListener.timerExecuted(now, timer);
      }
    }
  }

  public void setInterval(long interval) {
    this.interval = interval;
  }
  
  private static final Log log = LogFactory.getLog(SchedulerThread.class);
}
