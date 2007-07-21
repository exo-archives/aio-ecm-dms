/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;


import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 17, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    template = "system:groovy/webui/core/UITabPane.gtmpl",
    events = {
        @EventConfig(listeners = UIPropertiesManager.ChangeTabActionListener.class)
    }
)

public class UIPropertiesManager extends UIContainer implements UIPopupComponent {

  public void activate() throws Exception {
    addChild(UIPropertyTab.class, null, null)  ;
    addChild(UIPropertyForm.class, null, null).setRendered(false) ;
  }

  public void deActivate() throws Exception {}
  public void setLockForm(boolean isLockForm) {
    getChild(UIPropertyForm.class).lockForm(isLockForm) ;
  }
  static public class ChangeTabActionListener extends EventListener<UIPropertiesManager> {
    public void execute(Event<UIPropertiesManager> event) throws Exception {
      System.out.println("\n\nGo here\n\n");
      UIPropertiesManager uiManager = event.getSource() ;
      for(UIComponent uiChild : uiManager.getChildren()) {
        if(uiChild.isRendered()) System.out.println("\n\nuicomponent name====>" +uiChild.getId()+ "\n\n");
      }
    }
  }
}

