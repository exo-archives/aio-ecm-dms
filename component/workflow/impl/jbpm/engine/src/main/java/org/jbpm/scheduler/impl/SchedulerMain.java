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
package org.jbpm.scheduler.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.scheduler.exe.Timer;

public class SchedulerMain {

  static DateFormat dateFormat = null;

  public static void main(String[] args) {
    // java SchedulerMain <interval> <historyMaxSize> <dateFormat>
    
    // create a new scheduler
    Scheduler scheduler = new Scheduler();
    
    // initialize it with the command line parameters 
    int interval = Integer.parseInt(getParameter(args, 0, "5000"));
    scheduler.setInterval(interval);
    int historyMaxSize = Integer.parseInt(getParameter(args, 1, "50"));
    scheduler.setHistoryMaxSize(historyMaxSize);
    dateFormat = new SimpleDateFormat(getParameter(args, 2, "dd/MM/yyyy HH:mm:ss"));
    
    // register the console listener
    scheduler.getSchedulerThread().addListener(new LogListener());
    
    // start the scheduler
    scheduler.start();
  }

  private static final String NEWLINE = System.getProperty("line.separator");
  private static class LogListener implements SchedulerListener {
    public void timerExecuted(Date date, Timer timer) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(dateFormat.format(date));
      buffer.append(" | ");
      buffer.append(timer.toString());
      buffer.append(" | ");
      if (timer.getException()==null) {
        buffer.append("OK |");
      } else {
        buffer.append("exception...");
        buffer.append(NEWLINE);
        buffer.append(timer.getException());
        buffer.append(NEWLINE);
      }
      log.info(buffer.toString());
    }
  }

  private static String getParameter(String[] args, int index, String defaultValue) {
    String value = null;
    if ( (args!=null)
         && (args.length>index)
       ) {
      value = args[index];
    } else {
      value = defaultValue;
    }
    return value;
  }

  private static final Log log = LogFactory.getLog(SchedulerMain.class);
}
