/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.impl.ViewDataImpl;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.test.BasicTestCase;
import org.hibernate.Session;

/**
 * wen, mar 1, 2006 @   
 * @author: Nguyen Quang Hung
 * @version: $Id: TestManageViewData.java $
 * @since: 0.0
 * @email: nguyekequanghung@yahoo.com
 */
public class TestManageViewService  extends BasicTestCase {
  ManageViewService service_ ;
  HibernateService hservice_ ;
  public TestManageViewService(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    service_ = (ManageViewService) PortalContainer.getInstance().
                getComponentInstanceOfType(ManageViewService.class) ;
    hservice_ = (HibernateService) PortalContainer.getInstance().
                getComponentInstanceOfType(HibernateService.class) ;
  }

  public void tearDown() throws Exception {}
  /*
  public void testGetAdminView() throws Exception {
    ViewData view = service_.getAdminView() ;
    assertNotNull(view) ;
    assertEquals(view.getViewName(), "admin") ;
    
    List tabs = view.getAllTabs() ;
    assertEquals(tabs.size(), 4) ;
    
    List buttons = view.getAllButtons() ;
    assertEquals(buttons.size(), 3) ;
    
    List permissions = view.getAllPermissions() ;
    assertEquals(permissions.size(), 1) ;
    
    assertEquals(permissions.get(0).toString(), "member:/admin") ;
  }
  
  public void testGetDefaultView() throws Exception {
    ViewData view = service_.getDefaultView() ;
    assertNotNull(view) ;
    assertEquals(view.getViewName(), "default") ;
    
    String temp = view.getTemplate() ;
    assertEquals(temp , "mac") ;
    
    List permissions = view.getAllPermissions() ;
    assertEquals(permissions.size(), 2) ;
    
    Collection perCol = new ArrayList() ;
    perCol.add("*:/user") ;
    perCol.add("member:/admin") ;
    assertTrue(permissions.containsAll(perCol)) ;
    
  }
  
  public void testGetExoView() throws Exception {
    ViewData view = service_.getViewByName("exo") ;
    assertNotNull(view) ;
    assertEquals(view.getViewName(), "exo") ;
    
    List permissions = view.getAllPermissions() ;
    assertEquals(permissions.size(), 2) ;
    
    Collection perCol = new ArrayList() ;
    perCol.add("member:/user") ;
    perCol.add("owner:/user") ;
    assertTrue(permissions.containsAll(perCol)) ;
    
    String temp = view.getTemplate() ;
    assertEquals(temp , "mac") ;
    
  }
  
  public void testGetAllViews() throws Exception {
    List allView = service_.getAllViews() ;
    assertEquals(allView.size(), 3) ;
  }
  
  public void testSave() throws Exception {
    ViewDataImpl newData = new ViewDataImpl() ;
    newData.setId("myView") ;
    newData.setViewName("myView") ;
    newData.setPermissions("*:/user;member:/admin") ;
    newData.setTabs("Actions;Info;Admin") ;
    newData.setButtons("Actions:addFolder;Actions:addDocument;Info:viewProperties;Info:viewPermissions;Admin:versioning") ;
    newData.setTemplate("list") ;
    Session session = hservice_.openSession() ;
    session.save(newData) ;
    session.flush() ;
    
    List allView = service_.getAllViews() ;
    assertEquals(allView.size(), 4) ;
    
    ViewData myView = service_.getViewByName("myView") ;
    assertEquals(myView.getViewName() , "myView") ;
    
    List permission = myView.getAllPermissions() ;
    assertEquals(permission.size() , 2) ;
    
    List buttons = myView.getAllButtons() ;
    assertEquals(buttons.size() , 5) ;
    
    List tabs = myView.getAllTabs() ;
    assertEquals(tabs.size() , 3) ;
    
    assertTrue(myView.hasPermission("member:/user")) ;
    assertFalse(myView.hasPermission(null)) ;
    assertFalse(myView.hasPermission("invalid permission")) ;
    assertFalse(myView.hasPermission("hello:/guest")) ;   
    
    assertEquals(myView.getTemplate() , "list") ;    
  }
  
  public void testInitiateButtonTab() {
    List tabs = service_.getTabs() ;
    assertEquals(tabs.size() , 3) ;
    List buttons = service_.getButtons() ;
    assertEquals(buttons.size() , 3) ;
  }
   */
}