/*
 * Created on Feb 21, 2005
 */
package org.exoplatform.services.workflow.impl.jbpm;

import java.util.Date;

import org.jbpm.graph.exe.ProcessInstance;



/**
 * @author benjaminmestrallet
 */
public class ProcessInstanceData implements org.exoplatform.services.workflow.ProcessInstance{

  public static final String STARTED = "started";
  public static final String FINISHED = "finished";  
  
  private String state = STARTED;
  private ProcessInstance processInstance;  
  
  public ProcessInstanceData(ProcessInstance processInstance) {
    this.processInstance = processInstance;         
  }
  public Date getEndDate() {
    return processInstance.getEnd();
  }  

  public String getProcessId() {
    return ""+processInstance.getProcessDefinition().getId();
  }  

  public String getProcessInstanceId() {
    return ""+processInstance.getId();
  }

  public Date getStartDate() {
    return processInstance.getStart();
  }
  
  public String getProcessName() {
    return processInstance.getProcessDefinition().getName();
  }  
  
}
