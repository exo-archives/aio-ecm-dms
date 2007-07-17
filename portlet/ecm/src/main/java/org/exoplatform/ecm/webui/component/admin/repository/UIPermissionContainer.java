/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 02-07-2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIPermissionContainer  extends UIContainer implements UIPopupComponent{

  public UIPermissionContainer() throws Exception {
    UIECMPermissionBrowser uiECMPermission = 
      addChild(UIECMPermissionBrowser.class, null, "PermissionPopupSelect") ;
    UIWorkspacePermissionForm uiWsPermissionForm = addChild(UIWorkspacePermissionForm.class, null, null) ;
    uiECMPermission.isUsePopup_ = false ;
    uiECMPermission.setComponent(uiWsPermissionForm, null) ;
  }

  protected void setValues(String user, String permission) {    
    UIWorkspacePermissionForm uiWsPremForm = getChild(UIWorkspacePermissionForm.class) ;
    uiWsPremForm.reset() ;
    UIFormStringInput permissionField =  uiWsPremForm.getUIStringInput(UIWorkspacePermissionForm.FIELD_PERMISSION) ;
    permissionField.setValue(user) ;
    if( permission != null) {
      for(String perm : PermissionType.ALL) {
        uiWsPremForm.getUIFormCheckBoxInput(perm).setChecked(permission.contains(perm)) ;
      }
    }
  }
  protected void lockForm(boolean lock) {
    getChild(UIWorkspacePermissionForm.class).lockForm(lock) ;
  }
  protected void disposePopup() {}

  public void activate() throws Exception {

  }

  public void deActivate() throws Exception {
    getChild(UIWorkspacePermissionForm.class).reset() ;
  }
}
