/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : tuanp
 *        phamtuanchip@yahoo.de
 * Oct 13, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIPermissionForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectUserActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectGroupActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectMemberActionListener.class)
    }
)

public class UIPermissionForm extends UIForm implements UISelector {

  final static public String PERMISSION = "permission" ;
  final static public String POPUP_SELECT = "SelectUserOrGroup" ;

  public UIPermissionForm() throws Exception {
    addChild(new UIPermissionInputSet(PERMISSION)) ;
    setActions(new String[]{"Save", "Cancel"}) ;
  }

  private void refresh() {
    reset() ;
    for(String perm : PermissionType.ALL) {
      getUIFormCheckBoxInput(perm).setChecked(false) ;
    }    
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
  }

  static  public class SaveActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIPermissionManager uiParent = uiForm.getParent() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String userOrGroup = uiForm.getChild(UIPermissionInputSet.class).
                           getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).getValue() ;
      List<String> permsList = new ArrayList<String>() ;
      for(String perm : PermissionType.ALL) {
        if(uiForm.getUIFormCheckBoxInput(perm).isChecked()) permsList.add(perm) ;
      }
      if(userOrGroup == null || userOrGroup.length() < 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.userOrGroup-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      if(permsList.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.checkbox-require", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      String[] permsArray = permsList.toArray(new String[permsList.size()]) ;      
      try {
        UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        ExtendedNode node = (ExtendedNode)uiJCRExplorer.getCurrentNode() ;
        if(node.canAddMixin("exo:privilegeable")) node.addMixin("exo:privilegeable");
        node.setPermission(userOrGroup, permsArray) ;
        uiParent.getChild(UIPermissionInfo.class).updateGrid() ;
        node.save() ;
        if(!uiJCRExplorer.getPreference().isJcrEnable()) {
          uiJCRExplorer.getSession().save() ;
        }
        uiForm.refresh() ;
        uiJCRExplorer.setIsHidePopup(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParent) ;
        uiJCRExplorer.updateAjax(event) ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
      }   
    }
  }

  static  public class SelectUserActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm  uiForm = event.getSource() ;
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null) ;
      uiGroupSelector.setSelectUser(true) ;
      uiGroupSelector.setComponent(uiForm, new String[] {UIPermissionInputSet.FIELD_USERORGROUP});
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupSelector) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static  public class SelectGroupActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm  uiForm = event.getSource() ;
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null) ;
      uiGroupSelector.setSelectGroup(true) ;
      uiGroupSelector.setComponent(uiForm, new String[] {UIPermissionInputSet.FIELD_USERORGROUP});
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupSelector) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static  public class SelectMemberActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm  uiForm = event.getSource() ;
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null) ;
      uiGroupSelector.setSelectMember(true) ;
      uiGroupSelector.setComponent(uiForm, new String[] {UIPermissionInputSet.FIELD_USERORGROUP});
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupSelector) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}

