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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook moves the document into the destination Workspace
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 21, 2006
 */
public class ContentBackupBackupHook implements NodeHookI {

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#getMetadata()
   */
  public String getMetadata() {

    // Return Metadata information
    return Constants.Nd.BEFORETERMINATE;
  }

  public void beforeStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void afterStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#beforeTerminate(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void beforeTerminate(Object obj, BnNodeLocal node)
      throws HeroHookException {
    
    ProjectSessionLocal projectSession = null;
    boolean portalContainerSet = false;
    
    try {
      // Initialize Project Session
      ProjectSessionLocalHome projectSessionHome =
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      projectSession.initProject(node.getBnProject().getName());

      /*
       * This Hook may not have been invoked by an eXo Thread in case a
       * Deadline occured so it is needed to retrieve the Portal Container by
       * name.
       */
      if(PortalContainer.getInstance() == null) {
        String containerName = projectSession.getProperty(
            ContentBackupWaitSwitchHook.CONTAINER_PROPERTY_NAME).getTheValue();
        PortalContainer container = RootContainer.getInstance().
          getPortalContainer(containerName);
        PortalContainer.setInstance(container);
        portalContainerSet = true;
      }
      
      // Retrieve Workflow properties
      String actionName =
        projectSession.getProperty("actionName").getTheValue();
      String nodePath =
        projectSession.getProperty("nodePath").getTheValue();
      String srcPath =
        projectSession.getProperty("srcPath").getTheValue();
      String srcWorkspace =
        projectSession.getProperty("srcWorkspace").getTheValue();
     String repository = 
       projectSession.getProperty("repository").getTheValue() ;
      // Retrieve references to Services
      PortalContainer container = PortalContainer.getInstance();
      RepositoryService repositoryService = (RepositoryService)
        container.getComponentInstanceOfType(RepositoryService.class);
      ActionServiceContainer actionServiceContainer = (ActionServiceContainer)
        container.getComponentInstanceOfType(ActionServiceContainer.class);
      CmsService cmsService = (CmsService)
        container.getComponentInstanceOfType(CmsService.class);

      // Open a JCR session
      Session session = repositoryService.getRepository(repository).
        getSystemSession(srcWorkspace);
      
      // Retrieve information from the Action that triggered the Worflow
      Node actionnableNode = (Node) session.getItem(srcPath);
      Node actionNode = actionServiceContainer.getAction(actionnableNode, actionName);
      String destWorkspace = actionNode.getProperty("exo:destWorkspace")
        .getString();
      String destPath = actionNode.getProperty("exo:destPath").getString();
      
      // Move the Node to the target Workspace
      String relPath = nodePath.substring(srcPath.length() + 1);
      if (!relPath.startsWith("/"))
        relPath = "/" + relPath;
      relPath = relPath.replaceAll("\\[\\d*\\]", "");
      cmsService.moveNode(nodePath,
                          srcWorkspace,
                          destWorkspace,
                          destPath + relPath, repository);
      session.logout();
    }catch(Exception e) {
      // TODO Use logging system instead
      e.printStackTrace();
    }
    finally {
      try {
        
        if(portalContainerSet) {
          /*
           * If we are running in an EJB Thread then remove the reference to
           * the Portal Container we have set previously
           */
          PortalContainer.setInstance(null);
        }
        
        projectSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }

  public void afterTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void anticipate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void onCancel(Object arg0, BnNodeLocal arg1) throws HeroHookException {
  }

  public void onDeadline(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void onReady(Object arg0, BnNodeLocal arg1) throws HeroHookException {
  }
}
