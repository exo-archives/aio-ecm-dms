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
package org.exoplatform.services.ecm.dms.scripts;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestScriptService extends BaseDMSTestCase {
    
  private ScriptService scriptService;
  private String expectedECMScriptHomePath = "/exo:ecm/scripts/ecm-explorer";
  private String expectedCBScriptHomePath = "/exo:ecm/scripts/content-browser";
  private String expectedBaseScriptPath = "/exo:ecm/scripts";
  private NodeHierarchyCreator nodeHierarchyCreator;
  private String cmsScriptsPath;  
  
  public void setUp() throws Exception {
    super.setUp();
    scriptService = (ScriptService)container.getComponentInstanceOfType(ScriptService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    cmsScriptsPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_SCRIPTS_PATH);
  }
  
  public void testInitRepo() throws Exception {
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node myScript = (Node)mySession.getItem(cmsScriptsPath);
    assertEquals(2, myScript.getNodes().getSize());
  }
  
  public void testGetECMScriptHome() throws Exception {
    assertNotNull(scriptService.getECMScriptHome(REPO_NAME, SessionProviderFactory.createSessionProvider()));
    assertEquals(expectedECMScriptHomePath, scriptService.getECMScriptHome(REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).getPath());
  }
  
  public void testGetCBScriptHome() throws Exception {
    assertNotNull(scriptService.getCBScriptHome(REPO_NAME, SessionProviderFactory.createSessionProvider()));
    assertEquals(expectedCBScriptHomePath, scriptService.getCBScriptHome(REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).getPath());
  }
  
  public void testGetECMActionScripts() throws Exception {
    assertEquals(10, scriptService.getECMActionScripts(REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).size());
  }
  
  public void testGetECMInterceptorScripts() throws Exception {
    assertEquals(3, scriptService.getECMInterceptorScripts(REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).size());
  }
  
  public void testGetECMWidgetScripts() throws Exception {
    assertEquals(4, scriptService.getECMWidgetScripts(REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).size());
  }
  
  public void testGetScript() throws Exception {
    assertNotNull(scriptService.getScript("content-browser/GetDocuments.groovy", REPO_NAME));
  }
  
  public void testGetBaseScriptPath() throws Exception {
    assertEquals(expectedBaseScriptPath, scriptService.getBaseScriptPath());
  }
  
  public void testGetScriptAsText() throws Exception {
    scriptService.addScript("My script", "This is my script as text", REPO_NAME, SessionProviderFactory.createSessionProvider());
    assertEquals("This is my script as text", scriptService.getScriptAsText("My script", REPO_NAME));
  }
  
  public void testAddScript() throws Exception {
    scriptService.addScript("Hello Name", "Hello Text", REPO_NAME, SessionProviderFactory.createSessionProvider());
    Node hello = scriptService.getScriptNode("Hello Name", REPO_NAME, 
        SessionProviderFactory.createSessionProvider());
    assertNotNull(hello);
    assertEquals("Hello Text", hello.getProperty("jcr:data").getString());
  }

  public void testRemoveScript() throws Exception {
    Node hello = scriptService.getScriptNode("Hello Name", REPO_NAME, 
        SessionProviderFactory.createSessionProvider());
    assertNotNull(hello);
    assertEquals("Hello Text", hello.getProperty("jcr:data").getString());
    
    scriptService.removeScript("Hello Name", REPO_NAME, SessionProviderFactory.createSessionProvider());
    assertNull(scriptService.getScriptNode("Hello Name", REPO_NAME, SessionProviderFactory.createSessionProvider()));
  }

  public void testGetScriptNode() throws Exception {
    scriptService.addScript("My script 2", "This is my script as text 2", REPO_NAME, SessionProviderFactory.createSessionProvider());
    
    Node scriptNode = scriptService.getScriptNode("My script 2", REPO_NAME, SessionProviderFactory.createSessionProvider());
    assertEquals("My script 2", scriptNode.getName());
    assertEquals("This is my script as text 2", scriptNode.getProperty("jcr:data").getString());
  }  
}
