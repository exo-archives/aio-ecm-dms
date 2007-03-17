/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;


/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 03, 2006
 * 9:43:23 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UITemplatesManager extends UIContainer{
  final static public String VIEW_TEMPLATE = "UIViewTemplate" ;
  final static public String NEW_TEMPLATE = "TemplatePopup" ;

  public UITemplatesManager() throws Exception {
    addChild(UINodeTypeList.class, null, null) ;
  }

  public void initPopup(UIComponent uiComponent, String title) throws Exception {
    String popuId = title ; 
    if (title == null ) popuId = uiComponent.getId() ;
    UIPopupWindow uiPopup = getChildById(popuId) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, popuId) ;
      uiPopup.setWindowSize(700, 0) ;
    } else { 
      uiPopup.setRendered(true) ;
    }
    uiPopup.setUIComponent(uiComponent) ;
    uiPopup.setShow(true) ;
  }

  public void initPopupPermission(String id) throws Exception {
    String popupId = id + UITemplateContent.TEMPLATE_PERMISSION ;
    UIPopupWindow uiPopup = getChildById(popupId) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, popupId);
      uiPopup.setWindowSize(560, 300);
      UIECMPermissionBrowser uiECMPermission = 
        createUIComponent(UIECMPermissionBrowser.class, null, null) ;
      UITemplateContent uiTemContent = findComponentById(id) ;
      uiECMPermission.setComponent(uiTemContent, null) ;
      uiPopup.setUIComponent(uiECMPermission);
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }

  public void refresh() throws Exception {
    getChild(UINodeTypeList.class).updateGrid() ;
  }   
}