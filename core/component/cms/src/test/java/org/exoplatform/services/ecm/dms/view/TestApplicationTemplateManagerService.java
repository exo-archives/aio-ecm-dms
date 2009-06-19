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
package org.exoplatform.services.ecm.dms.view;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ApplicationTemplateManagerService;
import org.exoplatform.services.cms.views.PortletTemplatePlugin.PortletTemplateConfig;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestApplicationTemplateManagerService extends BaseDMSTestCase {
    
  private ApplicationTemplateManagerService appTemplateManagerService;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private String basedApplicationTemplatesPath;
  
  public void setUp() throws Exception {
    super.setUp();
    appTemplateManagerService = (ApplicationTemplateManagerService)container.getComponentInstanceOfType(
        ApplicationTemplateManagerService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    basedApplicationTemplatesPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);
  }
  
  public void testAddPlugin() throws Exception {
    
  }
  
  public void testGetAllManagedPortletName() throws Exception {
    List<String> listTemplateManager = appTemplateManagerService.getAllManagedPortletName(REPO_NAME);
    assertEquals(0, listTemplateManager.size());
  }
  
  public void testGetTemplatesByApplication() throws Exception {
    assertNull(appTemplateManagerService.getTemplatesByApplication(REPO_NAME, 
        "UIBrowseContentPortlet", SessionProviderFactory.createSessionProvider()));
  }
  
  public void testGetTemplatesByCategory() throws Exception {
    assertEquals(1, appTemplateManagerService.getTemplatesByCategory(REPO_NAME, "content-browser", 
        "detail-document", SessionProviderFactory.createSessionProvider()).size());
  }
  
  public void testGetTemplateByName() throws Exception {
    assertNotNull(appTemplateManagerService.getTemplateByName(REPO_NAME, "content-browser", 
        "detail-document", "DocumentView", SessionProviderFactory.createSessionProvider()));
  }
  
  public void testGetTemplateByPath() throws Exception {
    assertNotNull(appTemplateManagerService.getTemplateByPath(REPO_NAME, 
        "/exo:ecm/views/templates/content-browser/detail-document/DocumentView", SessionProviderFactory.createSessionProvider()));
  }
  
  public void testAddTemplate() throws Exception {
    Session sessionSystem = repository.login(credentials, DMSSYSTEM_WS);
    Node portletTemplateHome = (Node) sessionSystem.getItem(basedApplicationTemplatesPath);
    
    PortletTemplateConfig config = new PortletTemplateConfig();
    ArrayList<String> accessPermissions = new ArrayList<String>();
    accessPermissions.add("*:/platform/administrators");
    config.setCategory("categoryA");
    config.setAccessPermissions(accessPermissions);
    config.setEditPermissions(accessPermissions);
    config.setTemplateName("HelloName");
    config.setTemplateData("Hello teamplate data");    
    appTemplateManagerService.addTemplate(portletTemplateHome, config);
    
    assertNotNull(appTemplateManagerService.getTemplateByPath(REPO_NAME, 
        "/exo:ecm/views/templates/categoryA/HelloName", SessionProviderFactory.createSessionProvider()));
  }
  
  /**
   * Test Method: removeTemplate(): 
   * Input: Test node has multi-languages node, but not "jp" language.
   *        Comment node has properties: Commenter = root,
   *                                     Commentor_email = root@exoplatform,
   *                                     Commentor_messages = Hello,
   *                                     Language = jp
   * Expected Result:
   *       "jp" node is added in languages node
   *       comment node is added in "jp" node with properties like input.
   */
  public void testRemoveTemplate() throws Exception {
    appTemplateManagerService.removeTemplate(REPO_NAME, "content-browser", "detail-document", 
        "DocumentView", SessionProviderFactory.createSessionProvider());
    
    assertEquals(0, appTemplateManagerService.getTemplatesByCategory(REPO_NAME, "content-browser", 
        "detail-document", SessionProviderFactory.createSessionProvider()).size());
  }
}
