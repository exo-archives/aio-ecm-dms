/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 02-07-2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIWorkspacePermissionForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIWorkspacePermissionForm.CancelActionListener.class)
    }
)
public class UIWorkspacePermissionForm extends UIForm implements UISelector {
  final static public String FIELD_PERMISSION = "permission" ;

  public UIWorkspacePermissionForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION, null).addValidator(EmptyFieldValidator.class).setEditable(false)) ;
    for (String perm : PermissionType.ALL) {
      addUIFormInput(new UIFormCheckBoxInput<String>(perm, perm, null)) ;
    }

  }
  protected boolean isCheckedAny() {
    for(String perm : PermissionType.ALL) {
      if(getUIFormCheckBoxInput(perm).isChecked()) return true ;
    }
    return false ;
  }
  public void updateSelect(String selectField, String value) {
    getUIStringInput(FIELD_PERMISSION).setValue(value) ;

  }
  
  public void reset() {
    getUIStringInput(FIELD_PERMISSION).setValue(null) ;
    for(String perm : PermissionType.ALL) {
      getUIFormCheckBoxInput(perm).setChecked(false) ;
    }
  }
  
  public static class SaveActionListener extends EventListener<UIWorkspacePermissionForm> {
    public void execute (Event<UIWorkspacePermissionForm> event) throws Exception {
      UIWorkspacePermissionForm uiForm =  event.getSource();
      UIWorkspaceWizardContainer uiWizardContainer = uiForm.getAncestorOfType(UIWorkspaceWizardContainer.class) ;
      UIWorkspaceWizard uiWizardForm = uiWizardContainer.getChild(UIWorkspaceWizard.class) ;
      String user = uiForm.getUIStringInput(UIWorkspacePermissionForm.FIELD_PERMISSION).getValue() ;
      if(!uiForm.isCheckedAny()) {
         UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
         uiApp.addMessage(new ApplicationMessage("UIWorkspacePermissionForm.msg.check-one", null)) ;
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
         return ;
      }
      StringBuilder sb = new StringBuilder() ;
      for(String perm : PermissionType.ALL) {
        if(uiForm.getUIFormCheckBoxInput(perm).isChecked()) sb.append(user +" "+ perm + ";") ;
      }
      uiWizardForm.permissions_.put(user, sb.toString()) ;
      uiWizardForm.refreshPermissionList() ;
      UIPopupAction uiPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardForm) ;
    }

  }

  public static class CancelActionListener extends EventListener<UIWorkspacePermissionForm> {
    public void execute(Event<UIWorkspacePermissionForm> event) throws Exception {
      UIPopupAction uiPopup = event.getSource().getAncestorOfType(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }

}
