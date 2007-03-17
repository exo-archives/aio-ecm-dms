/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.rules;

import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 23, 2006
 * 16:37:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIRuleManager extends UIContainer {

  public UIRuleManager() throws Exception {addChild(UIRuleList.class, null, null) ;}
  
  public void refresh() throws Exception {
    UIRuleList list = getChild(UIRuleList.class) ;
    list.updateGrid() ;
  }

  public void initPopup(UIComponent uiRuleForm) throws Exception {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, "RulePopup") ;     
      uiPopup.setUIComponent(uiRuleForm) ;
      uiPopup.setWindowSize(600, 0) ;
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}