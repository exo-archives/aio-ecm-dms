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
 * Oct 18, 2006
 * 10:29:16 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UITabPaneWithAction.gtmpl",
    events = @EventConfig(listeners = UIRelationManager.CloseActionListener.class)
)
public class UIRelationManager extends UITabPane implements UIPopupComponent {

  final static public String[] ACTIONS = {"Close"} ;
  
  public UIRelationManager() throws Exception {
    addChild(UIRelationsAddedList.class, null, null) ;
    addChild(UIJCRBrowser.class, null, null).setRendered(false) ;
  }
  
  public String[] getActions() { return ACTIONS ; }

  static public class CloseActionListener extends EventListener<UIRelationManager> {
    public void execute(Event<UIRelationManager> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
}
