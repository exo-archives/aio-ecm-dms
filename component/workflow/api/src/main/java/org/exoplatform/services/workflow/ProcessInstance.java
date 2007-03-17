package org.exoplatform.services.workflow;

import java.util.Date;

public interface ProcessInstance {

  public String getProcessInstanceId();
  public String getProcessId();
  public String getProcessName();
  public Date getStartDate();
  public Date getEndDate();
  
}
