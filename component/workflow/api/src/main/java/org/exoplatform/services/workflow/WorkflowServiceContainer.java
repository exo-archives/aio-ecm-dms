package org.exoplatform.services.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 28 juin 2004
 */
public interface WorkflowServiceContainer {

  public static final String ACTOR_ID_KEY_SEPARATOR = ":";
    
  public void deployProcess(InputStream iS) throws IOException;
  
  public List<Process> getProcesses();
  public Process getProcess(String processId);
  public boolean hasStartTask(String processId);
  
  public List<ProcessInstance> getProcessInstances(String processId);
  public ProcessInstance getProcessInstance(String processInstance);
  
  public Map getVariables(String processInstanceId, String taskId);
  
  public List<Task> getTasks(String processInstanceId);
  public Task getTask(String taskId);
  
  public List<Task> getAllTasks(String user) throws Exception;
  public List<Task> getUserTaskList(String user);
  public List<Task> getGroupTaskList(String user) throws Exception;
  
  public List<Timer> getTimers();
  
  public void startProcess(String processId);  
  public void startProcess(String remoteUser, String processId, Map variables);  
  public void startProcessFromName(String remoteUser, String processName, Map variables);
  public void endTask(String taskId, Map variables);
  public void endTask(String taskId, Map variables, String transition);
  
  public void deleteProcess(String processId);
  public void deleteProcessInstance(String processInstanceId);
}
