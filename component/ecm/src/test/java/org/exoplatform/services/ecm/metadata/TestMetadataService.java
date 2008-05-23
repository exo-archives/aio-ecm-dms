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
package org.exoplatform.services.ecm.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.ecm.template.TemplateEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 22, 2008  
 */
public class TestMetadataService extends BaseECMTestCase {

  private RegistryService registryService_ ;
  private MetadataManagerService metadataManagerService_ ;
  private ArrayList<String> permission ;

  public void setUp() throws Exception {
    super.setUp() ;
    metadataManagerService_ = (MetadataManagerService) container.getComponentInstanceOfType(MetadataManagerService.class) ;
    registryService_ = (RegistryService) container.getComponentInstanceOfType(RegistryService.class) ;
  }

  public void testAddMetaDataTemplate() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String entryPath = "exo:services/exo:ecm/exo:metadata/xml/dialogs/news" ;
    RegistryEntry regEntry = null ;
    ArrayList<String> permission1 = new ArrayList<String>() ;
    permission1.add("member:/organization/management/executive-board") ;
    permission1.add("manager:/platform/administrators") ;
    TemplateEntry entry = new TemplateEntry() ;
    entry = createTemplateEntryToTest("xml", "label", "news", true, true, permission, "<%!@#$&**(%>") ;
    assertNotNull(entry) ;

    metadataManagerService_.addMetadataTemplate(entry, "repository", sessionProvider) ;

    try{
      regEntry = registryService_.getEntry(sessionProvider, entryPath) ;
      Document doc = regEntry.getDocument() ;
      Element element = doc.getDocumentElement() ;

      assertEquals("xml", element.getAttribute("exo:nodeTypeName")) ;
      assertEquals("label", element.getAttribute("exo:label")) ;
      assertEquals("news", element.getAttribute("exo:templateName")) ;
      assertEquals(true, Boolean.parseBoolean(element.getAttribute("exo:isDialog"))) ;
      assertEquals(permission1, fromXmlMultiValue(element.getAttribute("exo:accessPermissions"))) ;
      assertEquals("<%!@#$&**(%>", getCDATA(doc, "exo:templateData")) ;
    }catch(PathNotFoundException e) {
      fail("Error in add method: haven't just added metadataTemplate") ;
    }
    String metadataPath = null ;
    metadataPath = metadataManagerService_.getMetadataPath("xml", true, "repository", sessionProvider) ;
    assertNotNull(metadataPath) ;
    assertEquals("/exo:registry/"+entryPath, metadataPath) ;

    metadataManagerService_.removeMetadataTemplateType("xml", "repository", sessionProvider) ;
    try{
      RegistryEntry entry2 = registryService_.getEntry(sessionProvider, entryPath) ;
      assertNotNull(entry2) ;
      fail("\n\n=====>This should have thrown PathNotFoundException\n\n");
    }catch(PathNotFoundException e) {}
    sessionProvider.close() ;
  }

  public void testGetMetadataPaths() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    List<String> paths = new ArrayList<String>() ;
    TemplateEntry tempEntry1 = new TemplateEntry() ;
    TemplateEntry tempEntry2 = new TemplateEntry() ;
    TemplateEntry tempEntry3 = new TemplateEntry() ;
    TemplateEntry tempEntry4 = new TemplateEntry() ;

    tempEntry1 = createTemplateEntryToTest("file", "one", "poem", false, true, permission, "<@#!$%#?>") ;
    tempEntry2 = createTemplateEntryToTest("file", "two", "essay", false, true, permission, "<!@#$%^&*?>") ;
    tempEntry3 = createTemplateEntryToTest("exo:file", "three", "project", false, true, permission, "<!@#$%^&*?>") ;
    tempEntry4 = createTemplateEntryToTest(null, null, null, true, true, permission, "<adf>") ;

    metadataManagerService_.addMetadataTemplate(tempEntry1, "repository", sessionProvider) ;
    metadataManagerService_.addMetadataTemplate(tempEntry2, "repository", sessionProvider) ;
    metadataManagerService_.addMetadataTemplate(tempEntry3, "repository", sessionProvider) ;

    try {
      metadataManagerService_.addMetadataTemplate(tempEntry4, "repository", sessionProvider) ;
      fail("This should have thrown Exception") ;
    } catch (Exception e) {}

    paths = metadataManagerService_.getMetadataPaths("file", false, "repository", sessionProvider) ;
    assertEquals(3, paths.size()) ;
    assertEquals(1, metadataManagerService_.getMetadataPaths("exo:file", false, "repository", sessionProvider).size()) ;

    metadataManagerService_.removeMetadataTemplateType("file", "repository", sessionProvider) ;
    metadataManagerService_.removeMetadataTemplateType("exo:file", "repository", sessionProvider) ;
    sessionProvider.close() ;
  }

  public void testGetAllMetadataNodeType() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    List<NodeType> nodeTypeList = new ArrayList<NodeType>() ;
    NodeTypeManager nodeTypeManager =  registryService_.getRepositoryService().getRepository("repository").getNodeTypeManager() ;
    NodeTypeValue nodeTypeValue = new NodeTypeValue() ;
    List<String> superType = new ArrayList<String>() ;
    superType.add("exo:metadata") ;
    nodeTypeValue.setName("myNodeType") ;
    nodeTypeValue.setPrimaryItemName("") ;
    nodeTypeValue.setMixin(true) ;
    nodeTypeValue.setDeclaredSupertypeNames(superType) ;
    ExtendedNodeTypeManager extNodeTypeManager = (ExtendedNodeTypeManager) nodeTypeManager ;
    extNodeTypeManager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS) ;
    
    nodeTypeList = metadataManagerService_.getAllMetadataNodeType("repository") ;
    assertNotNull(nodeTypeList) ;
    assertEquals(4, nodeTypeList.size()) ;
    // can not delete NodeType which is created.
    sessionProvider.close() ;
  }

  public void testIsManagedNodeType() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    TemplateEntry templateEntry1 = new TemplateEntry() ;
    templateEntry1 = createTemplateEntryToTest("file", "one", "poem", false, true, permission, "<@#!$%#?>") ;
    metadataManagerService_.addMetadataTemplate(templateEntry1, "repository", sessionProvider) ;

    boolean isManagedNodeType = false ;
    isManagedNodeType = metadataManagerService_.isManagedNodeType("file", "repository", sessionProvider) ;
    assertEquals(true, isManagedNodeType) ;
    assertEquals(false, metadataManagerService_.isManagedNodeType("myNodeType", "repository", sessionProvider)) ;

    metadataManagerService_.removeMetadataTemplateType("file", "repository", sessionProvider) ;
    sessionProvider.close() ;
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
    permission.add("member:/organization/management/executive-board") ;
    permission.add("manager:/platform/administrators") ;
    tempEntry.setAccessPermissions(permission) ;
    tempEntry.setTemplateData(templateData) ;
    return tempEntry ;
  }

  private ArrayList<String> fromXmlMultiValue(String list) {
    ArrayList<String> values = new ArrayList<String>() ;
    for (String value: list.split(" ")) {
      values.add(value) ;
    }
    return values ;
  }

  private String getCDATA(Document doc, String tagName) {
    Node node = doc.getElementsByTagName(tagName).item(0) ;
    return node.getTextContent() ; 
  }
}
