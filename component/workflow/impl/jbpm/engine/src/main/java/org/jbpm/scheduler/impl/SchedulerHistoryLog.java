package org.jbpm.scheduler.impl;

import java.io.Serializable;
import java.util.Date;
import org.jbpm.scheduler.exe.Timer;

public class SchedulerHistoryLog implements Serializable {

  private static final long serialVersionUID = 1L;
  
  Date date;
  Timer timer;
  
  public SchedulerHistoryLog(Date date, Timer timer) {
    this.date = date;
    this.timer = timer;
  }

  public Date getDate() {
    return date;
  }
  public Timer getTimer() {
    return timer;
  }
}
