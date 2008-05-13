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

import javax.jcr.Node;

import org.exoplatform.services.cms.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.cms.publication.PublicationPresentationService;
import org.exoplatform.webui.form.UIForm;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 7 mai 08  
 */ 
public class PublicationPresentationServiceImpl implements PublicationPresentationService {

  protected static Log log;  
  
  public PublicationPresentationServiceImpl () {
    log = ExoLogger.getLogger("portal:PublicationPresentationServiceImpl");
    log.info("PublicationPresentationServiceImpl initialization");    
    log.info("PublicationPresentationServiceImpl initialized");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getStateUI(javax.jcr.Node)
   */
  public UIForm getStateUI(Node node) throws NotInPublicationLifecycleException {
    // TODO Auto-generated method stub
    log.info("getStateUI");
    
    return null;
  }
}
