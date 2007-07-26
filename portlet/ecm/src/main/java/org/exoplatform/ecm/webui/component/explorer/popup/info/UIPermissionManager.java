/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : TrongTT
 *          TrongTT@exoplatform.com
 * Sep 13, 2006
 * Editor : TuanP
 *        phamtuanchip@yahoo.de   
 * Oct 13, 2006
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIPermissionManager extends UIContainer implements UIPopupComponent{
  public UIPermissionManager() throws Exception {
    addChild(UIPermissionInfo.class, null, null);    
    addChild(UIPermissionForm.class, null, null);    
  }
  public void initPopupPermission(UIComponent uiSelector) throws Exception {
    UIPopupWindow uiPopup = getChildById(UIPermissionForm.POPUP_SELECT) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, UIPermissionForm.POPUP_SELECT);
      uiPopup.setWindowSize(560, 300);
    } else {
      uiPopup.setRendered(true) ;
    }
    uiPopup.setUIComponent(uiSelector);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  public void activate() throws Exception {
    getChild(UIPermissionInfo.class).updateGrid() ;
  }
  public void checkPermissonInfo(Node node) throws Exception {
    if(node.isLocked()){
      if(!Utils.isLockTokenHolder(node)) {
        getChild(UIPermissionInfo.class).getChild(UIGrid.class).configure("usersOrGroups", UIPermissionInfo.PERMISSION_BEAN_FIELD, new String[]{}) ;
        getChild(UIPermissionForm.class).setRendered(false) ;
      }
    } else {
      if(!Utils.hasChangePermissionRight(node)) {
        getChild(UIPermissionInfo.class).getChild(UIGrid.class).configure("usersOrGroups", UIPermissionInfo.PERMISSION_BEAN_FIELD, new String[]{}) ;
        getChild(UIPermissionForm.class).setRendered(false) ;
      }
    }
  }
  public void deActivate() throws Exception {} 
}
