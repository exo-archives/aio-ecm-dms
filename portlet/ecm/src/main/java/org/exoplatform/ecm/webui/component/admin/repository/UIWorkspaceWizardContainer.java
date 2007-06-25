/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 25-06-2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIWorkspaceWizardContainer extends UIContainer implements UIPopupComponent {

  public UIWorkspaceWizardContainer() throws Exception {
    addChild(UIWorkspaceWizard.class, null, null) ;
    //UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupWizard");
    //uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowInWizard") ;
  }
  protected void initWizard(boolean isAddnew, WorkspaceEntry ws) throws Exception {
    getChild(UIWorkspaceWizard.class).isNewWizard_ = isAddnew ;
    getChild(UIWorkspaceWizard.class).refresh(ws) ;
  }
  protected void initPopupPermission(String id, String membership, UIComponent comp) throws Exception {
    UIPopupWindow uiPopup = getChildById(id) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, id);
      uiPopup.setWindowSize(560, 300);
      UIECMPermissionBrowser uiECMPermission = 
        createUIComponent(UIECMPermissionBrowser.class, null, null) ;
      if(membership != null && membership.indexOf(":/") > -1) {
        String[] arrMember = membership.split(":/") ;
        uiECMPermission.setCurrentPermission("/" + arrMember[1]) ;
      }
      uiECMPermission.setComponent(comp, null) ;
      uiPopup.setUIComponent(uiECMPermission);
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  protected void removePopup(String id) {
    removeChildById(id) ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }

}
