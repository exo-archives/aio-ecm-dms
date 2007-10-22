/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

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
  public void update()throws Exception  {
    getChild(UIDriveList.class).updateDriveListGrid() ;
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
    uiPopup.setResizable(true) ;
  }
  
  public void initPopupPermission(String membership) throws Exception {
    removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, UIDriveForm.POPUP_DRIVEPERMISSION);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = 
      createUIComponent(UIECMPermissionBrowser.class, null, null) ;
    if(membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/") ;
      uiECMPermission.setCurrentPermission("/" + arrMember[1]) ;
    }
    uiPopup.setUIComponent(uiECMPermission);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiECMPermission.setComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_PERMISSION}) ;
    uiPopup.setShow(true) ;
  }
  
  public void initPopupJCRBrowser(String workspace, boolean isDisable) throws Exception {
    removeChildById("JCRBrowser") ;
    removeChildById("JCRBrowserAssets") ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "JCRBrowser");
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setRepository(repository) ;
    uiJCRBrowser.setIsDisable(workspace, isDisable) ;
    uiJCRBrowser.setShowRootPathSelect(true) ;
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
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    uiJCRBrowser.setRepository(repository) ;
    uiJCRBrowser.setRootPath("/") ;
    uiJCRBrowser.setFilterType(new String[] {Utils.NT_FILE}) ;
    uiJCRBrowser.setMimeTypes(new String[] {"image/jpeg", "image/gif", "image/png"}) ;
    uiJCRBrowser.setComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_WORKSPACEICON}) ;
    uiPopup.setShow(true) ;
  }
}
