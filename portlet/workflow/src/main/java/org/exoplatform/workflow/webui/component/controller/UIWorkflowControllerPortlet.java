/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Dec 15, 2006  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template =  "app:/groovy/webui/component/UIWorkflowPortlet.gtmpl"
)
public class UIWorkflowControllerPortlet extends UIPortletApplication {  
  private boolean isShowMonitor = false ;
  
  public boolean isShowMonitor() { return isShowMonitor ; }

  public UIWorkflowControllerPortlet() throws Exception {
    addChild(UIControllerManager.class, null, null) ;
    UIPopupWindow popup = addChild(UIPopupWindow.class, null, "ControllerPopup").setRendered(false) ;
    popup.setUIComponent(createUIComponent(UITaskManager.class, null, null)) ;
    popup.setWindowSize(600, 0) ;
    popup.setResizable(true) ;
  }
}