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
package org.jbpm.calendar;

import java.io.Serializable;
import java.text.*;
import java.text.DateFormat;
import java.util.*;

/**
 * is part of a day that can for example be used to represent business hours. 
 *
 */public class DayPart implements Serializable {

  private static final long serialVersionUID = 1L;
  
  int fromHour = -1;
  int fromMinute = -1;
  int toHour = -1;
  int toMinute = -1;
  Day day = null;
  int index = -1;

  public DayPart(String dayPartText, DateFormat dateFormat, Day day, int index) {
    this.day = day;
    this.index = index;
    
    int separatorIndex = dayPartText.indexOf('-');
    if (separatorIndex==-1) throw new IllegalArgumentException("improper format of daypart '"+dayPartText+"'");
    String fromText = dayPartText.substring(0, separatorIndex).trim().toLowerCase(); 
    String toText = dayPartText.substring(separatorIndex+1).trim().toLowerCase();
    
    try {
      Date from = dateFormat.parse(fromText);
      Date to = dateFormat.parse(toText);
      
      Calendar calendar = BusinessCalendar.getCalendar();
      calendar.setTime(from);
      fromHour = calendar.get(Calendar.HOUR_OF_DAY);
      fromMinute = calendar.get(Calendar.MINUTE);

      calendar.setTime(to);
      toHour = calendar.get(Calendar.HOUR_OF_DAY);
      toMinute = calendar.get(Calendar.MINUTE);

    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  public Date add(Date date, Duration duration) {
    Date end = null;
    
    Calendar calendar = BusinessCalendar.getCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);

    long dateMilliseconds = ((hour*60)+minute)*60*1000;
    long dayPartEndMilleseconds = ((toHour*60)+toMinute)*60*1000;
    long millisecondsInThisDayPart = dayPartEndMilleseconds - dateMilliseconds;
    
    if (duration.milliseconds <= millisecondsInThisDayPart) {
      end = new Date( date.getTime() + duration.milliseconds);
    } else {
      Duration remainder = new Duration(duration.milliseconds - millisecondsInThisDayPart);
      Date dayPartEndDate = new Date(date.getTime() + duration.milliseconds);
      
      Object[] result = new Object[2];
      day.findNextDayPartStart(index+1, dayPartEndDate, result);
      Date nextDayPartStart = (Date) result[0];
      DayPart nextDayPart = (DayPart) result[1];
      
      end = nextDayPart.add(nextDayPartStart, remainder);
    }
    
    return end;
  }
  
  public boolean isStartAfter(Date date) {
    Calendar calendar = BusinessCalendar.getCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    
    return ( (hour<fromHour)
             || ( (hour==fromHour)
                  && (minute<=fromMinute) 
                ) 
           );
  }


  public boolean includes(Date date) {
    Calendar calendar = BusinessCalendar.getCalendar();
    calendar.setTime(date);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    
    return ( ( (fromHour<hour)
               || ( (fromHour==hour)
                   && (fromMinute<=minute) 
                 )
             ) &&
             ( (hour<toHour)
               || ( (hour==toHour)
                    && (minute<=toMinute) 
                  )
             )
           );
  }

  public Date getStartTime(Date date) {
    Calendar calendar = BusinessCalendar.getCalendar();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, fromHour);
    calendar.set(Calendar.MINUTE, fromMinute);
    return calendar.getTime();
  }
}
