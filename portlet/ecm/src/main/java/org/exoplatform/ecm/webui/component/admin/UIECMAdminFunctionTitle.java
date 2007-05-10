/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin;

import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 8:26:51 AM 
 */
@ComponentConfig( 
    template = "app:/groovy/webui/component/admin/UIECMAdminFunctionTitle.gtmpl",
    events ={@EventConfig(listeners = UIECMAdminFunctionTitle.BackActionListener.class)}

)
public class UIECMAdminFunctionTitle extends UIComponent {
  public UIECMAdminFunctionTitle() {}

  public String getCurrentRepoName() {
    return getAncestorOfType(UIECMAdminPortlet.class).getRepoName() ;
  }
  public String getRenderedChildLabel() {
    return this.<UIECMAdminPortlet>getParent().getRenderedCompName() ;
  }
  
  public static class BackActionListener extends EventListener<UIECMAdminFunctionTitle>{
    public void execute(Event<UIECMAdminFunctionTitle> event) {
      UIECMAdminFunctionTitle uiTitleForm = event.getSource() ;
      UIECMAdminPortlet uiPortlet = uiTitleForm.getAncestorOfType(UIECMAdminPortlet.class) ;
      uiPortlet.setSelectedRepo(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }
}