/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
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
 * Mar 16, 2006
 */
public class ContentValidationPublicationHook implements NodeHookI {

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
          ContentValidationWaitSwitchHook.CONTAINER_PROPERTY_NAME).
          getTheValue();
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
        projectSession.getProperty("repository").getTheValue();
      Date startDate = new Date (Long.parseLong(projectSession.
        getNodeProperty(node.getName(), "startDate").getTheValue()));
      Date endDate = new Date (Long.parseLong(projectSession.
        getNodeProperty(node.getName(), "endDate").getTheValue()));
    
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
      Node actionableNode = (Node) session.getItem(srcPath);
      if(!actionableNode.isNodeType("exo:actionable")) {
        actionableNode = (Node) session.getItem(nodePath);
      }
      Node actionNode = actionServiceContainer.getAction(actionableNode, actionName);
      String destWorkspace = actionNode.getProperty(
        "exo:destWorkspace").getString();
      String destPath = actionNode.getProperty("exo:destPath").getString();
      
      // Add a Mixin to the document and set publication properties
      Node srcNode = (Node) session.getItem(nodePath);
      srcNode.addMixin("exo:published");      
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(startDate);
      srcNode.setProperty("exo:startPublication", calendar);
      if (endDate != null) {
        calendar = new GregorianCalendar();
        calendar.setTime(endDate);
        srcNode.setProperty("exo:endPublication", calendar);
      }
      srcNode.save();

      // Move the Node to the target Workspace
      String relPath = nodePath.substring(srcPath.length() + 1); 
      if(!relPath.startsWith("/"))
        relPath = "/" + relPath;
      relPath = relPath.replaceAll("\\[\\d*\\]", "");
      cmsService.moveNode(nodePath,srcWorkspace,destWorkspace,destPath + relPath,repository );
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
