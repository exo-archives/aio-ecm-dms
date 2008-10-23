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
package org.exoplatform.ecm.webui.selector;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh
 * minh.dang@exoplatform.com Nov 17, 2006
 */
@ComponentConfigs( {
    @ComponentConfig(
        template = "classpath:groovy/ecm/webui/UIMemberSelector.gtmpl", 
        events = {
          @EventConfig(listeners = UIPermissionSelector.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIPermissionSelector.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIPermissionSelector.SelectPathActionListener.class) 
        }),
    @ComponentConfig(
        type = UITree.class, 
        id = "UITreeGroupSelector", 
        template = "system:/groovy/webui/core/UITree.gtmpl", 
        events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)),
    @ComponentConfig(
        type = UIBreadcumbs.class, 
        id = "BreadcumbGroupSelector", 
        template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl", 
        events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)) 
    }
)

public class UIPermissionSelector extends UIGroupMembershipSelector implements ComponentSelector {

  /** The Constant defaultValue. */
  final static public String defaultValue    = "/admin";

  /** The ui component. */
  private UIComponent        uiComponent;

  /** The return field name. */
  private String             returnFieldName = null;

  /** The is selected membership. */
  private boolean            isSelectedMembership = true;

  /** The is selected user. */
  private boolean            isSelectedUser;

  /** The is use popup. */
  private boolean             isUsePopup      = true;

  /**
   * Instantiates a new uI permission selector.
   * 
   * @throws Exception the exception
   */
  public UIPermissionSelector() throws Exception {
    changeGroup(defaultValue);
    addChild(UIAnyPermission.class, null, null);
  }

  /**
   * Sets the current permission.
   * 
   * @param per the new current permission
   * 
   * @throws Exception the exception
   */
  public void setCurrentPermission(String per) throws Exception {
    changeGroup(per);
  }

  /**
   * Gets the return component.
   * 
   * @return the return component
   */

  public UIComponent getSourceComponent() {
    return uiComponent;
  }

  /**
   * Gets the return field.
   * 
   * @return the return field
   */
  public String getReturnField() {
    return returnFieldName;
  }
  
  public void setIsUsePopup(boolean isUsePopup) { this.isUsePopup = isUsePopup; }
  
  public boolean isUsePopup() { return isUsePopup; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.ComponentSelector#setComponent(org.exoplatform.webui.core.UIComponent, java.lang.String[])
   */
  public void setSourceComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent;
    if (initParams == null || initParams.length == 0)
      return;
    for (int i = 0; i < initParams.length; i++) {
      if (initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=");
        returnFieldName = array[1];
        break;
      }
      returnFieldName = initParams[0];
    }
  }
  
  /**
   * The listener interface for receiving selectMembershipAction events. The
   * class that is interested in processing a selectMembershipAction event
   * implements this interface, and the object created with that class is
   * registered with a component using the component's
   * <code>addSelectMembershipActionListener<code> method. When
   * the selectMembershipAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectMembershipActionEvent
   */
  static public class SelectMembershipActionListener extends EventListener<UIPermissionSelector> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPermissionSelector> event) throws Exception {
      UIPermissionSelector uiPermissionSelector = event.getSource();
      if (uiPermissionSelector.getCurrentGroup() == null)
        return;
      String groupId = uiPermissionSelector.getCurrentGroup().getId();
      String permission = event.getRequestContext().getRequestParameter(OBJECTID);
      String value = "";
      if(uiPermissionSelector.isSelectedUser()) {
        value = permission;
      } else {
        value = permission + ":" + groupId;
      }
      String returnField = uiPermissionSelector.getReturnField();
      ((UISelectable) uiPermissionSelector.getSourceComponent()).doSelect(returnField, value);
      if (uiPermissionSelector.isUsePopup) {
        UIPopupWindow uiPopup = uiPermissionSelector.getParent();
        uiPopup.setShow(false);
        UIComponent uicomp = uiPermissionSelector.getSourceComponent().getParent();
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);
        if (!uiPopup.getId().equals("PopupComponent"))
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiPermissionSelector.getSourceComponent());
      }
    }
  }

  /**
   * The listener interface for receiving changeNodeAction events. The class
   * that is interested in processing a changeNodeAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addChangeNodeActionListener<code> method. When
   * the changeNodeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangeNodeActionEvent
   */
  static public class ChangeNodeActionListener extends EventListener<UITree> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UITree> event) throws Exception {
      UIPermissionSelector uiPermissionSelector = event.getSource().getParent();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiPermissionSelector.changeGroup(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector);
    }
  }

  /**
   * The listener interface for receiving selectPathAction events. The class
   * that is interested in processing a selectPathAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addSelectPathActionListener<code> method. When
   * the selectPathAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectPathActionEvent
   */
  static public class SelectPathActionListener extends EventListener<UIBreadcumbs> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      UIPermissionSelector uiPermissionSelector = uiBreadcumbs.getParent();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiPermissionSelector.changeGroup(selectGroupId);
      if (uiPermissionSelector.isUsePopup) {
        UIPopupWindow uiPopup = uiBreadcumbs.getAncestorOfType(UIPopupWindow.class);
        uiPopup.setShow(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector);
    }
  }

  /**
   * Sets the selected user.
   * 
   * @param bool the new selected user
   */
  public void setSelectedUser(boolean bool) {
    isSelectedUser = bool;
  }

  /**
   * Checks if is selected user.
   * 
   * @return true, if is selected user
   */
  public boolean isSelectedUser() {
    return isSelectedUser;
  }

  /**
   * Sets the selected membership.
   * 
   * @param bool the new selected membership
   */
  public void setSelectedMembership(boolean bool) {
    isSelectedMembership = bool;
  }

  /**
   * Checks if is selected membership.
   * 
   * @return true, if is selected membership
   */
  public boolean isSelectedMembership() {
    return isSelectedMembership;
  }

  /**
   * Gets the users.
   * 
   * @return the users
   * 
   * @throws Exception the exception
   */
  public List getUsers() throws Exception {
    List<User> children = new ArrayList<User>();
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    PageList userPageList = service.getUserHandler().findUsersByGroup(
        this.getCurrentGroup().getId());
    for (Object child : userPageList.getAll()) {
      children.add((User) child);
    }
    return children;
  }

}
