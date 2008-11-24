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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * a calendar that knows about business hours.
 */
public class BusinessCalendar implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  Day[] weekDays = null;
  List holidays = null;

  private static Properties businessCalendarProperties = null;
  public static Properties getBusinessCalendarProperties() {
    if (businessCalendarProperties==null) {
      businessCalendarProperties = ClassLoaderUtil.getProperties("jbpm.business.calendar.properties", "org/jbpm/calendar");
    }
    return businessCalendarProperties;
  }

  public BusinessCalendar() {
    try {
      Properties calendarProperties = getBusinessCalendarProperties();
      weekDays = Day.parseWeekDays(calendarProperties, this);
      holidays = Holiday.parseHolidays(calendarProperties, this);

    } catch (Exception e) {
      throw new RuntimeException("couldn't create business calendar", e);
    }
  }

  public Date add(Date date, Duration duration) {
    Date end = null;
    if (duration.isBusinessTime) {
      DayPart dayPart = findDayPart(date);
      boolean isInbusinessHours = (dayPart!=null);
      if (! isInbusinessHours) {
        Object[] result = new Object[2];
        findDay(date).findNextDayPartStart(0, date, result);
        date = (Date) result[0];
        dayPart = (DayPart) result[1];
      }
      end = dayPart.add(date, duration);
    } else {
      end = new Date(date.getTime()+duration.milliseconds);
    }
    return end;
  }

  public Date findStartOfNextDay(Date date) {
    Calendar calendar = getCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    while(isHoliday(date)) {
      calendar.setTime(date);
      calendar.add(Calendar.DATE, 1);
      date = calendar.getTime();
    }
    return date;
  }

  public Day findDay(Date date) {
    Calendar calendar = getCalendar();
    calendar.setTime(date);
    return weekDays[calendar.get(Calendar.DAY_OF_WEEK)];
  }

  public boolean isHoliday(Date date) {
    Iterator iter = holidays.iterator();
    while (iter.hasNext()) {
      Holiday holiday = (Holiday) iter.next();
      if (holiday.includes(date)) {
        return true;
      }
    }
    return false;
  }

  DayPart findDayPart(Date date) {
    DayPart dayPart = null;
    if (! isHoliday(date)) {
      Day day = findDay(date);
      for (int i=0; ((i < day.dayParts.length)
                     && (dayPart==null)); i++) {
        DayPart candidate = day.dayParts[i];
        if (candidate.includes(date)) {
          dayPart = candidate;
        }
      }
    }
    return dayPart;
  }

  public DayPart findNextDayPart(Date date) { 
    DayPart nextDayPart = null; 
    while(nextDayPart==null) { 
      nextDayPart = findDayPart(date); 
      if (nextDayPart==null) { 
        date = findStartOfNextDay(date); 
        Object result[] = new Object[2]; 
        Day day = findDay(date); 
        day.findNextDayPartStart(0, date, result); 
        nextDayPart = (DayPart) result[1]; 
      } 
    } 
    return nextDayPart; 
  } 

  public boolean isInBusinessHours(Date date) { 
    return (findDayPart(date)!=null); 
  } 

  public static Calendar getCalendar() {
    return new GregorianCalendar();
  }
}
