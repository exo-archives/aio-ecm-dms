package org.exoplatform.services.workflow;

import java.util.Date;

public interface Task {

  public String getActorId();

  public String getDescription();

  public String getId();

  public String getProcessId();
  
  public String getProcessInstanceId();

  public String getTaskName();

  public Date getEnd();
  
}