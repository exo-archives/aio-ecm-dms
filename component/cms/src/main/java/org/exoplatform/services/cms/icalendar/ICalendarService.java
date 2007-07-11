package org.exoplatform.services.cms.icalendar;

import java.util.Map;

/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface ICalendarService {
  
	public void generateICalendar(Map content) throws Exception;
  
  public Object generateICalendar(String category) throws Exception;

}
