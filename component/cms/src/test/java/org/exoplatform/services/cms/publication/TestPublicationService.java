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
package org.exoplatform.services.cms.publication;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.command.action.ActionMatcher;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.ext.audit.AuditService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 9 mai 08  
 */
public class TestPublicationService extends BaseStandaloneTest {
  
  private static final String  ROOT_PATH      = "TestPublicationPresentationService";

  private PublicationPresentationService publicationPresentationService;
  private PublicationService publicationService;
  
  private Session exo1Session;
  private Session adminSession;
  
  protected static Log log;  
  
  public void setUp() throws Exception {
    super.setUp();
    log = ExoLogger.getLogger("portal:PublicationServiceImpl");
    publicationPresentationService = (PublicationPresentationService) container.getComponentInstanceOfType(PublicationPresentationService.class);
    publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);

    exo1Session = repository.login(new SimpleCredentials("exo1", "exo1".toCharArray()));
    adminSession = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    
  }
  
  protected void tearDown() throws Exception {
    exo1Session.logout();
    exo1Session = null;

    adminSession.logout();
    adminSession = null;

    super.tearDown();

  }
  
  public void testIfPublicationServiceIsConfigured() {
    log.info("########################################");
    log.info("# testIfPublicationServiceIsConfigured #");
    log.info("########################################\n");
    
    assertNotNull(container.getComponentInstanceOfType(PublicationService.class));
    assertNotNull(container.getComponentInstanceOfType(PublicationPresentationService.class));
  }
  
  public void test2() {
    log.info("#########");
    log.info("# test2 #");
    log.info("#########\n");
    
    
  }
  
  
}
