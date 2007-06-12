/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package hero.hook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

import hero.interfaces.BnEdge;
import hero.interfaces.BnEdgeLocal;
import hero.interfaces.BnNodeHome;
import hero.interfaces.BnNodeLocal;
import hero.interfaces.BnNodeLocalHome;
import hero.interfaces.BnNodeUtil;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook changes Instance Attributes to indicate the Workflow Engine
 * whether the document should be backed up immediately or not.
 * If a wait is schedulded, then a Deadline is set accordingly in the
 * corresponding subsequent Activity.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 21, 2006
 */
public class ContentBackupWaitSwitchHook implements NodeHookI {

  /** Name of the Property that contains the current Portal Container */
  public static final String CONTAINER_PROPERTY_NAME = "containername";
  
  /** Name of the Property that contains the Node path */
  public static final String NODE_PATH_PROPERTY_NAME = "nodePath";

  /** Name of the Property that contains the source Workspace name */
  public static final String SRC_WORKSPACE_PROPERTY_NAME = "srcWorkspace";
  
  /** Name of the Property that contains the source repository name */
  public static final String REPOSITORY_PROPERTY_NAME = "repository";
  
  /** Name of the Wait Activity */
  public static final String WAIT_ACTIVITY_NAME = "wait";
  
  /**
   * Name of the Property that determines whether the Process should go into
   * the Wait Activity first.
   */
  public static final String WAIT_PROPERTY_NAME = "wait"; 
  

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

  public void afterStart(Object obj, BnNodeLocal node)
      throws HeroHookException {
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

      // Retrieve textual information
      String nodeName = node.getName();

      // Retrieve some Instance variables
      String nodePath = projectSession.getProperty(
        ContentBackupWaitSwitchHook.NODE_PATH_PROPERTY_NAME).getTheValue();
      String srcWorkspace = projectSession.getProperty(
        ContentBackupWaitSwitchHook.SRC_WORKSPACE_PROPERTY_NAME).getTheValue();
      String repository = projectSession.getProperty(
          ContentBackupWaitSwitchHook.REPOSITORY_PROPERTY_NAME).getTheValue();
      /*
       * Retrieve the Node from the JCR. The Portal Container Thread
       * Local is currently set as the current Thread is an eXo one.
       */
      PortalContainer container = PortalContainer.getInstance();
      RepositoryService repositoryService = (RepositoryService) container.
        getComponentInstanceOfType(RepositoryService.class);
      Session session = repositoryService.getRepository(repository).getSystemSession(
        srcWorkspace);
      Node srcNode = (Node) session.getItem(nodePath);
      
      if(!srcNode.isNodeType("exo:published")) {
        // The Node was previously published by the Workflow. Do nothing.
        projectSession.setNodeProperty(nodeName,
                                       WAIT_PROPERTY_NAME,
                                       "end",
                                       false);
      }
      else {
        // The Node was not previously published. Get the backup relative date.
        long relativeDeadline = 
          srcNode.getProperty("exo:endPublication").getDate().getTimeInMillis()
          - new Date().getTime();
        
        if(relativeDeadline > 0) {
          // The processing should go to the wait Activity
          projectSession.setNodeProperty(nodeName,
                                         WAIT_PROPERTY_NAME,
                                         "wait",
                                          false);
          
          // Modifiy the Deadline of the Wait Activity
          Collection<Long> deadlines = new ArrayList<Long>();
          deadlines.add(relativeDeadline);
          projectSession.setNodeRelativeDeadlines(WAIT_ACTIVITY_NAME,
                                                  deadlines);
          
          /*
           * Cache the name of the Portal Container so that it can be retrieved
           * from the Backup Hook. Indeed, the latter may be fired by an
           * EJB Thread in case a wait is to be performed. EJB Threads do not
           * have the eXo Portal Container attached to their context and this
           * forces to retrieve it by name.
           */
          projectSession.setProperty(
              ContentBackupWaitSwitchHook.CONTAINER_PROPERTY_NAME,
              PortalContainer.getInstance().getPortalContainerInfo().
                getContainerName());
        }
        else {
          // The processing should directly go to the backup Activity
          projectSession.setNodeProperty(nodeName,
                                         WAIT_PROPERTY_NAME,
                                         "backup",
                                          false);
        }
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
