package org.exoplatform.services.cms.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.drools.WorkingMemory;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.actions.activation.RuleActionActivationJob;
import org.exoplatform.services.cms.rules.RuleService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class RuleActionPlugin extends BaseActionPlugin implements ComponentPlugin{
  
  public static final String ACTION_TYPE = "exo:ruleAction";
    
  private RepositoryService repositoryService_;  
  private ActionConfig config_;
  private RuleService ruleService_;

  public RuleActionPlugin(RuleService ruleService,InitParams params,
                          RepositoryService repositoryService) throws Exception {
    ruleService_ = ruleService;
    repositoryService_ = repositoryService;
    
    config_ = (ActionConfig) params.getObjectParamValues(ActionConfig.class).get(0);    
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

  public String getExecutableDefinitionName() {
    return "exo:rule";
  } 
  
  protected ECMEventListener createEventListener(String actionName, String actionExecutable, String srcWorkspace, 
      String srcPath, Map variables) throws Exception {
    return new RuleActionLauncherListener(actionName, actionExecutable, srcWorkspace, srcPath, variables);
  }

  public Collection<String> getActionExecutables() throws Exception {
    Collection<String> ruleNames = new ArrayList<String>();
    NodeIterator iter = ruleService_.getRules();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      ruleNames.add(node.getName());
    }
    return ruleNames;
  }

  public String getActionExecutableLabel() {
    return "Defined Rules: ";
  }

  public String getName() {    
    return ACTION_TYPE;
  }

  public void setName(String s) {    
  }

  public String getDescription() {
    return "Add a action service";
  }

  public void setDescription(String desc) {
  }    
  
  public void executeAction(String userId, Node actionNode, Map variables) throws Exception {
    String rule = actionNode.getProperty("exo:rule").getString();    
    executeAction(userId, rule, variables);
  }  
  
  public void executeAction(String userId, String executable, Map variables) throws Exception {
    PortalContainer pContainer = PortalContainer.getInstance();    
    RuleService ruleService = (RuleService) pContainer.getComponentInstanceOfType(RuleService.class);    
    WorkingMemory workingMemory = ruleService.getRule(executable);
    workingMemory.assertObject(variables);
    workingMemory.fireAllRules();
  }    
  
  public class RuleActionLauncherListener extends BaseActionLauncherListener {

    public RuleActionLauncherListener(String actionName, String rule, String srcWorkspace,
        String srcPath, Map actionVariables) throws Exception {
      super(actionName, rule, srcWorkspace, srcPath, actionVariables);
    }

    public void triggerAction(String userId, Map variables) throws Exception {
      executeAction(userId, super.executable_, variables);      
    }
    
  }

  public void activateAction(String userId, String executable, Map variables) throws Exception {  
    executeAction(userId,executable,variables) ;
  }

  protected Class createActivationJob() throws Exception {   
    return RuleActionActivationJob.class ;
  }  
  
}
