/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.ecm.webui.component.UIPopupAction;
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
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupWizard");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowInWizard") ;
  }
  protected void initWizard(boolean isAddnewRepo, boolean isAddNewWizard, WorkspaceEntry ws) throws Exception {
    getChild(UIWorkspaceWizard.class).isNewWizard_ = isAddNewWizard ;
    getChild(UIWorkspaceWizard.class).isNewRepo_ = isAddnewRepo ;
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
