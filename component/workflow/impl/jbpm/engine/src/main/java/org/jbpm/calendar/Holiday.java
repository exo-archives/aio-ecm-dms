package org.jbpm.calendar;

import java.io.Serializable;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Properties;

/**
 * identifies a continuous set of days.
 */
public class Holiday implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  Date fromDay = null;
  Date toDay = null;
  BusinessCalendar businessCalendar = null;

  public static List parseHolidays(Properties calendarProperties, BusinessCalendar businessCalendar) {
    List holidays = new ArrayList();
    
    DateFormat dateFormat = new SimpleDateFormat(calendarProperties.getProperty("day.format"));
    Iterator iter = calendarProperties.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      if (key.startsWith("holiday")) {
        Holiday holiday = new Holiday(calendarProperties.getProperty(key), dateFormat, businessCalendar);
        holidays.add(holiday);
      }
    }

    return holidays;
  }

  public Holiday(String holidayText, DateFormat dateFormat, BusinessCalendar businessCalendar) {
    this.businessCalendar = businessCalendar;
    try {
      int separatorIndex = holidayText.indexOf('-');
      if (separatorIndex==-1) {
        fromDay = dateFormat.parse(holidayText.trim());
        toDay = fromDay;
      } else {
        String fromText = holidayText.substring(0, separatorIndex).trim();
        String toText = holidayText.substring(separatorIndex+1).trim();
        fromDay = dateFormat.parse(fromText);
        toDay = dateFormat.parse(toText);
      }
      // now we are going to set the toDay to the end of the day, rather then the beginning.
      // we take the start of the next day as the end of the toDay.
      Calendar calendar = BusinessCalendar.getCalendar();
      calendar.setTime(toDay);
      calendar.add(Calendar.DATE, 1);
      toDay = calendar.getTime();
      
    } catch (ParseException e) {
      throw new RuntimeException("couldn't parse holiday '"+holidayText+"'", e);
    }
  }

  public boolean includes(Date date) {
    return ( (fromDay.getTime()<=date.getTime())
             && (date.getTime()<toDay.getTime())
           );
  }
}
