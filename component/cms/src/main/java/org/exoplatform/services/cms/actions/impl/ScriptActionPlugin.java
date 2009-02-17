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
import org.exoplatform.services.jcr.ext.common.SessionProvider;

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
    SessionProvider provider = SessionProvider.createSystemProvider();
    List<Node> actionScriptList = scriptService_.getECMActionScripts(repository,provider) ;
    String baseScriptPath = scriptService_.getBaseScriptPath() ;
    for(Node script:actionScriptList) {
      String actionScriptName = StringUtils.substringAfter(script.getPath(),baseScriptPath + "/") ;
      actionScriptNames.add(actionScriptName) ;
    }
    provider.close();
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
  
  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  public void activateAction(String userId, String executable, Map variables, String repository) throws Exception {
    variables.put("repository",repository) ;
    executeAction(userId,executable,variables, repository) ;
  }

  protected Class createActivationJob() throws Exception {
    return ScriptActionActivationJob.class ;
  }  
  
}
