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
package org.exoplatform.workflow.webui.component.controller;

import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Dec 15, 2006  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template =  "app:/groovy/webui/component/UIWorkflowPortlet.gtmpl",
    events = {
      @EventConfig(listeners = UIWorkflowControllerPortlet.RefreshSessionActionListener.class)
    }
)
public class UIWorkflowControllerPortlet extends UIPortletApplication {  
  private boolean isShowMonitor = false ;
  
  public boolean isShowMonitor() { return isShowMonitor ; }

  public UIWorkflowControllerPortlet() throws Exception {
    addChild(UIControllerManager.class, null, null) ;
    UIPopupContainer uiWorkflowPopup = addChild(UIPopupContainer.class, null, null) ;
    uiWorkflowPopup.getChild(UIPopupWindow.class).setId("ControllerPopup") ;
  }
  
  static public class RefreshSessionActionListener extends EventListener<UIWorkflowControllerPortlet> {
    public void execute(Event<UIWorkflowControllerPortlet> event) throws Exception {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      String mess = "UIWorkflowControllerPortlet.msg.refresh-session-success";
      uiApp.addMessage(new ApplicationMessage(mess, null, ApplicationMessage.INFO));
      UIControllerManager uiControllerManager = event.getSource().getChild(UIControllerManager.class);
      UITaskList uiTaskList = uiControllerManager.getChild(UITaskList.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskList) ;
    }
  }
}