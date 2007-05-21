/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.organization.webui.component.UIGroupMembershipSelector;
import org.exoplatform.webui.component.UIBreadcumbs;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.UITree;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 17, 2006  
 */
@ComponentConfigs({ 
  @ComponentConfig(
      template = "system:/groovy/organization/webui/component/UIGroupMembershipSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIECMPermissionBrowser.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIECMPermissionBrowser.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIECMPermissionBrowser.SelectPathActionListener.class)  
      }  
  ),
  @ComponentConfig(
      type = UITree.class, id = "UITreeGroupSelector",
      template = "system:/groovy/webui/component/UITree.gtmpl",
      events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class, id = "BreadcumbGroupSelector",
      template = "system:/groovy/webui/component/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  )
})
public class UIECMPermissionBrowser extends UIGroupMembershipSelector implements ComponentSelector {

  final static public String defaultValue = "/admin" ;
  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  
  public UIECMPermissionBrowser() throws Exception {
    changeGroup(defaultValue) ;
  }
  
  public void setCurrentPermission(String per) throws Exception { changeGroup(per) ; }
  
  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }

  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length == 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }
  
  static  public class SelectMembershipActionListener extends EventListener<UIECMPermissionBrowser> {   
    public void execute(Event<UIECMPermissionBrowser> event) throws Exception {
      UIECMPermissionBrowser uiPermissionSelector = event.getSource();
      if(uiPermissionSelector.getCurrentGroup() == null) return ;
      String groupId = uiPermissionSelector.getCurrentGroup().getId() ;
      String permission = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(permission.equalsIgnoreCase("any")) permission = "*" ;
      String value = permission + ":" + groupId ;
      String returnField = uiPermissionSelector.getReturnField() ;
      ((UISelector)uiPermissionSelector.getReturnComponent()).updateSelect(returnField, value) ;
      UIPopupWindow uiPopup = uiPermissionSelector.getParent() ;
      uiPopup.setShow(false) ;
      UIComponent uicomp = uiPermissionSelector.getReturnComponent().getParent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
      if(!uiPopup.getId().equals("PopupComponent"))event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class ChangeNodeActionListener extends EventListener<UITree> {   
    public void execute(Event<UITree> event) throws Exception {   
      UIECMPermissionBrowser uiPermissionSelector = event.getSource().getParent() ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiPermissionSelector.changeGroup(groupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector) ;
    }
  }
  
  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource() ;
      UIECMPermissionBrowser uiPermissionSelector = uiBreadcumbs.getParent() ;
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiBreadcumbs.setSelectPath(objectId);     
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId() ;
      uiPermissionSelector.changeGroup(selectGroupId) ;
      UIPopupWindow uiPopup = uiBreadcumbs.getAncestorOfType(UIPopupWindow.class) ;
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector) ;
    }
  }
}
