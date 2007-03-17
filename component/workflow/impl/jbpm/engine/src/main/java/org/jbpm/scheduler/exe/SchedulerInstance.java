package org.jbpm.scheduler.exe;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.module.exe.ModuleInstance;

/**
 * process instance extension for scheduling and cancalling timers.
 */
public class SchedulerInstance extends ModuleInstance {

  private static final long serialVersionUID = 1L;

  private boolean isProcessEnded = false;
  private List scheduledTimers = new ArrayList();
  private List cancelledTimerNames = new ArrayList();
  
  public SchedulerInstance() {
  }

  public SchedulerInstance(ProcessInstance processInstance) {
    setProcessInstance(processInstance);
  }

  /**
   * schedules a timer.
   */
  public void schedule( Timer timer ) {
    scheduledTimers.add(timer);
  }
  
  public static class CancelledTimer {
    String timerName;
    Token token;
    public CancelledTimer(String timerName, Token token) {
      this.timerName = timerName;
      this.token = token;
    }
    public String getTimerName() {
      return timerName;
    }
    public Token getToken() {
      return token;
    }
  }

  /**
   * cancels all scheduled timers with the given name.
   */
  public void cancel(String timerName, Token token) {
    cancelledTimerNames.add(new CancelledTimer(timerName, token));
  }

  public List getScheduledTimers() {
    return scheduledTimers;
  }
  
  public List getCancelledTimerNames() {
    return cancelledTimerNames;
  }
  public boolean isProcessEnded() {
    return isProcessEnded;
  }
  public void setProcessEnded(boolean isProcessEnded) {
    this.isProcessEnded = isProcessEnded;
  }
}
