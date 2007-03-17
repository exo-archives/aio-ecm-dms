/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.OrganizationService;

/**
 * @author benjaminmestrallet
 */
public class BaseTest extends TestCase{
  
  protected static final String WORKSPACE = "ws1";
  protected Node root;
  protected Repository repository;
  protected SessionImpl session;
  protected SimpleCredentials credentials;
  protected PortalContainer servicesManager;
  protected OrganizationService orgService;
  protected TemplateService dialogService;
  protected ScriptService scriptService;
  protected CmsService cmsService;

  public void setUp() throws Exception{
    
    LogService logService = (LogService) RootContainer.getInstance().getComponentInstanceOfType(
               LogService.class); 

    logService.setLogLevel("org.exoplatform.services.jcr", LogService.DEBUG, true);

    servicesManager = RootContainer.getInstance().getPortalContainer("admin");

    if(System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/resource/login.conf" );

    credentials = new SimpleCredentials("admin", "admin".toCharArray());

    RepositoryService repositoryService = (RepositoryService) servicesManager.getComponentInstanceOfType(
                      RepositoryService.class);
    
    repository = repositoryService.getRepository();
    
    orgService = (OrganizationService)servicesManager.getComponentInstanceOfType(OrganizationService.class);
    
    scriptService = (ScriptService)servicesManager.getComponentInstanceOfType(
                    ScriptService.class);
        
    dialogService = (TemplateService) servicesManager.getComponentInstanceOfType(TemplateService.class);
    
    cmsService = (CmsService) servicesManager.getComponentInstanceOfType(
                 CmsService.class);
    
    if(!((RepositoryImpl) repository).isWorkspaceInitialized("production"));
      ((RepositoryImpl) repository).initWorkspace("production", "nt:unstructured");
  
    if(!((RepositoryImpl) repository).isWorkspaceInitialized("draft"));
      ((RepositoryImpl) repository).initWorkspace("draft", "nt:unstructured");

    session = (SessionImpl)repository.login(credentials, "production");
    root = session.getRootNode();    
  }
  
/*  
  public void setUp() throws Exception {    
    //StandaloneContainer.setConfigurationPath("src/java/conf/standalone/test-configuration.xml");
    StandaloneContainer container = StandaloneContainer.getInstance();    
    
    LogService logService = (LogService)container.getComponentInstanceOfType(
               LogService.class);

    Log log = logService.getLog("org.exoplatform.services.jcr");

    logService.setLogLevel("org.exoplatform.services.jcr", LogService.DEBUG,
    true);

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config",
          "src/resource/login.conf");
  
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());

    RepositoryService repositoryService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    
    repository = (RepositoryImpl) repositoryService
                  .getRepository();

    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    root = session.getRootNode();  
  }
*/  
}
