/*
 * Created on Mar 3, 2005
 */
package org.exoplatform.services.velocity.impl;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.OrganizationService;

/**
 * @author benjaminmestrallet
 */
public class TestJCRResourceLoader extends TestCase{
  
  protected Repository jcrRepository_;
  protected PortalContainer pcontainer_;


  public void setUp() throws Exception{
    pcontainer_ = PortalContainer.getInstance();
    LogService service = 
      (LogService) RootContainer.getInstance().getComponentInstanceOfType(LogService.class);    
    service.setLogLevel("org.exoplatform.services.jcr", LogService.WARN, true);    
    
    RepositoryService jcrService = 
      (RepositoryService) pcontainer_.getComponentInstanceOfType(RepositoryService.class);    
    
    
    PortalContainer.getInstance().createSessionContainer("id", "exo");

    if(System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/resource/login.conf" );          
          
    jcrRepository_ = jcrService.getRepository();
    OrganizationService orgService = (OrganizationService)pcontainer_.getComponentInstanceOfType(
        OrganizationService.class);
  }
  
  public void testResourceLoader() throws Exception {
    Session session = jcrRepository_.login();    
    Node root = session.getRootNode();
  }
}