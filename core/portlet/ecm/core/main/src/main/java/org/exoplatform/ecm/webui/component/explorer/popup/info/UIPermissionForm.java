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
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIDrivesBrowserContainer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.selector.UIGroupMemberSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
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
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", 
    events = {
      @EventConfig(listeners = UIPermissionForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.ResetActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.CloseActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectUserActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.SelectMemberActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPermissionForm.AddAnyActionListener.class)
    }
)

public class UIPermissionForm extends UIForm implements UISelectable {
  
  final static public String PERMISSION   = "permission";

  final static public String POPUP_SELECT = "SelectUserOrGroup";

  private Node               currentNode;
  
  public UIPermissionForm() throws Exception {
    addChild(new UIPermissionInputSet(PERMISSION));
    setActions(new String[] { "Save", "Reset", "Close" });
  }

  private void refresh() {
    reset();
    checkAll(false);
  }

  private void checkAll(boolean check) {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION);
    for (String perm : PermissionType.ALL) {
      uiInputSet.getUIFormCheckBoxInput(perm).setChecked(check);
    }
  }

  protected boolean isEditable(Node node) throws Exception {
    return PermissionUtil.canChangePermission(node);
  }
  public void fillForm(String user, ExtendedNode node) throws Exception {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION);
    refresh();
    uiInputSet.getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).setValue(user);
    if(user.equals(Utils.getNodeOwner(node))) {
      for (String perm : PermissionType.ALL) { 
        uiInputSet.getUIFormCheckBoxInput(perm).setChecked(true);
      }
    } else {
      List<AccessControlEntry> permsList = node.getACL().getPermissionEntries();
      Iterator<AccessControlEntry> perIter = permsList.iterator();
      StringBuilder userPermission = new StringBuilder();
      while(perIter.hasNext()) {
        AccessControlEntry accessControlEntry = perIter.next();
        if(user.equals(accessControlEntry.getIdentity())) {
          userPermission.append(accessControlEntry.getPermission()).append(" ");
        }
      }
      for (String perm : PermissionType.ALL) { 
        boolean isCheck = userPermission.toString().contains(perm);
        uiInputSet.getUIFormCheckBoxInput(perm).setChecked(isCheck);
      }   
    }

  }
  protected void lockForm(boolean isLock) {
    UIPermissionInputSet uiInputSet = getChildById(PERMISSION);
    if(isLock) {
      setActions(new String[] {"Reset", "Close" });
      uiInputSet.setActionInfo(UIPermissionInputSet.FIELD_USERORGROUP, null);
    } else {
      setActions(new String[] { "Save", "Reset", "Close" });
      uiInputSet.setActionInfo(UIPermissionInputSet.FIELD_USERORGROUP, new String[] {"SelectUser", "SelectMember", "AddAny"});
    }
    for (String perm : PermissionType.ALL) { 
      uiInputSet.getUIFormCheckBoxInput(perm).setEnable(!isLock);
    }
  }

  private String  getExoOwner(Node node) throws Exception {
    return Utils.getNodeOwner(node);
  }
  
  public void doSelect(String selectField, Object value) {
    try {
      ExtendedNode node = (ExtendedNode)getAncestorOfType(UIJCRExplorer.class).getCurrentNode();
      checkAll(false);
      fillForm(value.toString(), node);
      lockForm(value.toString().equals(getExoOwner(node)));
      getUIStringInput(selectField).setValue(value.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private boolean hasMembership(String userId, String roleExpression) {
    if (roleExpression.indexOf(":/") < 0) return false;
    if("*".equals(roleExpression))
      return true;
    OrganizationService organizationService_ = getApplicationComponent(OrganizationService.class);
    String membershipType = roleExpression.substring(0, roleExpression.indexOf(":"));
    String groupName = roleExpression.substring(roleExpression.indexOf(":") + 1);
    try {
      MembershipHandler membershipHandler = organizationService_.getMembershipHandler();
      if ("*".equals(membershipType)) {
        // Determine if there exists at least one membership
        return !membershipHandler.findMembershipsByUserAndGroup(userId,groupName).isEmpty();
      } 
      // Determine if there exists the membership of specified type
      return membershipHandler.findMembershipByUserGroupAndType(userId,groupName,membershipType) != null;      
    }
    catch(Exception e) {            
    }  
    return false;
  }   
  
  static public class ResetActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      uiForm.lockForm(false);
      uiForm.refresh();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }
  static public class SaveActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      
      UIPermissionManager uiParent = uiForm.getParent();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      String userOrGroup = uiForm.getChild(UIPermissionInputSet.class).getUIStringInput(
          UIPermissionInputSet.FIELD_USERORGROUP).getValue();
      List<String> permsList = new ArrayList<String>();
      List<String> permsRemoveList = new ArrayList<String>();
      uiExplorer.addLockToken(currentNode);
      if (!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      for (String perm : PermissionType.ALL) {
        if (uiForm.getUIFormCheckBoxInput(perm).isChecked()) permsList.add(perm);
        else {
          if (uiForm.hasMembership(currentNode.getSession().getUserID(), userOrGroup)) {
            if (perm.equals(PermissionType.SET_PROPERTY)) {
              uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.allow-setProperty", null, 
                  ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
          } 
          permsRemoveList.add(perm);
        }
      }
      if (uiForm.getUIFormCheckBoxInput(PermissionType.ADD_NODE).isChecked() ||
          uiForm.getUIFormCheckBoxInput(PermissionType.REMOVE).isChecked() || 
          uiForm.getUIFormCheckBoxInput(PermissionType.SET_PROPERTY).isChecked())
      {
        if(!permsList.contains(PermissionType.READ))
          permsList.add(PermissionType.READ);
      }

      if (Utils.isNameEmpty(userOrGroup)) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.userOrGroup-required", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (permsList.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.checkbox-require", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] permsArray = permsList.toArray(new String[permsList.size()]);
      ExtendedNode node = (ExtendedNode) currentNode;
      if (PermissionUtil.canChangePermission(node)) {
        if (node.canAddMixin("exo:privilegeable")){
          node.addMixin("exo:privilegeable");
          node.setPermission(Utils.getNodeOwner(node),PermissionType.ALL);
        }
        for (String perm : permsRemoveList) {
          try {
            node.removePermission(userOrGroup, perm);
          } catch (AccessDeniedException ade) {
            uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.access-denied", null, 
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        } 
        if(PermissionUtil.canChangePermission(node)) node.setPermission(userOrGroup, permsArray);
        uiParent.getChild(UIPermissionInfo.class).updateGrid(1);
        node.save();
        if (uiExplorer.getRootNode().equals(node)) {
          if (!PermissionUtil.canRead(currentNode)) {
            uiExplorer.setRenderSibbling(UIDrivesBrowserContainer.class);
            return;
          }
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UIPermissionForm.msg.not-change-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      Node realNode = uiExplorer.getRealCurrentNode();
      LinkManager linkManager = uiExplorer.getApplicationComponent(LinkManager.class);
      if (linkManager.isLink(realNode)) {
        // Reset the permissions
        linkManager.updateLink(realNode, currentNode);
      }
      currentNode.getSession().save();
      uiForm.refresh();
      uiExplorer.setIsHidePopup(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiParent);
      uiExplorer.updateAjax(event);
    }
  }

  static public class SelectUserActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      ((UIPermissionManager)uiForm.getParent()).initUserSelector();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class AddAnyActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIPermissionInputSet uiInputSet = uiForm.getChildById(UIPermissionForm.PERMISSION);
      uiInputSet.getUIStringInput(UIPermissionInputSet.FIELD_USERORGROUP).setValue(
          SystemIdentity.ANY);
      uiForm.checkAll(false);
      uiInputSet.getUIFormCheckBoxInput(PermissionType.READ).setChecked(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class SelectMemberActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIPermissionForm uiForm = event.getSource();
      UIGroupMemberSelector uiGroupMemberSelector = uiForm.createUIComponent(UIGroupMemberSelector.class, null, null);
      uiGroupMemberSelector.setSourceComponent(uiForm, new String[] { UIPermissionInputSet.FIELD_USERORGROUP });
      uiForm.getAncestorOfType(UIPermissionManager.class).initPopupPermission(uiGroupMemberSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class CloseActionListener extends EventListener<UIPermissionForm> {
    public void execute(Event<UIPermissionForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

  public Node getCurrentNode() {
    return currentNode;
  }

  public void setCurrentNode(Node currentNode) {
    this.currentNode = currentNode;
  }
}