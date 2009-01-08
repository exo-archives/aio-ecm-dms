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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.workflow.impl.bonita.WorkflowServiceContainerImpl;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook creates Workflow Instance Properties based on values put in
 * the Thread Local. As most Activities in the Process are automatic they are
 * called prior the Properties can be set. Hence this Role Mapper finds them in
 * a Thread Local set by the method that starts the Instance in the Bonita
 * service.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 22, 2006
 */
public class ContentBackupCreatePropertiesHook implements NodeHookI {

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
      // Initialize Project Session
      String projectName = node.getBnProject().getName();
      ProjectSessionLocalHome projectSessionHome =
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      projectSession.initProject(projectName);

      // Get Properties from the Thread Local and inject them in the Instance
      Map<String,Object> variables =
        WorkflowServiceContainerImpl.InitialVariables.get();
      for(String variable : variables.keySet()) {
        // All Properties are assumed to be Strings
        projectSession.setProperty(variable, (String) variables.get(variable));
      }
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
