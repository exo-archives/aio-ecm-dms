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
package org.exoplatform.services.ecm.template;

import java.util.ArrayList;

import javax.jcr.PathNotFoundException;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 15, 2008  
 */
public class TestTemplateService extends BaseECMTestCase{
  private NodeTemplateService nodeTemplateService_ ;
  private ArrayList<String> permission1, permission2 ;
  private SessionProviderService sessionProviderService_ ;
  
  public void setUp() throws Exception {
    super.setUp() ;
    nodeTemplateService_ = (NodeTemplateService) container.getComponentInstanceOfType(NodeTemplateService.class) ;
    sessionProviderService_ = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
  }
  
  public void tearDown() {
    
  }
  
  public void testAddTemplate() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry tempEntry = new TemplateEntry() ;
    tempEntry = createTemplateEntryToTest("nt:file", "article", "exo:article", true, true, permission1, "<abc>") ;
    assertNotNull(tempEntry) ;
    nodeTemplateService_.addTemplate(tempEntry, "repository", sessionProvider) ;
    
    TemplateEntry savedTemplate = new TemplateEntry() ;
    savedTemplate = nodeTemplateService_.getTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
    
    nodeTemplateService_.removeTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
    Exception ex = new Exception();
    try{
      savedTemplate = nodeTemplateService_.getTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
    }catch(PathNotFoundException e) {
      ex = e ;
    }
    assertEquals(ex.getClass(), PathNotFoundException.class) ;
    assertGetTemplate(tempEntry, savedTemplate) ;
  }
  
  private TemplateEntry createTemplateEntryToTest(String nodeTypeName, String label, String templateName,
                                                  Boolean isDialog, Boolean isDocumentTemplate, 
                                                  ArrayList<String> permission, String templateData) {
    TemplateEntry tempEntry = new TemplateEntry() ;
    tempEntry.setNodeTypeName(nodeTypeName) ;
    tempEntry.setLabel(label) ;
    tempEntry.setTemplateName(templateName) ;
    tempEntry.setDialog(isDialog) ;
    tempEntry.setDocumentTemplate(isDocumentTemplate) ;
    permission = new ArrayList<String>() ;
    for (int i=0; i < 3;i++) {
      permission.add(templateName + i) ;
    }
    tempEntry.setAccessPermissions(permission) ;
    tempEntry.setTemplateData(templateData) ;
    return tempEntry ;
  }
  
  private void assertGetTemplate(TemplateEntry tempEntry, TemplateEntry savedTemplate) {
    assertEquals(tempEntry.getLabel(), savedTemplate.getLabel()) ;
    assertEquals(tempEntry.getNodeTypeName(), savedTemplate.getNodeTypeName()) ;
    assertEquals(tempEntry.getTemplateData(), savedTemplate.getTemplateData()) ;
    assertEquals(tempEntry.getTemplateName(), savedTemplate.getTemplateName()) ;
    assertEquals(tempEntry.isDialog(), savedTemplate.isDialog()) ;
    assertEquals(tempEntry.isDocumentTemplate(), savedTemplate.isDocumentTemplate()) ;
    assertEquals(tempEntry.getAccessPermissions(), savedTemplate.getAccessPermissions()) ;
  }
}
