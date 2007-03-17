package org.jbpm.calendar;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * is a day on a business calendar.
 */
public class Day implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  DayPart[] dayParts = null;
  BusinessCalendar businessCalendar = null;

  public static Day[] parseWeekDays(Properties calendarProperties, BusinessCalendar businessCalendar) {
    DateFormat dateFormat = new SimpleDateFormat(calendarProperties.getProperty("hour.format"));
    Day[] weekDays = new Day[8];
    weekDays[Calendar.MONDAY] = new Day( calendarProperties.getProperty("weekday.monday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.TUESDAY] = new Day( calendarProperties.getProperty("weekday.thuesday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.WEDNESDAY] = new Day( calendarProperties.getProperty("weekday.wednesday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.THURSDAY] = new Day( calendarProperties.getProperty("weekday.thursday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.FRIDAY] = new Day( calendarProperties.getProperty("weekday.friday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.SATURDAY] = new Day( calendarProperties.getProperty("weekday.saturday"), dateFormat, businessCalendar ); 
    weekDays[Calendar.SUNDAY] = new Day( calendarProperties.getProperty("weekday.sunday"), dateFormat, businessCalendar ); 
    return weekDays;
  }

  public Day(String dayPartsText, DateFormat dateFormat, BusinessCalendar businessCalendar) {
    this.businessCalendar = businessCalendar;
    
    List dayPartsList = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(dayPartsText, "&");
    while (tokenizer.hasMoreTokens()) {
      String dayPartText = tokenizer.nextToken().trim();
      dayPartsList.add(new DayPart(dayPartText, dateFormat, this, dayPartsList.size()));
    }
    
    dayParts = (DayPart[]) dayPartsList.toArray(new DayPart[dayPartsList.size()]); 
  }

  public void findNextDayPartStart(int dayPartIndex, Date date, Object[] result) {
    // if there is a day part in this day that starts after the given date
    if (dayPartIndex < dayParts.length) {
      if (dayParts[dayPartIndex].isStartAfter(date)) {
        result[0] = dayParts[dayPartIndex].getStartTime(date);
        result[1] = dayParts[dayPartIndex];
      } else {
        findNextDayPartStart(dayPartIndex+1, date, result);
      }
    } else {
      // descend recustively
      date = businessCalendar.findStartOfNextDay(date);
      Day nextDay = businessCalendar.findDay(date);
      nextDay.findNextDayPartStart(0, date, result);
    }
  }
}
