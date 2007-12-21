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
