/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.action;

import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 20, 2006
 * 16:37:15 AM
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)

public class UIActionManager extends UIContainer {

  public UIActionManager() throws Exception {addChild(UIActionTypeList.class, null, null) ;}

  public void refresh() throws Exception {
    UIActionTypeList list = getChild(UIActionTypeList.class) ;
    list.updateGrid() ;
  }

  public void initPopup(UIComponent uiActionForm, int width) throws Exception {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, "ActionPopup") ;     
      uiPopup.setUIComponent(uiActionForm) ;
      uiPopup.setWindowSize(width, 0) ;
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
}