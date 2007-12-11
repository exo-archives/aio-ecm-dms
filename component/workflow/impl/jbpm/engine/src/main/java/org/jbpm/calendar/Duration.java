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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * interpretes textual descriptions of a duration.
 * <p>Syntax: &lt;quantity&gt; [business] &lt;unit&gt; <br />
 * Where 
 * <ul>
 *   <li>&lt;quantity&gt; is a piece of text that is parsable 
 *       with <code>Double.parseDouble(quantity)</code>.
 *   </li>
 *   <li>&lt;unit&gt; is one of {second, seconds, minute, minutes, 
 *       hour, hours, day, days, week, weeks, month, months, year, years}.
 *   </li>
 *   <li>And adding the optional indication <code>business</code> means that 
 *       only business hours should be taken into account for this duration.
 *   </li>
 * </ul>
 * </p> 
 */
public class Duration implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private static final long SECOND = 1000; 
  private static final long MINUTE = 60*SECOND; 
  private static final long HOUR = 60*MINUTE; 
  private static final long DAY = 24*HOUR; 
  private static final long WEEK = 7*DAY; 
  private static final long MONTH = 30*DAY;
  private static final long YEAR = 365*DAY;

  private static long BUSINESS_DAY = 0; 
  private static long BUSINESS_WEEK = 0; 
  private static long BUSINESS_MONTH = 0;
  private static long BUSINESS_YEAR = 0;
  
  static {
    Properties businessCalendarProperties = BusinessCalendar.getBusinessCalendarProperties();
    String businessDayText = businessCalendarProperties.getProperty("business.day.expressed.in.hours");
    String businessWeekText = businessCalendarProperties.getProperty("business.week.expressed.in.hours");
    String businessMonthText = businessCalendarProperties.getProperty("business.month.expressed.in.business.days");
    String businessYearText = businessCalendarProperties.getProperty("business.year.expressed.in.business.days");
    
    BUSINESS_DAY = (long) (Double.parseDouble(businessDayText) * HOUR);
    BUSINESS_WEEK = (long) (Double.parseDouble(businessWeekText) * HOUR);
    BUSINESS_MONTH = (long) (Double.parseDouble(businessMonthText) * BUSINESS_DAY);
    BUSINESS_YEAR = (long) (Double.parseDouble(businessYearText) * BUSINESS_DAY);
  }
  
  private static Map units = null;
  static {
    units = new HashMap();
    units.put("second", new Long(SECOND));
    units.put("seconds", new Long(SECOND));
    units.put("business second", new Long(SECOND));
    units.put("business seconds", new Long(SECOND));
    units.put("minute", new Long(MINUTE));
    units.put("minutes", new Long(MINUTE));
    units.put("business minute", new Long(MINUTE));
    units.put("business minutes", new Long(MINUTE));
    units.put("hour", new Long(HOUR));
    units.put("hours", new Long(HOUR));
    units.put("business hour", new Long(HOUR));
    units.put("business hours", new Long(HOUR));
    units.put("day", new Long(DAY));
    units.put("days", new Long(DAY));
    units.put("business day", new Long(BUSINESS_DAY));
    units.put("business days", new Long(BUSINESS_DAY));
    units.put("week", new Long(WEEK));
    units.put("weeks", new Long(WEEK));
    units.put("business week", new Long(BUSINESS_WEEK));
    units.put("business weeks", new Long(BUSINESS_WEEK));
    units.put("month", new Long(MONTH));
    units.put("months", new Long(MONTH));
    units.put("business month", new Long(BUSINESS_MONTH));
    units.put("business months", new Long(BUSINESS_MONTH));
    units.put("year", new Long(YEAR));
    units.put("years", new Long(YEAR));
    units.put("business year", new Long(BUSINESS_YEAR));
    units.put("business years", new Long(BUSINESS_YEAR));
  }
  
  long milliseconds = 0;
  boolean isBusinessTime = false;
  
  Duration() {
  }

  public Duration(long milliseconds) {
    this.milliseconds = milliseconds;
  }

  public Duration(Duration duration) {
    this.milliseconds = duration.milliseconds;
    this.isBusinessTime = duration.isBusinessTime;
  }

  /**
   * creates a duration from a textual description.
   * syntax: {number} space {unit}
   * where number is parsable to a java.lang.Number and 
   * unit is one of
   * <ul>
   *   <li>second</li>
   *   <li>seconds</li>
   *   <li>minute</li>
   *   <li>minutes</li>
   *   <li>hour</li>
   *   <li>hours</li>
   *   <li>day</li>
   *   <li>days</li>
   *   <li>week</li>
   *   <li>weeks</li>
   *   <li>month (30 days)</li>
   *   <li>months (30 days)</li>
   *   <li>year (355 days)</li>
   *   <li>years (355 days)</li>
   * </ul>
   */
  public Duration(String duration) {
    if (duration==null) throw new NullPointerException("duration is null");
    int separatorIndex = duration.indexOf(' ');
    if (separatorIndex==-1) throw new IllegalArgumentException("improper format of duration '"+duration+"'");
    String quantityText = duration.substring(0, separatorIndex).trim().toLowerCase(); 
    String unitText = duration.substring(separatorIndex+1).trim().toLowerCase();
    if (unitText.startsWith("business")) {
      isBusinessTime = true;
   }
    double amount = Double.parseDouble(quantityText); 
    Long unit = (Long) units.get(unitText);
    this.milliseconds = (long) (amount * unit.longValue());
  }
}
