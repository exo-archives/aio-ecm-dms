/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.monitoring;

import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.UIPortletApplication;
import org.exoplatform.webui.component.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

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
public class UIWorkflowMonitoringPortlet extends UIPortletApplication {  
  public UIWorkflowMonitoringPortlet() throws Exception {
    addChild(UIMonitorManager.class, null, null) ;
    UIPopupWindow popup = addChild(UIPopupWindow.class, null, null) ;
    popup.setUIComponent(createUIComponent(UIProcessDetail.class, null, null)) ;
  }
}