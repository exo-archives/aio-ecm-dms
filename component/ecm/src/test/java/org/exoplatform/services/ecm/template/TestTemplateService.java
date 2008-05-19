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
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.auth.AuthenticationService;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 15, 2008  
 */
public class TestTemplateService extends BaseECMTestCase{
  private String GROUP1 = "group1".intern() ;
  private String GROUP2 = "group2".intern() ;
  private String memshipType1 = "user1".intern() ;
  private String memshipType2 = "user2".intern() ;

  private NodeTemplateService nodeTemplateService_ ;
  private ArrayList<String> permission1, permission2 ;
  private SessionProviderService sessionProviderService_ ;
  private OrganizationService organizationServices_ ;
  private GroupHandler groupHandler_ ;
  private MembershipTypeHandler membershipTypeHandler_ ;
  private UserHandler userHandler_ ;
  private MembershipHandler membershipHandler_ ;
  private AuthenticationService authenticationService_ ;


  public void setUp() throws Exception {
    super.setUp() ;
    nodeTemplateService_ = (NodeTemplateService) container.getComponentInstanceOfType(NodeTemplateService.class) ;
    sessionProviderService_ = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    organizationServices_ = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class) ;
  }

  public void tearDown() {

  }

  public void testAddTemplate() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry tempEntry = new TemplateEntry() ;
    try{
      nodeTemplateService_.getTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException ex) {}
    tempEntry = createTemplateEntryToTest("nt:file", "article", "exo:article", true, true, permission1, "<abc>") ;
    assertNotNull(tempEntry) ;
    nodeTemplateService_.addTemplate(tempEntry, "repository", sessionProvider) ;

    TemplateEntry savedTemplate = new TemplateEntry() ;
    savedTemplate = nodeTemplateService_.getTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;

    assertGetTemplate(tempEntry, savedTemplate) ;

    nodeTemplateService_.removeTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
    Exception ex = new Exception();
    try{
      savedTemplate = nodeTemplateService_.getTemplate("nt:file", "exo:article", true, "repository", sessionProvider) ;
    }catch(PathNotFoundException e) {
      ex = e ;
    }
    assertEquals(ex.getClass(), PathNotFoundException.class) ;

    sessionProvider.close() ;
  }

  public void testGetTemplatePath() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry templateEntry = new TemplateEntry() ;
    try {
      nodeTemplateService_.getTemplate("xml", "news", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {}
    templateEntry = createTemplateEntryToTest("xml", "data", "news", true, false, permission1, "<abc>") ;
    String entryPath = "/exo:registry/exo:services/exo:ecm/exo:templates/"+ "xml/" +"dialog/"+ "news" ;
    nodeTemplateService_.addTemplate(templateEntry, "repository", sessionProvider) ;

    assertNotNull(templateEntry) ;
    String savedTemplatePath = nodeTemplateService_.getTemplatePath("xml", "news", true, "repository", sessionProvider) ; 
    assertNotNull(savedTemplatePath) ;
    assertEquals(entryPath, savedTemplatePath) ;

    nodeTemplateService_.removeTemplate("xml", "news", true, "repository", sessionProvider) ;
    sessionProvider.close() ;
  }

  public void testIsManagedNodeType() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry templateEntry = new TemplateEntry() ;
    try {
      nodeTemplateService_.getTemplate("xml", "news", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {}
    templateEntry = createTemplateEntryToTest("xml", "data", "news", true, false, permission1, "<abc>") ;

    nodeTemplateService_.addTemplate(templateEntry, "repository", sessionProvider) ;
    assertEquals(true, nodeTemplateService_.isManagedNodeType("xml", "repository", sessionProvider)) ;

    nodeTemplateService_.removeTemplate("xml", "news", true, "repository", sessionProvider) ;
    sessionProvider.close() ;
  }

  public void testRemoveManagedNodeType() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry templateEntry = new TemplateEntry() ;
    try {
      nodeTemplateService_.getTemplate("xml", "news", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {}
    templateEntry = createTemplateEntryToTest("xml", "data", "news", true, false, permission1, "<abc>") ;

    nodeTemplateService_.addTemplate(templateEntry, "repository", sessionProvider) ;
    nodeTemplateService_.removeManagedNodeType("xml", "repository", sessionProvider) ;
    assertEquals(false, nodeTemplateService_.isManagedNodeType("xml", "repository", sessionProvider)) ;
    Exception ex = new Exception() ;
    try{
      nodeTemplateService_.removeTemplate("xml", "news", true, "repository", sessionProvider) ;
      fail("This should has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {
      ex = e;
    }
    assertEquals(ex.getClass(), PathNotFoundException.class) ;
    sessionProvider.close() ;
  }

  public void testGetDocumentNodeTypes() throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null) ;
    TemplateEntry templateEntry1 = new TemplateEntry() ;
    TemplateEntry templateEntry2 = new TemplateEntry() ;
    try {
      nodeTemplateService_.getTemplate("xml", "news1", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {}

    try {
      nodeTemplateService_.getTemplate("xml", "news2", true, "repository", sessionProvider) ;
      fail("This shoud has been PathNotFoundException") ;
    }catch(PathNotFoundException e) {}

    templateEntry1 = createTemplateEntryToTest("xml", "label1", "news1", true, true, permission1, "<cba>") ;
    templateEntry2 = createTemplateEntryToTest("xml", "label2", "news2", true, true, permission1, "<dfdf?134!@#$%>") ;
    nodeTemplateService_.addTemplate(templateEntry1, "repository", sessionProvider) ;
    nodeTemplateService_.addTemplate(templateEntry2, "repository", sessionProvider) ;

    List<String> documentNodeTypeList = new ArrayList<String>() ;
    documentNodeTypeList = nodeTemplateService_.getDocumentNodeTypes("repository", sessionProvider) ;
    assertNotNull(documentNodeTypeList) ;
    assertEquals(2, documentNodeTypeList.size()) ;
    assertEquals("exo:file", documentNodeTypeList.get(0)) ;
    assertEquals("xml", documentNodeTypeList.get(1)) ;

    nodeTemplateService_.removeTemplate("xml", "news1", true, "repository", sessionProvider) ;
    nodeTemplateService_.removeTemplate("xml", "news2", true, "repository", sessionProvider) ;
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

  private void assertGetTemplate(TemplateEntry tempEntry, TemplateEntry savedTemplate) {
    assertEquals(tempEntry.getLabel(), savedTemplate.getLabel()) ;
    assertEquals(tempEntry.getNodeTypeName(), savedTemplate.getNodeTypeName()) ;
    assertEquals(tempEntry.getTemplateData(), savedTemplate.getTemplateData()) ;
    assertEquals(tempEntry.getTemplateName(), savedTemplate.getTemplateName()) ;
    assertEquals(tempEntry.isDialog(), savedTemplate.isDialog()) ;
    assertEquals(tempEntry.isDocumentTemplate(), savedTemplate.isDocumentTemplate()) ;
    assertEquals(tempEntry.getAccessPermissions(), savedTemplate.getAccessPermissions()) ;
  }

  private void createPermissionToTest() throws Exception {
    groupHandler_ = organizationServices_.getGroupHandler() ;
    Group group1 = groupHandler_.createGroupInstance() ;
    if (group1 == null) {System.out.println("\n\n===========> group1 null");} else {group1.setGroupName(GROUP1) ;} ;

    groupHandler_.createGroup(group1, true) ;

    Group group2 = groupHandler_.createGroupInstance() ;
    group2.setGroupName(GROUP2) ;
    groupHandler_.createGroup(group2, true) ;

    membershipTypeHandler_ = organizationServices_.getMembershipTypeHandler() ;
    MembershipType memship1 = membershipTypeHandler_.createMembershipTypeInstance() ;
    memship1.setName(memshipType1) ;
    membershipTypeHandler_.createMembershipType(memship1, true) ;

    MembershipType memship2 = membershipTypeHandler_.createMembershipTypeInstance() ;
    memship2.setName(memshipType2) ;
    membershipTypeHandler_.createMembershipType(memship2, true) ;

    userHandler_ = organizationServices_.getUserHandler() ;
    User user = userHandler_.createUserInstance() ;
    user.setUserName("jame") ;
    userHandler_.createUser(user, true) ;

    membershipHandler_ = organizationServices_.getMembershipHandler() ;
    membershipHandler_.linkMembership(user, group1, memship1, true) ;
    membershipHandler_.linkMembership(user, group2, memship1, true) ;
  }
}
