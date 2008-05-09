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
package org.exoplatform.services.cms.publication.impl;

import java.util.HashMap;
import java.util.Set;

import javax.jcr.Node;

import org.exoplatform.services.cms.publication.AlreadyInPublicationLifecycleException;
import org.exoplatform.services.cms.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.cms.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.cms.publication.PublicationPlugin;
import org.exoplatform.services.cms.publication.PublicationService;
import org.exoplatform.webui.form.UIForm;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 7 mai 08  
 */
public class PublicationServiceImpl implements PublicationService {

  protected static Log log;  
  
  public PublicationServiceImpl () {
    log = ExoLogger.getLogger("portal:PublicationServiceImpl");
    log.info("PublicationService initialization");    
    log.info("PublicationService initialized");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#addLog(javax.jcr.Node, java.lang.String[])
   */
  public void addLog(Node node, String[] log) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("addLog");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#addPublicationPlugin(org.exoplatform.services.cms.publication.PublicationPlugin)
   */
  public void addPublicationPlugin(PublicationPlugin p) {
    // TODO Auto-generated method stub
    log.info("addPublicationPlugin");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#changeState(javax.jcr.Node, java.lang.String, java.util.HashMap)
   */
  public void changeState(Node node, String newState, HashMap<String, String> context)
      throws NotInPublicationLifecycleException, IncorrectStateUpdateLifecycleException {
    // TODO Auto-generated method stub
    log.info("changeState");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#enrollNodeInLifecycle(javax.jcr.Node, java.lang.String)
   */
  public void enrollNodeInLifecycle(Node node, String lifecycle)
      throws AlreadyInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("enrollNodeInLifecycle");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getCurrentState(javax.jcr.Node)
   */
  public String getCurrentState(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getCurrentState");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getLog(javax.jcr.Node)
   */
  public String[][] getLog(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getLog");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getNodeLifecycleDesc(javax.jcr.Node)
   */
  public String getNodeLifecycleDesc(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getNodeLifecycleDesc");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getNodeLifecycleName(javax.jcr.Node)
   */
  public String getNodeLifecycleName(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getNodeLifecycleName");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getPublicationPlugins()
   */
  public Set<PublicationPlugin> getPublicationPlugins() {
    // TODO Auto-generated method stub
    log.info("getPublicationPlugins");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getStateImage(javax.jcr.Node)
   */
  public byte[] getStateImage(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getStateImage");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getStateUI(javax.jcr.Node)
   */
  public UIForm getStateUI(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getStateUI");
    
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getUserInfo(javax.jcr.Node)
   */
  public String getUserInfo(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getUserInfo");
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#isNodeEnrolledInLifecycle(javax.jcr.Node)
   */
  public boolean isNodeEnrolledInLifecycle(Node node) {
    // TODO Auto-generated method stub
    log.info("isNodeEnrolledInLifecycle");
    return false;
  }

}
