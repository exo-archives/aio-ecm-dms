/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.queries;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 29, 2006  
 * 11:27:14 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIQueriesManager extends UIContainer {

  public UIQueriesManager() throws Exception {
    addChild(UIQueriesList.class, null, null) ;
  }
  
  public void update() throws Exception {
    getChild(UIQueriesList.class).updateQueriesGrid() ;
  }
  public void initFormPopup(String id) throws Exception {
    removeChildById(id) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, id) ;
    uiPopup.setWindowSize(600, 500) ;
    UIQueriesForm uiForm = createUIComponent(UIQueriesForm.class, null, null) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPermissionPopup(String membership) throws Exception {
    removeChildById("PermissionPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PermissionPopup");
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = 
      createUIComponent(UIECMPermissionBrowser.class, null, "QueriesPermissionBrowse") ;
    if(membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/") ;
      uiECMPermission.setCurrentPermission("/" + arrMember[1]) ;
    }
    uiPopup.setUIComponent(uiECMPermission);
    UIQueriesForm uiForm = findFirstComponentOfType(UIQueriesForm.class) ;
    uiECMPermission.setComponent(uiForm, new String[] {UIQueriesForm.PERMISSIONS}) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
}
