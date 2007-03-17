package org.exoplatform.services.cms.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.actions.activation.BPActionActivationJob;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.WorkflowServiceContainer;

public class BPActionPlugin extends BaseActionPlugin implements ComponentPlugin {

  public static final String ACTION_TYPE = "exo:businessProcessAction";

  private WorkflowServiceContainer workflowServiceContainer_;
  private ActionConfig config_;
  private RepositoryService repositoryService_;

  public BPActionPlugin(RepositoryService repositoryService,InitParams params,
      WorkflowServiceContainer workflowServiceContainer) throws Exception {
    workflowServiceContainer_ = workflowServiceContainer;
    repositoryService_ = repositoryService;    
    config_ = (ActionConfig) params.getObjectParamValues(ActionConfig.class).get(0);    
  }

  public Collection<String> getActionExecutables() throws Exception {
    List<Process> processes = workflowServiceContainer_.getProcesses();
    Collection<String> businessProcesses = new ArrayList<String>();    
    for (Iterator<Process> iter = processes.listIterator(); iter.hasNext();) {
      Process process =  iter.next();
      businessProcesses.add(process.getName());
    }
    return businessProcesses;
  }

  public String getActionExecutableLabel() {
    return "Business Processes:";
  }
  
  protected ECMEventListener createEventListener(String actionName, String moveExecutable, String srcWorkspace, 
      String srcPath, Map variables) throws Exception {
    return new BPActionLauncherListener(actionName, moveExecutable, srcWorkspace, srcPath, variables);
  }  

  public String getExecutableDefinitionName() {
    return "exo:businessProcess";
  }
   
  protected String getWorkspace() {
    return config_.getWorkspace();
  }

  protected ManageableRepository getRepository() throws Exception {
    return repositoryService_.getRepository();
  }

  protected String getActionType() {
    return ACTION_TYPE;
  }

  protected List getActions() {
    return config_.getActions();
  }

  public String getName() {    
    return "exo:businessProcessAction";
  }

  public void setName(String s) {    
  }

  public String getDescription() {
    return "Add an action service";
  }

  public void setDescription(String desc) {
  }  
  
  public void executeAction(String userId, Node actionNode, Map variables) throws Exception {
    String businessProcess = actionNode.getProperty("exo:businessProcess").getString();    
    executeAction(userId, businessProcess, variables);
  }
  
  public void executeAction(String userId, String executable, Map variables) {
    PortalContainer pContainer = PortalContainer.getInstance();    
    WorkflowServiceContainer workflowSContainer = (WorkflowServiceContainer) pContainer
    .getComponentInstanceOfType(WorkflowServiceContainer.class);     
    workflowSContainer.startProcessFromName(userId, executable, variables);    
  }    
  
  public class BPActionLauncherListener extends BaseActionLauncherListener {
    
    public BPActionLauncherListener(String actionName, String businessProcess, String srcWorkspace, 
        String srcPath, Map actionVariables) throws Exception {
      super(actionName, businessProcess, srcWorkspace, srcPath, actionVariables);
    }    
    
    public void triggerAction(String userId, Map variables) {
      executeAction(userId, super.executable_, variables);
    }  

  }

  public void activateAction(String userId, String executable, Map variables) throws Exception {
    executeAction(userId,executable,variables) ;
    
  }

  protected Class createActivationJob() throws Exception {
    return BPActionActivationJob.class ;
  } 
  
}
