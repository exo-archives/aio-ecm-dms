/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.administration;

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
public class UIWorkflowAdministrationPortlet extends UIPortletApplication {  
  public UIWorkflowAdministrationPortlet() throws Exception {
    addChild(UIAdministrationManager.class, null, null) ;
    UIPopupWindow popup = addChild(UIPopupWindow.class, null, "AdministrationPopup") ;
    popup.setUIComponent(createUIComponent(UIProcessDetail.class, null, null)) ;
  }
  
  public void initUploadPopup() throws Exception {
    UIPopupWindow uiPopup = getChildById("UploadProcessPopup") ;
    if(uiPopup == null) uiPopup = addChild(UIPopupWindow.class, null, "UploadProcessPopup") ;
    uiPopup.setWindowSize(530, 300);
    UIUploadProcess uiUploadProcess = createUIComponent(UIUploadProcess.class, null, null) ;
    uiPopup.setUIComponent(uiUploadProcess) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}