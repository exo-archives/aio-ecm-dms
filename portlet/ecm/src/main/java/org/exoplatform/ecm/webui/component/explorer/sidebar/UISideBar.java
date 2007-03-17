/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          nguyenkequanghung@yahoo.com
 * oct 5, 2006
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/sidebar/UISideBar.gtmpl",
    events = {
        @EventConfig(listeners = UISideBar.CloseActionListener.class),
        @EventConfig(listeners = UISideBar.ExplorerActionListener.class),
        @EventConfig(listeners = UISideBar.ViewRelationActionListener.class)
    }
)
public class UISideBar extends UIContainer {
  
  private String currentComp ;
  
  public UISideBar() throws Exception {
    currentComp = addChild(UITreeExplorer.class, null, null).getId() ;
    addChild(UIViewRelationList.class, null, null).setRendered(false) ;
  }
  
  public String getCurrentComp() { return currentComp ; }
  public void setCurrentComp(String currentComp) { this.currentComp = currentComp ; }
  
  static public class CloseActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent() ;
      uiWorkingArea.setShowSideBar(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class ExplorerActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.currentComp = "UITreeExplorer" ;
      uiSideBar.setRenderedChild(UITreeExplorer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar) ;
    }
  }

  static public class ViewRelationActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.setRenderedChild(UIViewRelationList.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar) ;
    }
  }
}