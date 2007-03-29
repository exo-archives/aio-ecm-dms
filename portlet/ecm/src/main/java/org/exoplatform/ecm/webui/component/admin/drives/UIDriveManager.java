/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 11:45:11 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIDriveManager extends UIContainer {
  
  public UIDriveManager() throws Exception {
    addChild(UIDriveList.class, null, null) ;
  }
  
  public void initPopup(String id) throws Exception {
    UIDriveForm uiDriveForm ;
    UIPopupWindow uiPopup = getChildById(id) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, id) ;
      uiPopup.setWindowSize(560,400) ;      
      uiDriveForm = createUIComponent(UIDriveForm.class, null, null) ;
    } else {
      uiDriveForm = uiPopup.findFirstComponentOfType(UIDriveForm.class) ;
      uiPopup.setRendered(true) ;
    }
    uiPopup.setUIComponent(uiDriveForm) ;
    uiPopup.setShow(true) ;
  }
  
  public void initPopupPermission() throws Exception {
    removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, UIDriveForm.POPUP_DRIVEPERMISSION);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = 
      createUIComponent(UIECMPermissionBrowser.class, null, null) ;
    uiPopup.setUIComponent(uiECMPermission);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiECMPermission.setComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_PERMISSION}) ;
    uiPopup.setShow(true) ;
  }
  
  public void initPopupJCRBrowser() throws Exception {
    CmsConfigurationService cmsService = getApplicationComponent(CmsConfigurationService.class) ;
    removeChildById("JCRBrowser") ;
    removeChildById("JCRBrowserAssets") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "JCRBrowser");
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setWorkspace(cmsService.getWorkspace()) ;
    uiPopup.setUIComponent(uiJCRBrowser);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiJCRBrowser.setComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_HOMEPATH}) ;
    uiPopup.setShow(true) ;
  }
  
  public void initPopupJCRBrowserAssets() throws Exception {
    removeChildById("JCRBrowserAssets") ;
    removeChildById("JCRBrowser") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "JCRBrowserAssets");
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiPopup.setUIComponent(uiJCRBrowser);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiJCRBrowser.setWorkspace("digital-assets") ;
    uiJCRBrowser.setRootPath("/") ;
    uiJCRBrowser.setFilterType(new String[] {Utils.NT_FILE}) ;
    uiJCRBrowser.setComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_WORKSPACEICON}) ;
    uiPopup.setShow(true) ;
  }
}
