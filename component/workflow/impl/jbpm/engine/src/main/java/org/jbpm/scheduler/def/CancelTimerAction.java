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
