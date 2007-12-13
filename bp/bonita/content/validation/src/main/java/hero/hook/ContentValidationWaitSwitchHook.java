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

import hero.interfaces.BnNodeLocal;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.exoplatform.container.PortalContainer;

/**
 * This Node Hook changes Instance Attributes to indicate the Workflow Engine
 * whether the document should be published immediately or not.
 * If a wait is schedulded, then a Deadline is set accordingly in the
 * corresponding subsequent Activity.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 13, 2006
 */
public class ContentValidationWaitSwitchHook implements NodeHookI {

  /** Name of the Property that contains the current Portal Container */
  public static final String CONTAINER_PROPERTY_NAME = "containername";
  
  /** Name of the Property that contains the publication date */
  public static final String DATE_PROPERTY_NAME = "startDate";
  
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

      // Determine the amount of time to wait
      long relativeDeadline = Long.parseLong(projectSession.getNodeProperty(
        nodeName, ContentValidationWaitSwitchHook.DATE_PROPERTY_NAME).
        getTheValue()) - new Date().getTime();
      
      if(relativeDeadline > 0) {
        // The processing should go to the wait Activity
        projectSession.setNodeProperty(nodeName,
                                       WAIT_PROPERTY_NAME,
                                       "true",
                                       false);
        
        // Modifiy the Deadline of the Wait Activity
        Collection<Long> deadlines = new ArrayList<Long>();
        deadlines.add(relativeDeadline);
        projectSession.setNodeRelativeDeadlines(WAIT_ACTIVITY_NAME, deadlines);
        
        /*
         * Cache the name of the Portal Container so that it can be retrieved
         * from the Publication Hook. Indeed, the latter may be fired by an EJB
         * Thread in case a wait is to be performed. EJB Threads do not have
         * the eXo Portal Container attached to their context and this forces to
         * retrieve it by name.
         */
        projectSession.setProperty(
          ContentValidationWaitSwitchHook.CONTAINER_PROPERTY_NAME,
          PortalContainer.getInstance().getPortalContainerInfo().
            getContainerName());
      }
      else {
        // The processing should directly go to the publication Activity
        projectSession.setNodeProperty(nodeName,
                                       WAIT_PROPERTY_NAME,
                                       "false",
                                       false);
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
