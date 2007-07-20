/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
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
 * Created by The eXo Platform SARL Author : nqhungvn
 * nguyenkequanghung@yahoo.com July 3, 2006 10:07:15 AM Editor : tuanp
 * phamtuanchip@yahoo.de Oct 13, 2006
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
  @EventConfig(listeners = UIPermissionForm.SaveActionListener.class),
  @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.ResetActionListener.class),
  @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.CancelActionListener.class),
  @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectUserActionListener.class),
  @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectMemberActionListener.class),
  @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.AddAnyActionListener.class) })

  public class UIPermissionForm extends UIForm implements UISelector {
  final static public String PERMISSION   = "permission";
  final static public String POPUP_SELECT = "SelectUserOrGroup";
  public UIPermissionForm() throws Exception {
    addChild(new UIPermissionInputSet(PERMISSION));
    setActions(new String[] { "Save", "Reset", "Cancel" });
  }

  private void refresh() {
    reset();
    checkAll(false);
  }

  private void checkAll(boolean check) {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION) ;
    for (String perm : PermissionType.ALL) {
      uiInputSet.getUIFormCheckBoxInput(perm).setChecked(check);
    }
  }

  protected boolean isEditable(Node node) throws Exception {
    return Utils.isChangePermissionAuthorized(node);
  }
  public void fillForm(String user, ExtendedNode node) throws Exception {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION) ;
    refresh() ;
    uiInputSet.getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).setValue(user) ;
    if(user.equals(Utils.getNodeOwner(node))) {
      for (String perm : PermissionType.ALL) { 
        uiInputSet.getUIFormCheckBoxInput(perm).setChecked(true) ;
      }
    } else {
      List<AccessControlEntry> permsList = node.getACL().getPermissionEntries() ;
      Iterator perIter = permsList.iterator() ;
      StringBuilder userPermission = new StringBuilder() ;
      while(perIter.hasNext()) {
        AccessControlEntry accessControlEntry = (AccessControlEntry)perIter.next() ;
        if(user.equals(accessControlEntry.getIdentity())) {
          userPermission.append(accessControlEntry.getPermission()).append(" ");
        }
      }
      for (String perm : PermissionType.ALL) { 
        boolean isCheck = userPermission.toString().contains(perm) ;
        uiInputSet.getUIFormCheckBoxInput(perm).setChecked(isCheck) ;
      }   
    }

  }
  protected void lockForm(boolean isLock) {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION) ;
    if(isLock) {
      setActions(new String[] {"Reset", "Cancel" });
      uiInputSet.setActionInfo(UIPermissionInputSet.FIELD_USERORGROUP, null) ;
    } else {
      setActions(new String[] { "Save", "Reset", "Cancel" });
      uiInputSet.setActionInfo(UIPermissionInputSet.FIELD_USERORGROUP, new String[] {"SelectUser", "SelectMember", "AddAny"}) ;
    }
    //uiInputSet.getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).setEditable(!isLock) ;
    for (String perm : PermissionType.ALL) { 
      uiInputSet.getUIFormCheckBoxInput(perm).setEnable(!isLock) ;
    }
  }

  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value);
    checkAll(false);
  }
  static public class ResetActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      uiForm.lockForm(false) ;
      uiForm.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }
  static public class SaveActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIPermissionManager uiParent = uiForm.getParent();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      String userOrGroup = uiForm.getChild(UIPermissionInputSet.class).getUIStringInput(
          UIPermissionInputSet.FIELD_USERORGROUP).getValue();
      List<String> permsList = new ArrayList<String>();
      List<String> permsRemoveList = new ArrayList<String>();
      for (String perm : PermissionType.ALL) {
        if (uiForm.getUIFormCheckBoxInput(perm).isChecked())
          permsList.add(perm);
        else
          permsRemoveList.add(perm);
      }
      if(uiForm.getUIFormCheckBoxInput(PermissionType.ADD_NODE).isChecked() ||
          uiForm.getUIFormCheckBoxInput(PermissionType.REMOVE).isChecked() || 
          uiForm.getUIFormCheckBoxInput(PermissionType.SET_PROPERTY).isChecked())
      {
        if(!permsList.contains(PermissionType.READ))
          permsList.add(PermissionType.READ) ;
      }

      if (userOrGroup == null || userOrGroup.length() < 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.userOrGroup-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permsList.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.checkbox-require", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] permsArray = permsList.toArray(new String[permsList.size()]);
      UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      ExtendedNode node = (ExtendedNode) uiJCRExplorer.getCurrentNode();
      if (!Utils.isChangePermissionAuthorized(node)) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.not-change-permission", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (node.canAddMixin("exo:privilegeable")) node.addMixin("exo:privilegeable");
      try {
        for (String perm : permsRemoveList) {
          node.removePermission(userOrGroup, perm);
        } 
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
      try {
        node.setPermission(userOrGroup, permsArray);
        uiParent.getChild(UIPermissionInfo.class).updateGrid();
        node.save();
        if (!uiJCRExplorer.getPreference().isJcrEnable()) {
          uiJCRExplorer.getSession().save();
        }
        uiForm.refresh();
        uiJCRExplorer.setIsHidePopup(true);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParent);
        uiJCRExplorer.updateAjax(event);
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static public class SelectUserActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setSelectUser(true);
      uiGroupSelector.setComponent(uiForm, new String[] { UIPermissionInputSet.FIELD_USERORGROUP });
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class AddAnyActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIPermissionInputSet uiInputSet = uiForm.getChildById(UIPermissionForm.PERMISSION);
      uiInputSet.getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).setValue(
          SystemIdentity.ANY);
      uiForm.checkAll(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class SelectMemberActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIECMPermissionBrowser uiMemberSelect = uiForm.createUIComponent(
          UIECMPermissionBrowser.class, null, null);
      uiMemberSelect.setComponent(uiForm, new String[] { UIPermissionInputSet.FIELD_USERORGROUP });
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiMemberSelect);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class CancelActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }
}
