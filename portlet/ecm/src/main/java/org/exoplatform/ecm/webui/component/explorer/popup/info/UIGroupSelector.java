/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.organization.webui.component.UIGroupMembershipSelector;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.UserImpl;
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
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 7, 2006 8:31:56 AM 
 */
@ComponentConfigs({ 
  @ComponentConfig(
      template = "app:/groovy/webui/component/explorer/popup/info/UIGroupSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIGroupSelector.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectPathActionListener.class)  
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

public class UIGroupSelector extends UIGroupMembershipSelector implements ComponentSelector {

  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  private boolean isSelectGroup_ = false ;
  private boolean isSelectMember_ = false ;
  private boolean isSelectUSer_ = false ;

  public UIGroupSelector() throws Exception {}

  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }

  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }

  public void setSelectGroup(boolean isSelect) { isSelectGroup_ = isSelect ;}
  public void setSelectMember(boolean isSelect) { isSelectMember_ = isSelect ;}
  public void setSelectUser(boolean isSelect) { isSelectUSer_ = isSelect ;}
  
  public boolean isSelectGroup() {return isSelectGroup_ ;}
  public boolean isSelectMember() {return isSelectMember_ ;}
  public boolean isSelectUser() {return isSelectUSer_ ;}
  
  private void setDefaultValue() {
    isSelectGroup_ = false ;
    isSelectMember_ = false ;
    isSelectUSer_ = false ;
  }
  
  @SuppressWarnings({ "unchecked", "cast" })
  public List getChildGroup() throws Exception {
    List children = new ArrayList() ;    
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    for (Object child : service.getGroupHandler().findGroups(this.getCurrentGroup())) {
      children.add((GroupImpl)child) ;
    }
    return children ;
  }
  
  @SuppressWarnings({ "unchecked", "cast" })
  public List getUsers() throws Exception {
    List children = new ArrayList() ; 
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    PageList userPageList = service.getUserHandler().findUsersByGroup(this.getCurrentGroup().getId()) ;    
    for(Object child : userPageList.getAll()){
      children.add((UserImpl)child) ;
    }
    return children ;
  }

  static  public class SelectMembershipActionListener extends EventListener<UIGroupSelector> {   
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource();
      if(uiGroupSelector.getCurrentGroup() == null) return ;
      String groupId = uiGroupSelector.getCurrentGroup().getId() ;
      String value = "" ;
      String permission = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(uiGroupSelector.isSelectMember_) value = permission + ":" + groupId ;
      else value = permission ;
      String returnField = uiGroupSelector.getReturnField() ;
      ((UISelector)uiGroupSelector.getReturnComponent()).updateSelect(returnField, value) ;
      UIPopupWindow uiPopup = uiGroupSelector.getParent() ;
      uiGroupSelector.setDefaultValue() ;
      uiPopup.setShow(false) ;
    }
  }

  static  public class ChangeNodeActionListener extends EventListener<UITree> {   
    public void execute(Event<UITree> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getAncestorOfType(UIGroupSelector.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiGroupSelector.changeGroup(groupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector) ;
    }
  }

  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource() ;
      UIGroupSelector uiGroupSelector = uiBreadcumbs.getParent() ;
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiBreadcumbs.setSelectPath(objectId);     
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId() ;
      uiGroupSelector.changeGroup(selectGroupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector) ;
    }
  }
}
