/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 23, 2006
 * 3:47:44 PM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UITemplateContainer extends UIContainer {

  public UITemplateContainer() throws Exception {
  }

  public void initPopup(String id) throws Exception {
    String popupId = id + "Popup" ;
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId) ;
    uiPopup.setWindowSize(600,400) ;
    UITemplateForm uiTempForm = createUIComponent(UITemplateForm.class, null, id) ;
    uiTempForm.updateOptionList() ;
    uiPopup.setUIComponent(uiTempForm) ;
    uiPopup.setShow(true) ;
  }
}
