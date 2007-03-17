package org.exoplatform.services.workflow.impl.jbpm;

import java.util.Date;

import org.jbpm.scheduler.exe.Timer;

public class TimerData implements org.exoplatform.services.workflow.Timer {
  
  private Timer jbpmTimer_;

  public TimerData(Timer jbpmTimer) {
   jbpmTimer_ = jbpmTimer; 
  }

  public String getId() {
    return ""+jbpmTimer_.getId();
  }

  public String getName() {
    return jbpmTimer_.getName();
  }

  public Date getDueDate() {
    return jbpmTimer_.getDueDate();
  }

}
