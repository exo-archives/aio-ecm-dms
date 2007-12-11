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

package hero.hook;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.services.workflow.impl.bonita.WorkflowServiceContainerHelper;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook launches a recursive Subprocess when delegating.
 * Subprocess facilities in Bonita cannot be used as the Process to launch
 * is the same one. So it is decided to leverage the Workflow Service Container. 
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 20, 2006
 */
public class ContentValidationDelegateHook implements NodeHookI {

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#getMetadata()
   */
  public String getMetadata() {

    // Return Metadata information
    return Constants.Nd.BEFORETERMINATE;
  }

  public void beforeStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  public void afterStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#beforeTerminate(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void beforeTerminate(Object obj, BnNodeLocal node)
      throws HeroHookException {
    
    ProjectSessionLocal projectSession = null;
    
    try {
      // Instance name
      String instanceName = node.getBnProject().getName();
      
      // Initialize Project Session
      ProjectSessionLocalHome projectSessionHome =
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      projectSession.initProject(instanceName);
            
      // Retrieve the variables that will be used in the new Process Instance
      String srcWorkspace = projectSession.getProperty(
                              "srcWorkspace").getTheValue();
      String initiator    = projectSession.getProperty(
                              "initiator").getTheValue();
      String validator    = projectSession.getNodeProperty(
                              node.getName(), "delegate").getTheValue();
      String srcPath      = projectSession.getProperty(
                              "srcPath").getTheValue();
      String nodePath     = projectSession.getProperty(
                              "nodePath").getTheValue();
      String documentType = projectSession.getProperty(
                              "document-type").getTheValue();
      String actionName   = projectSession.getProperty(
                              "actionName").getTheValue();
      
      // Gather the variables
      Map<String, String> variables = new HashMap<String, String>();
      variables.put("srcWorkspace",  srcWorkspace);
      variables.put("initiator",     initiator);
      variables.put("exo:validator", validator);
      variables.put("srcPath",       srcPath);
      variables.put("nodePath",      nodePath);
      variables.put("document-type", documentType);
      variables.put("actionName",    actionName);
      
      // Retrieve reference to Workflow Service Container
      PortalContainer container = PortalContainer.getInstance();
      WorkflowServiceContainer workflowService = (WorkflowServiceContainer)
        container.getComponentInstanceOfType(WorkflowServiceContainer.class);

      // Spawn a new Process Instance
      workflowService.startProcessFromName(
        initiator,
        WorkflowServiceContainerHelper.getModelName(instanceName),
        variables);
    }
    catch(Exception e) {
      // TODO Use logging system instead
      e.printStackTrace();
    }
    finally {
      try {
        projectSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }

  public void afterTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  public void anticipate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  public void onCancel(Object arg0, BnNodeLocal arg1) throws HeroHookException {
    // TODO Auto-generated method stub

  }

  public void onDeadline(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  public void onReady(Object arg0, BnNodeLocal arg1) throws HeroHookException {
    // TODO Auto-generated method stub

  }

}
