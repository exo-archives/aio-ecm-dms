/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UITabPane;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 10:41:44 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UITabPaneWithAction.gtmpl",
    events = @EventConfig(listeners = UICategoryManager.CloseActionListener.class)
)
public class UICategoryManager extends UITabPane implements UIPopupComponent {

  final static public String[] ACTIONS = {"Close"} ;
  
  public UICategoryManager() throws Exception {
    addChild(UICategoriesAddedList.class, null, null) ;
    addChild(UIJCRBrowser.class, null, null).setRendered(false) ;
  }
  
  public String[] getActions() { return ACTIONS ; }

  static public class CloseActionListener extends EventListener<UICategoryManager> {
    public void execute(Event<UICategoryManager> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
      uiExplorer.setIsHidePopup(false) ;
    }
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
}
