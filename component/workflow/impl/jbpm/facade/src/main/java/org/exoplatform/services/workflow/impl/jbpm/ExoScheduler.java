package org.exoplatform.services.workflow.impl.jbpm;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.scheduler.exe.Timer;
import org.jbpm.scheduler.impl.SchedulerHistoryLog;
import org.jbpm.scheduler.impl.SchedulerListener;

public class ExoScheduler {

  ExoSchedulerThread schedulerThread = null;
  LinkedList historyLogs = new LinkedList();
  int interval = 5000;
  int historyMaxSize = 30;
  private JbpmSessionFactory jbpmSessionFactory;
  private String containerName;

  public ExoScheduler(String containerName) {
    this.containerName = containerName;
  }
  
  public void start() {
    schedulerThread = new ExoSchedulerThread(containerName);
    schedulerThread.setInterval(interval);
    schedulerThread.addListener(new HistoryListener());
    schedulerThread.start();
  }

  public void stop() {
    if (isRunning()) {
      schedulerThread.keepRunning = false;
      schedulerThread.interrupt();
      schedulerThread = null;
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

}
