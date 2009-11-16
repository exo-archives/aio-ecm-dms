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
package org.exoplatform.ecm.webui.component.admin.unlock;

import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.selector.UIPermissionSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
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
public class UIUnLockManager extends UIAbstractManager {

  public UIUnLockManager() throws Exception {
    addChild(UILockList.class, null, null);
  }
  
  public void refresh() throws Exception {
    update();
  }
  
  public void update() throws Exception {
    getChild(UILockList.class).updateLockedNodesGrid(1);
  }
  public void initFormPopup(String id) throws Exception {
    removeChildById(id);
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, id);
    uiPopup.setWindowSize(600, 500);
    UIUnLockForm uiForm = createUIComponent(UIUnLockForm.class, null, null);
    uiPopup.setUIComponent(uiForm);
    uiPopup.setRendered(true);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
  }
  
  public void initPermissionPopup(String membership) throws Exception {
    removeChildById("PermissionPopup");
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PermissionPopup");
    uiPopup.setWindowSize(560, 300);
    UIPermissionSelector uiECMPermission = 
      createUIComponent(UIPermissionSelector.class, null, "QueriesPermissionBrowse");
    uiECMPermission.setSelectedMembership(true);
    if(membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/");
      uiECMPermission.setCurrentPermission("/" + arrMember[1]);
    }
    uiPopup.setUIComponent(uiECMPermission);
    UIUnLockForm uiForm = findFirstComponentOfType(UIUnLockForm.class);
    uiECMPermission.setSourceComponent(uiForm, new String[] {UIUnLockForm.PERMISSIONS});
    uiPopup.setRendered(true);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
  }
}
