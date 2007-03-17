package org.jbpm.scheduler.impl;

import java.util.Date;

import org.jbpm.scheduler.exe.Timer;

public interface SchedulerListener {

  void timerExecuted(Date date, Timer timer);
}
