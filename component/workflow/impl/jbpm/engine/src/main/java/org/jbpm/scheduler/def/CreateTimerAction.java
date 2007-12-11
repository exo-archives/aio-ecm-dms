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
package org.jbpm.scheduler.def;

import java.util.Date;

import org.dom4j.Element;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.scheduler.exe.Timer;

public class CreateTimerAction extends Action {

  private static final long serialVersionUID = 1L;
  private static BusinessCalendar businessCalendar = new BusinessCalendar(); 

  String timerName = null;
  String dueDate = null;
  String repeat = null;
  String transitionName = null;
  Action timerAction = null;
  
  public void read(Element actionElement, JpdlXmlReader jpdlReader) {
    timerName = actionElement.attributeValue("name");
    timerAction = jpdlReader.readSingleAction(actionElement);
    
    dueDate = actionElement.attributeValue("duedate");
    if (dueDate==null) {
      jpdlReader.addWarning("no duedate specified in create timer action '"+actionElement+"'");
    }
    repeat = actionElement.attributeValue("repeat");
    if ( "true".equalsIgnoreCase(repeat)
         || "yes".equalsIgnoreCase(repeat) ) {
      repeat = dueDate;
    }
    transitionName = actionElement.attributeValue("transition");
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    Duration duration = new Duration(dueDate);
    Date dueDate = businessCalendar.add( new Date(), duration );
    
    Timer timer = new Timer(executionContext.getToken());
    timer.setName(timerName);
    timer.setRepeat(repeat);
    timer.setDueDate(dueDate);
    timer.setAction(timerAction);
    timer.setTransitionName(transitionName);
    timer.setGraphElement(executionContext.getEventSource());
    timer.setTaskInstance(executionContext.getTaskInstance());

    executionContext.getSchedulerInstance().schedule(timer);
  }
  public String getDueDate() {
    return dueDate;
  }
  public void setDueDate(String dueDateDuration) {
    this.dueDate = dueDateDuration;
  }
  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeatDuration) {
    this.repeat = repeatDuration;
  }
  public String getTransitionName() {
    return transitionName;
  }
  public void setTransitionName(String transitionName) {
    this.transitionName = transitionName;
  }
  public String getTimerName() {
    return timerName;
  }
  public void setTimerName(String timerName) {
    this.timerName = timerName;
  }
  public Action getTimerAction() {
    return timerAction;
  }
  public void setTimerAction(Action timerAction) {
    this.timerAction = timerAction;
  }
}
