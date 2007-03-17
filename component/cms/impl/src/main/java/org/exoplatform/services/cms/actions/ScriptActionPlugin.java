package org.exoplatform.services.cms.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.actions.activation.ScriptActionActivationJob;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.jcr.RepositoryService;
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
  
  public Collection<String> getActionExecutables() throws Exception {
    Collection<String> actionScriptNames = new ArrayList<String>();
    List<Node> actionScriptList = scriptService_.getECMActionScripts() ;
    String baseScriptPath = scriptService_.getBaseScriptPath() ;
    for(Node script:actionScriptList) {
      String actionScriptName = StringUtils.substringAfter(script.getPath(),baseScriptPath + "/") ;
      actionScriptNames.add(actionScriptName) ;
    }
    return actionScriptNames;
  }
  
  public String getActionExecutableLabel() { return "Groovy Scripts:"; }
  
  public String getExecutableDefinitionName() { return "exo:script"; }
  
  protected String getWorkspace() { return config_.getWorkspace(); }
  
  protected ManageableRepository getRepository() throws Exception {
    return repositoryService_.getRepository();
  }
  
  protected String getActionType() {  return ACTION_TYPE;  }
  
  protected List getActions() { return config_.getActions(); }
  
  protected ECMEventListener createEventListener(String actionName, String actionExecutable,
      String srcWorkspace, String srcPath, Map variables) throws Exception {
    return new ScriptActionLauncherListener(actionName, actionExecutable, srcWorkspace,
        srcPath, variables);
  }
  
  public String getName() { return ACTION_TYPE; }  
  public void setName(String s) { }
  
  public String getDescription() { return "Add a action service"; }  
  public void setDescription(String desc) { }
  
  public void executeAction(String userId, Node actionNode, Map variables) throws Exception {
    String script = actionNode.getProperty("exo:script").getString();    
    variables.put("actionNode", actionNode);        
    executeAction(userId, script, variables);
  }      
  
  public void executeAction(String userId, String executable, Map variables) throws Exception {    
    ScriptService scriptService =  (ScriptService) PortalContainer.getComponent(ScriptService.class);
    CmsScript cmsScript = scriptService.getScript(executable);
    cmsScript.execute(variables);
  }
  
  public class ScriptActionLauncherListener extends BaseActionLauncherListener {
    
    public ScriptActionLauncherListener(String actionName, String script, String srcWorkspace,
        String srcPath, Map actionVariables) throws Exception {
      super(actionName, script, srcWorkspace, srcPath, actionVariables);
    }
    
    public void triggerAction(String userId, Map variables) throws Exception {
      executeAction(userId, super.executable_, variables);
    }
  }

  public void activateAction(String userId, String executable, Map variables) throws Exception {   
    executeAction(userId,executable,variables) ;
  }

  protected Class createActivationJob() throws Exception {
    return ScriptActionActivationJob.class ;
  } 
  
}
