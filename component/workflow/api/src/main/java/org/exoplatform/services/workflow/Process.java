package org.exoplatform.services.workflow;

public interface Process {
  public String getId();
  public String getName();
  public int getVersion();    
  public String getStartStateName();  
}
