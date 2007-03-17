/*
 * Created on Feb 22, 2005
 */
package org.exoplatform.services.workflow.impl.jbpm;

import java.util.Date;

import org.exoplatform.services.workflow.Task;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;


/**
 * @author benjaminmestrallet
 */
public class TaskData implements Task{
  
  public static final String STARTED = "started";
  public static final String FINISHED = "finished";

  private TaskInstance taskInstance;
  private String imageURL_;

  public TaskData(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
  }
  
  public String getId(){
    return "" + taskInstance.getId();
  }
  
  public String getTaskName(){
    return taskInstance.getName();
  }

  public String getActorId(){    
    String actorId = taskInstance.getActorId();
    if(actorId == null) {
      SwimlaneInstance swimlane = taskInstance.getSwimlaneInstance();
      if(swimlane != null)
        actorId = swimlane.getActorId();
    }
    if(actorId == null)
      actorId = "N/A";
    return actorId;
  }
    
  public String getSwimlane(){
    return taskInstance.getSwimlaneInstance().getName();
  }
  
  public Date getEnd(){
    return taskInstance.getEnd();
  }
  
  public String getProcessId() {
    return "" + taskInstance.getToken().getProcessInstance().getProcessDefinition().getId();
  }
  
  public String getProcessInstanceId() {
    return "" + taskInstance.getToken().getProcessInstance().getId();
  }
  
  public String getDescription() {
    return taskInstance.getTask().getDescription();
  }
  
}
