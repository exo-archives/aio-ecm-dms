/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL 
 * Author : Pham Tuan 
 *          phamtuanchip@yahoo.de 
 * Nov 15, 2006 11:10:20 AM
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/popup/admin/UITabWithAction.gtmpl",
    events = { @EventConfig(listeners = UIActionViewContainer.CancelActionListener.class)}
)

public class UIActionViewContainer extends UIContainer {

  private String[] actions_ = new String[] {"Cancel"} ;

  public String[] getActions() {return actions_ ;}

  static public class CancelActionListener extends EventListener<UIActionViewContainer> {
    public void execute(Event<UIActionViewContainer> event) throws Exception {
      UIActionManager uiActionManager = event.getSource().getAncestorOfType(UIActionManager.class) ;
      uiActionManager.removeChild(UIActionViewContainer.class) ;
      uiActionManager.setRenderedChild(UIActionList.class) ;
    }
  }
}
