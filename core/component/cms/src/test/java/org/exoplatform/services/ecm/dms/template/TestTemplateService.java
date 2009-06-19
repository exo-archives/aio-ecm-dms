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
package org.exoplatform.services.ecm.dms.template;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestTemplateService extends BaseDMSTestCase {
    
  private TemplateService templateService;
  private String expectedArticleDialogPath = "/exo:ecm/templates/exo:article/dialogs/dialog1";
  private String expectedTemplateLabel = "Article";
  private NodeHierarchyCreator nodeHierarchyCreator;
  private String cmsTemplatesBasePath;
  
  static private final String DMSSYSTEM_WS = "dms-system".intern();
  static private final String EXO_ARTICLE = "exo:article".intern();
  
  public void setUp() throws Exception {
    super.setUp();
    templateService = (TemplateService)container.getComponentInstanceOfType(TemplateService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    cmsTemplatesBasePath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_TEMPLATES_PATH);
  }
  
  public void testGetDefaultTemplatePath() throws Exception {
    assertEquals(expectedArticleDialogPath, templateService.getDefaultTemplatePath(true, EXO_ARTICLE));
  }
  
  public void testGetTemplatesHome() throws Exception {
    assertNotNull(templateService.getTemplatesHome(REPO_NAME, SessionProviderFactory.createSessionProvider()));
  }
  
  public void testGetTemplatePath() throws Exception {    
    Session sessionSystem = repository.login(credentials, DMSSYSTEM_WS);
    Node root = sessionSystem.getRootNode();
    Node aaa = root.addNode("AAA");
    Node bbb = root.addNode("BBB", "exo:article");
    bbb.setProperty("exo:title", "Hello");
    sessionSystem.save();
    
    assertEquals(expectedArticleDialogPath, templateService.getTemplatePath(bbb, true));
    
    Exception e = null;
    try {
      templateService.getTemplatePath(aaa, true);
    } catch (Exception ex) {
      e = ex;
      assertNotNull(e);
    }
    
    assertEquals(expectedArticleDialogPath, templateService.getTemplatePath(true, EXO_ARTICLE, "dialog1", REPO_NAME));
  }
  
  public void testGetTemplatePathByAnonymous() throws Exception {    
    assertEquals(expectedArticleDialogPath, templateService.getTemplatePathByAnonymous(true, EXO_ARTICLE, REPO_NAME));
  }
  
  public void testGetTemplatePathByUser() throws Exception {    
    assertEquals(expectedArticleDialogPath, templateService.getTemplatePathByUser(true, EXO_ARTICLE, "root", REPO_NAME));
  }
  
  public void testGetTemplate() throws Exception {    
    assertNotNull(templateService.getTemplate(true, EXO_ARTICLE, "dialog1", REPO_NAME));
    assertNotNull(templateService.getTemplate(false, EXO_ARTICLE, "view1", REPO_NAME));
  }
  
  public void testAddTemplate() throws Exception {    
    boolean isDialog = true;
    String label = "AALabel";
    boolean isDocumentTemplate = true;
    String templateName = "AAName"; 
    String templateFile = "Hello";
    String[] roles = {"*"};
    assertNotNull(templateService.addTemplate(isDialog, EXO_ARTICLE, label, isDocumentTemplate, 
        templateName, roles, templateFile, REPO_NAME));
    assertNotNull(templateService.getTemplate(true, EXO_ARTICLE, templateName, REPO_NAME));
  }
  
  public void testAddTemplateWithLocale() throws Exception {    
    boolean isDialog = true;
    String label = "BBLabel";
    boolean isDocumentTemplate = true;
    String templateName = "BBName"; 
    String templateFile = "BBHello";
    String[] roles = {"*"};
    String locale = "en";
    assertNotNull(templateService.addTemplateWithLocale(isDialog, EXO_ARTICLE, label, isDocumentTemplate, 
        templateName, roles, templateFile, REPO_NAME, locale));
    assertNotNull(templateService.getTemplate(true, EXO_ARTICLE, templateName, REPO_NAME));
  }
  
  public void testRemoveTemplate() throws Exception {    
    assertNotNull(templateService.getTemplate(false, EXO_ARTICLE, "view1", REPO_NAME));
    
    templateService.removeTemplate(false, EXO_ARTICLE, "view1", REPO_NAME);
    
    Exception e = null;
    try {
      templateService.getTemplate(false, EXO_ARTICLE, "view1", REPO_NAME);
    } catch (Exception ex) {
      e = ex;
      assertNotNull(e);
    }
  }
  
  public void testIsManagedNodeType() throws Exception {    
    assertTrue(templateService.isManagedNodeType(EXO_ARTICLE, REPO_NAME));
  }
  
  public void testGetDocumentTemplates() throws Exception {    
    assertNotNull(templateService.getDocumentTemplates(REPO_NAME));
  }
  
  public void testGetAllTemplatesOfNodeType() throws Exception {
    assertEquals(1, templateService.getAllTemplatesOfNodeType(true, "exo:sample", REPO_NAME, 
        SessionProviderFactory.createSessionProvider()).getSize());
  }
  
  public void testRemoveManagedNodeType() throws Exception {
    assertTrue(templateService.isManagedNodeType("exo:podcast", REPO_NAME));    
    templateService.removeManagedNodeType("exo:podcast", REPO_NAME);    
    assertFalse(templateService.isManagedNodeType("exo:podcast", REPO_NAME));
  }
  
  public void testGetTemplateLabel() throws Exception {    
    assertEquals(expectedTemplateLabel, templateService.getTemplateLabel(EXO_ARTICLE ,REPO_NAME));
  }
  
  public void testGetTemplateRoles() throws Exception {    
    assertEquals("*", templateService.getTemplateRoles(true, EXO_ARTICLE, "dialog1", REPO_NAME));
  }
  
  public void testGetTemplateNode() throws Exception {
    assertNotNull(templateService.getTemplateNode(true, EXO_ARTICLE, "dialog1", REPO_NAME, 
        SessionProviderFactory.createSessionProvider()));
  }
  
  public void testGetCreationableContentTypes() throws Exception {
    Session sessionSystem = repository.login(credentials, DMSSYSTEM_WS);
    Node root = sessionSystem.getRootNode();
    Node ddd = root.addNode("DDD", "exo:article");
    ddd.setProperty("exo:title", "Hello DDD");
    sessionSystem.save();
    
    assertNotNull(templateService.getCreationableContentTypes(ddd));
  }
  
  public void testGetTemplateData() throws Exception {
    Session sessionSystem = repository.login(credentials, DMSSYSTEM_WS);
    Node root = sessionSystem.getRootNode();
    Node eee1 = root.addNode("EEE");
    Node eee = eee1.addNode("EEE", "exo:article");
    eee.setProperty("exo:title", "Hello EEE");
    sessionSystem.save();
    
    assertEquals("Hello EEE", templateService.getTemplateData(eee, "en", "exo:title", REPO_NAME));
  }
  
  public void testInit() throws Exception {
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node myTemplate = (Node)mySession.getItem(cmsTemplatesBasePath);
    assertTrue(myTemplate.getNodes().getSize() > 0);
  }
}
