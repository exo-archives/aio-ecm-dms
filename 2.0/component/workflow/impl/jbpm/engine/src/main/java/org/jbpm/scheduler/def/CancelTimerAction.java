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

import org.dom4j.Element;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;

public class CancelTimerAction extends Action {

  private static final long serialVersionUID = 1L;
  
  String timerName = null;
  
  public void read(Element actionElement, JpdlXmlReader jpdlReader) {
    timerName = actionElement.attributeValue("name");
    if (timerName==null) {
      jpdlReader.addWarning("no 'name' specified in CancelTimerAction '"+actionElement.asXML()+"'");
    }
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    executionContext.getSchedulerInstance().cancel(timerName, executionContext.getToken());
  }
  
  public String getTimerName() {
    return timerName;
  }
  public void setTimerName(String timerName) {
    this.timerName = timerName;
  }
}
