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
package org.exoplatform.services.cms.actions.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class BPActionPlugin extends BaseActionPlugin implements ComponentPlugin {

  public static final String ACTION_TYPE = "exo:businessProcessAction";

  @Override
  protected Class createActivationJob() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ECMEventListener createEventListener(String actionName, String actionExecutable, String repository, String srcWorkspace, String srcPath, Map variables) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getActionType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List getActions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List<RepositoryEntry> getRepositories() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ManageableRepository getRepository(String repositoryName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getWorkspaceName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDescription(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setName(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void activateAction(String userId, String executable, Map variables, String repository) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void executeAction(String userId, Node actionNode, Map variables, String repository) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void executeAction(String userId, String executable, Map variables, String repository) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public String getActionExecutableLabel() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<String> getActionExecutables(String repository) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getExecutableDefinitionName() {
    // TODO Auto-generated method stub
    return null;
  }

  /*private WorkflowServiceContainer workflowServiceContainer_;
  private ActionConfig config_;
  private RepositoryService repositoryService_;

  public BPActionPlugin(RepositoryService repositoryService,InitParams params,
      WorkflowServiceContainer workflowServiceContainer) throws Exception {
    workflowServiceContainer_ = workflowServiceContainer;
    repositoryService_ = repositoryService;    
    config_ = (ActionConfig) params.getObjectParamValues(ActionConfig.class).get(0);    
  }

  public Collection<String> getActionExecutables(String repository) throws Exception {
    List<Process> processes = workflowServiceContainer_.getProcesses();
    Collection<String> businessProcesses = new ArrayList<String>();    
    for (Iterator<Process> iter = processes.listIterator(); iter.hasNext();) {
      Process process =  iter.next();
      businessProcesses.add(process.getName());
    }
    return businessProcesses;
  }
    
  protected ECMEventListener createEventListener(String actionName, String moveExecutable, 
      String repository, String srcWorkspace, String srcPath, Map variables) throws Exception {
    return new BPActionLauncherListener(actionName, moveExecutable, repository, 
                                        srcWorkspace, srcPath, variables);
  }  

  public String getActionExecutableLabel() { return "Business Processes:"; }
  public String getExecutableDefinitionName() { return "exo:businessProcess"; }
    
  protected String getRepositoryName() { return config_.getRepository() ; }
  protected String getWorkspaceName() { return config_.getWorkspace() ; }
  
  protected List<RepositoryEntry> getRepositories() {
    return repositoryService_.getConfig().getRepositoryConfigurations() ;
  }
  protected ManageableRepository getRepository(String repository) throws Exception {
    return repositoryService_.getRepository(repository);
  }
  
  protected String getActionType() { return ACTION_TYPE ; }
  protected List getActions() { return config_.getActions() ; }
  public String getName() { return "exo:businessProcessAction" ; }
  public void setName(String s) {}
  public String getDescription() { return "Add an action service" ; }
  public void setDescription(String desc) {}  
  
  @SuppressWarnings("unchecked")
  public void executeAction(String userId, Node actionNode, Map variables, String repository) throws Exception {
    String businessProcess = actionNode.getProperty("exo:businessProcess").getString();
    //TODO check maybe don't need put repository here
    variables.put("repository",repository) ;
    executeAction(userId, businessProcess, variables, repository);
  }
  
  public void executeAction(String userId, String executable, Map variables, String repository) {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    WorkflowServiceContainer workflowSContainer = (WorkflowServiceContainer) container
    .getComponentInstanceOfType(WorkflowServiceContainer.class);     
    workflowSContainer.startProcessFromName(userId, executable, variables);    
  }    
  
  public class BPActionLauncherListener extends BaseActionLauncherListener {
    
    public BPActionLauncherListener(String actionName, String businessProcess, String repository,
        String srcWorkspace, String srcPath, Map actionVariables) throws Exception {
      super(actionName, businessProcess, repository, srcWorkspace, srcPath, actionVariables);
    }    
    
    public void triggerAction(String userId, Map variables, String repository) {
      executeAction(userId, super.executable_, variables, repository);
    }  

  }

  public void activateAction(String userId, String executable, Map variables, String repository) throws Exception {
    executeAction(userId, executable, variables, repository) ;
    
  }

  protected Class createActivationJob() throws Exception {
    return BPActionActivationJob.class ;
  }
  */
}
