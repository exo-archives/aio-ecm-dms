package org.jbpm.scheduler.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.scheduler.exe.Timer;

public class Scheduler {
  
  SchedulerThread schedulerThread = null;
  LinkedList historyLogs = new LinkedList();
  int interval = 5000;
  int historyMaxSize = 30;
  
  public void start() {
    log.debug("starting the scheduler");
    schedulerThread = new SchedulerThread();
    schedulerThread.setInterval(interval);
    schedulerThread.addListener(new HistoryListener());
    schedulerThread.start();
  }

  public void stop() {
    if (isRunning()) {
      log.debug("stopping the scheduler");
      schedulerThread.keepRunning = false;
      schedulerThread.interrupt();
      schedulerThread = null;
    } else {
      log.debug("scheduler can't be stopped cause it was not running");
    }
  }
  
  public boolean isRunning() {
    return ( (schedulerThread!=null)
             && (schedulerThread.isAlive()) );
  }
  
  public List getSchedulerHistoryLogs() {
    return historyLogs;
  }

  public void clearSchedulerHistoryLogs() {
    historyLogs.clear();
  }

  class HistoryListener implements SchedulerListener {
    public void timerExecuted(Date date, Timer timer) {
      historyLogs.add(new SchedulerHistoryLog(date, timer));
      if (historyLogs.size()>historyMaxSize) {
        historyLogs.removeLast();
      }
    }
  }
  
  public int getHistoryMaxSize() {
    return historyMaxSize;
  }
  public void setHistoryMaxSize(int historyMaxSize) {
    this.historyMaxSize = historyMaxSize;
  }
  public int getInterval() {
    return interval;
  }
  public void setInterval(int interval) {
    this.interval = interval;
  }
  public SchedulerThread getSchedulerThread() {
    return schedulerThread;
  }
  
  private static final Log log = LogFactory.getLog(Scheduler.class);
}
