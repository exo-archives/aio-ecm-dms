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
package org.exoplatform.services.ecm.dms.metadata;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestMetadataService extends BaseDMSTestCase {
    
  private MetadataService metadataService;
  private String expectedArticleDialogPath = "/exo:ecm/metadata/exo:article/dialogs/dialog1";
  private NodeHierarchyCreator nodeHierarchyCreator;
  private String baseMetadataPath;

  static private final String EXO_ARTICLE = "exo:article".intern();
  
  public void setUp() throws Exception {
    super.setUp();
    metadataService = (MetadataService)container.getComponentInstanceOfType(MetadataService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    baseMetadataPath = nodeHierarchyCreator.getJcrPath(BasePath.METADATA_PATH);
  }
  
  public void testGetMetadataList() throws Exception {
    List<String> metadataTypes = metadataService.getMetadataList(REPO_NAME);
    assertEquals(8, metadataTypes.size());
  }
  
  public void testGetAllMetadatasNodeType() throws Exception {
    List<NodeType> metadataTypes = metadataService.getAllMetadatasNodeType(REPO_NAME);
    assertEquals(8, metadataTypes.size());
  }
  
  public void testAddMetadata() throws Exception {
    assertNull(metadataService.getMetadataTemplate(EXO_ARTICLE, true, REPO_NAME));
    metadataService.addMetadata(EXO_ARTICLE, true, "*:/platform/administrators", "This is content", true, REPO_NAME);
    assertEquals("This is content", metadataService.getMetadataTemplate(EXO_ARTICLE, true, REPO_NAME));
  }

  public void testRemoveMetadata() throws Exception {
    assertEquals("This is content", metadataService.getMetadataTemplate(EXO_ARTICLE, true, REPO_NAME));
    metadataService.removeMetadata(EXO_ARTICLE, REPO_NAME);
    assertNull(metadataService.getMetadataTemplate(EXO_ARTICLE, true, REPO_NAME));
  }

  public void testGetExternalMetadataType() throws Exception {
    List<String> extenalMetaTypes = metadataService.getExternalMetadataType(REPO_NAME);
    assertEquals(1, extenalMetaTypes.size());
  }

  public void testGetMetadataTemplate() throws Exception {
    metadataService.addMetadata(EXO_ARTICLE, true, "*:/platform/administrators", "This is content", true, REPO_NAME);
    assertEquals("This is content", metadataService.getMetadataTemplate(EXO_ARTICLE, true, REPO_NAME));
  }

  public void testGetMetadataPath() throws Exception {
    metadataService.addMetadata(EXO_ARTICLE, true, "*:/platform/administrators", "This is my content", true, REPO_NAME);
    assertEquals(expectedArticleDialogPath, metadataService.getMetadataPath(EXO_ARTICLE, true, REPO_NAME));
  }

  public void testGetMetadataRoles() throws Exception {
    assertEquals("*:/platform/administrators", metadataService.getMetadataRoles(EXO_ARTICLE, true, REPO_NAME));
  }

  public void testHasMetadata() throws Exception {
    assertTrue(metadataService.hasMetadata(EXO_ARTICLE, REPO_NAME));
    metadataService.removeMetadata(EXO_ARTICLE, REPO_NAME);
    assertFalse(metadataService.hasMetadata(EXO_ARTICLE, REPO_NAME));
  } 

  public void testInit() throws Exception {
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node myMetadata = (Node)mySession.getItem(baseMetadataPath);
    assertEquals(1, myMetadata.getNodes().getSize());
  }
}
