/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.icalendar.ICalendarService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

public class ICalendarScript implements CmsScript {
  
  private ICalendarService icalService_;
  
  public ICalendarScript(ICalendarService icalService) {
		icalService_ = icalService;
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;
    
    println("***  INTERNET CALENDAR AND SCHEDULES BUILDING...   ***");    
					
		icalService_.generateICalendar(variables);

		println("***  BUILD SUCCESSFULL  ***");  
          
  }

  public void setParams(String[] params) {}

}
