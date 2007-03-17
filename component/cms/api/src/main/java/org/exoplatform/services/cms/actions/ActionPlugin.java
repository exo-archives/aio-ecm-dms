package org.exoplatform.services.cms.actions;

import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;

public interface ActionPlugin {
  
  public boolean isActionTypeSupported(String actionType);

  public String getExecutableDefinitionName();
  
  public Collection<String> getActionExecutables() throws Exception;

  public String getActionExecutableLabel();
  
  public String getActionExecutable(String actionTypeName) throws Exception;

  public boolean isVariable(String variable) throws Exception;
  
  public Collection<String> getVariableNames(String actionTypeName) throws Exception;
  
  public void removeObservation(String moveName) throws Exception;
  
  public void removeActivationJob(String jobName,String jobGroup,String jobClass) throws Exception ;
  
  public void addAction(String actionType, String srcWorkspace, String srcPath, Map mappings) throws Exception;
  
  public void initiateActionObservation(Node actionNode) throws Exception ;
  
  public void reScheduleActivations(Node actionNode) throws Exception ;
  
  public void executeAction(String userId, Node actionNode, Map variables) throws Exception;
  
  public void executeAction(String userId, String executable, Map variables) throws Exception;
  
  public void activateAction(String userId, String executable, Map variables) throws Exception;
  
}
