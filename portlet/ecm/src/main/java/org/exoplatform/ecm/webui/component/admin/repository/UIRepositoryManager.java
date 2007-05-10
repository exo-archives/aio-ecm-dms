/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 9, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIRepositoryManager extends UIContainer {
  public UIRepositoryManager() throws Exception {
    addChild(UIRepositoryList.class, null, null) ;    
  }

  public void initPopup(String popupId ,UIComponent uiForm) throws Exception {
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setWindowSize(560,400) ;      
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public void initPopupPermission(String popupId, UIRepositoryForm uiForm) throws Exception {
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = createUIComponent(UIECMPermissionBrowser.class, null, null) ;
    uiECMPermission.setComponent(uiForm, null) ;
    uiPopup.setUIComponent(uiECMPermission);
    uiPopup.setShow(true) ;
    uiPopup.setRendered(true) ;
    uiPopup.setResizable(true) ;
  }
}
