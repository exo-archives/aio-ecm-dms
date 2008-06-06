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
import java.util.Locale;
import java.util.Map.Entry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;

import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.publication.impl.StaticAndDirectPublicationPlugin;
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
  
  private String LIFECYCLE = "StaticAndDirect"; 
  
  protected static Log log;  

  protected NodeImpl                   publicationServiceTestRoot;
  
  public void setUp() throws Exception {
    super.setUp();
    log = ExoLogger.getLogger("portal:PublicationServiceImpl");
    publicationPresentationService = (PublicationPresentationService) container.getComponentInstanceOfType(PublicationPresentationService.class);
    publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);

    adminSession = repository.login(new SimpleCredentials("root", "exo".toCharArray()));
    
    NodeImpl rootAdmin = (NodeImpl) adminSession.getRootNode();
    publicationServiceTestRoot = (NodeImpl) rootAdmin.addNode(ROOT_PATH);
    publicationServiceTestRoot.addMixin("mix:versionable");
    publicationServiceTestRoot.addMixin("exo:privilegeable");
    
    publicationServiceTestRoot.setPermission("root", PermissionType.ALL); // exo
    publicationServiceTestRoot.removePermission(SystemIdentity.ANY);
    publicationServiceTestRoot.save();
    
    
  }
  
  protected void tearDown() throws Exception {

    adminSession.logout();
    adminSession = null;

    super.tearDown();

  }
  
  public void testIfPluginIsAddAtStart () {
    log.info("############################");
    log.info("# testIfPluginIsAddAtStart #");
    log.info("############################\n");
    
    assertTrue(publicationService.getPublicationPlugins().size()!=0);
    log.info(publicationService.getPublicationPlugins().get("StaticAndDirect").getName());
    log.info(publicationService.getPublicationPlugins().get("StaticAndDirect").getDescription());
    
    
    
  }
  
  public void testIfPublicationServiceIsConfigured() throws NoSuchNodeTypeException,RepositoryException {
    log.info("########################################");
    log.info("# testIfPublicationServiceIsConfigured #");
    log.info("########################################\n");
    
    assertNotNull(container.getComponentInstanceOfType(PublicationService.class));
    assertNotNull(container.getComponentInstanceOfType(PublicationPresentationService.class));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:publication"));
  }
  
  public void testEnrollAndCheckIfNodeIsEnrolled() {
    log.info("########################################");
    log.info("#  testEnrollAndCheckIfNodeIsEnrolled  #");
    log.info("########################################\n");
    
    try {
      publicationService.enrollNodeInLifecycle(publicationServiceTestRoot, LIFECYCLE);
      assertTrue(publicationService.isNodeEnrolledInLifecycle(publicationServiceTestRoot));
      System.out.println("lifeCycleName = "+publicationService.getNodeLifecycleName(publicationServiceTestRoot));
      System.out.println("CurrentState = "+publicationService.getCurrentState(publicationServiceTestRoot));
      String [][] log =publicationService.getLog(publicationServiceTestRoot);
      printLog(log,Locale.FRENCH);
      System.out.println("UserInfo FR = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.FRENCH));
      System.out.println("UserInfo EN = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.ENGLISH));
      
     
    } catch (Exception e) {
      e.printStackTrace();
    }
    

  }
  
  public void testChangeStateNonPublishedToPublished() {
    log.info("##########################################");
    log.info("# testChangeStateNonPublishedToPublished #");
    log.info("##########################################\n");
    try {
      
      Version version = publicationServiceTestRoot.checkin();
      publicationServiceTestRoot.checkout();
      
      System.out.println("isEnrolled: "+publicationService.isNodeEnrolledInLifecycle(publicationServiceTestRoot));
      System.out.println("hasMixin : "+hasMixin(publicationServiceTestRoot, "exo:publication"));
       
      publicationService.enrollNodeInLifecycle(publicationServiceTestRoot, LIFECYCLE);
      System.out.println("isEnrolled: "+publicationService.isNodeEnrolledInLifecycle(publicationServiceTestRoot));
      System.out.println("hasMixin : "+hasMixin(publicationServiceTestRoot, "exo:publication"));
      
      HashMap<String,String> context=new HashMap<String,String>();
      context.put("nodeVersionUUID", version.getUUID());
      context.put("visibility", StaticAndDirectPublicationPlugin.PUBLIC);
      publicationService.changeState(publicationServiceTestRoot, StaticAndDirectPublicationPlugin.PUBLISHED, context);
     
      
      System.out.println("lifeCycleName = "+publicationService.getNodeLifecycleName(publicationServiceTestRoot));
      System.out.println("CurrentState = "+publicationService.getCurrentState(publicationServiceTestRoot));
      String [][] log =publicationService.getLog(publicationServiceTestRoot);
      printLog(log,Locale.FRENCH);
      System.out.println("UserInfo FR = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.FRENCH));
      System.out.println("UserInfo EN = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.ENGLISH));
      
      
      version = publicationServiceTestRoot.checkin();
      publicationServiceTestRoot.save();
      publicationServiceTestRoot.checkout();
      
      context.put("nodeVersionUUID", version.getUUID());
      context.put("visibility", StaticAndDirectPublicationPlugin.PRIVATE);
      publicationService.changeState(publicationServiceTestRoot, StaticAndDirectPublicationPlugin.PUBLISHED, context);
      
      
      System.out.println("lifeCycleName = "+publicationService.getNodeLifecycleName(publicationServiceTestRoot));
      System.out.println("CurrentState = "+publicationService.getCurrentState(publicationServiceTestRoot));
      log =publicationService.getLog(publicationServiceTestRoot);
      printLog(log,Locale.FRENCH);
      System.out.println("UserInfo FR = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.FRENCH));
      System.out.println("UserInfo EN = "+publicationService.getUserInfo(publicationServiceTestRoot, Locale.ENGLISH));
      
      
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  public void test2() {
    log.info("#########");
    log.info("# test2 #");
    log.info("#########\n");
    publicationService.testMethod();
    
  }
  
  
}
