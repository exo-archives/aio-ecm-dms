/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin;

import org.exoplatform.webui.component.UIPortletApplication;
import org.exoplatform.webui.component.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Jul 27, 2006
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/webui/component/admin/UIECMAdminPortlet.gtmpl",
    events = { @EventConfig(listeners = UIECMAdminPortlet.ShowHideActionListener.class)}
)
public class UIECMAdminPortlet extends UIPortletApplication {
  private String renderedCompName_ = "UITaxonomyManager" ;
  private boolean isShowSideBar = true ;

  public UIECMAdminPortlet() throws Exception {
    addChild(UIECMAdminFunctionTitle.class, null, null) ;
    addChild(UIECMAdminControlPanel.class, null, null) ;
    addChild(UIECMAdminWorkingArea.class, null, null) ;
  }

  public boolean isShowSideBar() { return isShowSideBar ; }
  public void setShowSideBar(boolean bl) { this.isShowSideBar = bl ; }
  
  public String getRenderedCompName() { return renderedCompName_ ; }
  public void setRenderedCompName(String name) { renderedCompName_ = name ; }
  
  static public class ShowHideActionListener extends EventListener<UIECMAdminPortlet> {
    public void execute(Event<UIECMAdminPortlet> event) throws Exception {
      UIECMAdminPortlet uiECMAdminPortlet = event.getSource() ;
      uiECMAdminPortlet.setShowSideBar(!uiECMAdminPortlet.isShowSideBar) ;
    }
  }
}
