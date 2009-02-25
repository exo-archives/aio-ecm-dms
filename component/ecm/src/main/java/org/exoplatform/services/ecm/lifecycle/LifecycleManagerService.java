/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.lifecycle;

import java.util.HashMap;
import java.util.Set;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 9, 2008  
 */
public interface LifecycleManagerService {
  /**
   * Add a Publication Plugin to the service.
   * The method caches all added plugins.
   * 
   * @param p the plugin to add
   */
  public void addPublicationPlugin(ContentLifecyclePlugin p) ;
  
  /**
   * Retrieves all added publication plugins.
   * This method is notably used to enumerate possible lifecycles.
   * 
   * @return the added publication plugins
   */
  public Set<ContentLifecyclePlugin> getPublicationPlugins() ;
  
  /**
   * Update the state of the specified node.
   * This method first inspects the publication mixin bound to the specified
   * Node. From that mixin, it retrieves the lifecycle registered with the
   * node. Finally, it delegates the call to the method with same name in the
   * plugin that implements the lifecycle.
   * 
   * @param node the Node whose state needs to be changed
   * @param newState the new state.
   * @param context a Hashmap containing contextual information needed
   * to change the state. The information set is defined on a State basis.
   * A typical example is information submitted by the user in a user
   * interface.
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   * @throws IncorrectStateUpdateLifecycleException if the update is not
   * allowed
   */
  public void changeState(Node node,
                          String newState,
                          HashMap<String, String> context)
  throws NotInLifecycleException,
  IncorrectStateUpdateLifecycleException; 

  /**
   * Retrieves the WebUI form corresponding to the current state of the
   * specified node.
   * The method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the lifecycle name from the mixin,
   * selects the appropriate publication plugin and delegates the call to it.
   * 
   * @param node the Node from which the state UI should be retrieved
   * @return a WebUI form corresponding to the current state and node.
   * @throws NotInPublicationLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
//  public UIForm getStateUI(Node node)
//    throws NotInPublicationLifecycleException ;
  
  /**
   * Retrieves an image showing the lifecycle state of the specified Node.
   * The method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the lifecycle name from the mixin,
   * selects the appropriate publication plugin and delegates the call to it.
   * 
   * @param node the node from which the image should be obtained
   * @return an array of bytes corresponding to the image to be shown to the
   * user
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public byte[] getStateImage(Node node)
    throws NotInLifecycleException ;
  
  /**
   * Retrieves the name of the publication state corresponding to the
   * specified Node.
   * This method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the current state name from the mixin.
   * Possible examples of State names are : "draft", "validation requested",
   * "publication pending", "published", "backed up", "validation refused".
   * 
   * @param node the node from which the publication state should be retrieved
   * @return a String giving the current state.
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public String getCurrentState(Node node)
    throws NotInLifecycleException ;

  /**
   * Retrieves description information explaining to the user the current
   * 
   * This method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the lifecycle name from the mixin,
   * selects the appropriate publication plugin and delegates the call to it.
   * 
   * @param node the Node from which user information should be retrieved
   * @return a text message describing the state of the current message.
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public String getUserInfo(Node node)
    throws NotInLifecycleException ;
  
  /**
   * Retrieves the history of publication changes made to the specified Node.
   *
   * This method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the lifecycle name from the mixin,
   * selects the appropriate publication plugin and delegates the call to it.
   * 
   * Log entries are specified as a multi-valued property of the publication
   * mixin.
   * 
   * @param node the Node from which the history Log should be retrieved
   * @return a String array with 2 dimensions. The first dimension contains
   * each log entry. The second dimension contains each information in a log
   * entry, which are : date, name of the new state, involved user, additional
   * information.
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public String[][] getLog(Node node)
    throws NotInLifecycleException ;

  /**
   * Adds a log entry to the specified Node.
   * The specified array of String defines the Log information to be added.
   * Log entries are specified as a multi-valued property of the publication
   * mixin.
   * 
   * @param node the Node from which the history Log should be updated
   * @param log the Log information to be added
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public void addLog(Node node, String[] log)
    throws NotInLifecycleException ;
  
  /**
   * Determines whether the specified Node has been enrolled into a
   * lifecycle.
   * 
   * @param node the Node from which the enrollment should be evaluated
   * @return true of the Node is enrolled
   */
  public boolean isNodeEnrolledInLifecycle(Node node) ;
  
  /**
   * Retrieves the name of the lifecycle in which the specified Node has
   * been enrolled.
   * 
   * @param node the Node from which the enrollment should be retrieved
   * @return the name of the lifecycle corresponding to the specified Node
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public String getNodeLifecycleName(Node node)
    throws NotInLifecycleException ;
  
  /**
   * Retrieves the description of the lifecycle in which the specified Node
   * has been enrolled.
   * 
   * This method first inspects the specified Node. If it does not contain
   * a publication mixin, then it throws a NotInPublicationLifecycleException
   * exception. Else, it retrieves the lifecycle name from the mixin,
   * selects the appropriate publication plugin and delegates the call to it.
   * 
   * @param node the Node from which the enrollment should be retrieved
   * @return the description of the lifecycle corresponding to the specified
   * Node
   * @throws NotInLifecycleException in case the Node has not
   * been registered in any lifecycle yet (in other words, if no publication
   * mixin has been found).
   */
  public String getNodeLifecycleDesc(Node node)
    throws NotInLifecycleException ;
  
  /**
   * Enroll the specified Node to the specified lifecycle.
   * This method adds a publication mixin to the specified Node. The lifecycle
   * name is the one specified as parameter. By default, the state is set
   * to "enrolled". 
   * 
   * @param node the Node to be enrolled in the specified lifecycle
   * @param lifecycle the name of the lifecycle in which the Node should be
   * enrolled
   */
  public void enrolNodeInLifecycle(Node node, String lifecycle)
      throws AlreadyInLifecycleException ;

}
