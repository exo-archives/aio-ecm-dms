package org.exoplatform.services.cms.actions.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.actions.activation.ScriptActionActivationJob;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class ScriptActionPlugin extends BaseActionPlugin implements ComponentPlugin {
  
  public static final String ACTION_TYPE = "exo:scriptAction";
  
  private ScriptService scriptService_;
  private RepositoryService repositoryService_;
  private ActionConfig config_;
  
  public ScriptActionPlugin(ScriptService scriptService, InitParams params,
                            RepositoryService repositoryService) throws Exception {
    scriptService_ = scriptService;
    repositoryService_ = repositoryService;
    config_ = (ActionConfig) params.getObjectParamValues(ActionConfig.class).get(0);
  }
  
  public Collection<String> getActionExecutables(String repository) throws Exception {
    Collection<String> actionScriptNames = new ArrayList<String>();
    List<Node> actionScriptList = scriptService_.getECMActionScripts(repository) ;
    String baseScriptPath = scriptService_.getBaseScriptPath() ;
    for(Node script:actionScriptList) {
      String actionScriptName = StringUtils.substringAfter(script.getPath(),baseScriptPath + "/") ;
      actionScriptNames.add(actionScriptName) ;
    }
    return actionScriptNames;
  }
  
  public String getActionExecutableLabel() { return "Groovy Scripts:"; }
  
  public String getExecutableDefinitionName() { return "exo:script"; }
  protected List<RepositoryEntry> getRepositories() {
    return repositoryService_.getConfig().getRepositoryConfigurations() ;
  }
  protected String getWorkspaceName() { return config_.getWorkspace(); }
  protected String getRepositoryName() {return config_.getRepository() ; }
  protected ManageableRepository getRepository(String repository) throws Exception {
    return repositoryService_.getRepository(repository);
  }
  protected String getActionType() {  return ACTION_TYPE;  }
  protected List getActions() { return config_.getActions(); }
  
  protected ECMEventListener createEventListener(String actionName, String actionExecutable,
      String repository, String srcWorkspace, String srcPath, Map variables) throws Exception {
    return new ScriptActionLauncherListener(actionName, actionExecutable, repository, srcWorkspace,
        srcPath, variables);
  }
  
  public String getName() { return ACTION_TYPE; }  
  public void setName(String s) { }
  
  public String getDescription() { return "Add a action service"; }  
  public void setDescription(String desc) { }
  
  public void executeAction(String userId, Node actionNode, Map variables, String repository) throws Exception {
    String script = actionNode.getProperty("exo:script").getString();    
    variables.put("actionNode", actionNode);
    variables.put("repository",repository) ;
    executeAction(userId, script, variables, repository);
  }      
  
  public void executeAction(String userId, String executable, Map variables, String repository) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ScriptService scriptService =  (ScriptService)container.getComponentInstanceOfType(ScriptService.class);
    CmsScript cmsScript = scriptService.getScript(executable, repository);
    cmsScript.execute(variables);
  }
  
  public class ScriptActionLauncherListener extends BaseActionLauncherListener {
    
    public ScriptActionLauncherListener(String actionName, String script, String repository, String srcWorkspace,
        String srcPath, Map actionVariables) throws Exception {
      super(actionName, script, repository, srcWorkspace, srcPath, actionVariables);
    }
    
    public void triggerAction(String userId, Map variables, String repository) throws Exception {
      executeAction(userId, super.executable_, variables, repository);
    }
  }

  public void activateAction(String userId, String executable, Map variables, String repository) throws Exception {
    variables.put("repository",repository) ;
    executeAction(userId,executable,variables, repository) ;
  }

  protected Class createActivationJob() throws Exception {
    return ScriptActionActivationJob.class ;
  }  
  
}
