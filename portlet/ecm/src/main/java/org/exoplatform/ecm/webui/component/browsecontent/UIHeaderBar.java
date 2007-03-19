/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 14, 2006 3:18:02 PM 
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/browse/UIHeaderBar.gtmpl",
    events = {@EventConfig(listeners = UIHeaderBar.SwitchActionListener.class)}
)

public class UIHeaderBar extends UIContainer {
  private static String[] actions_ = {"Switch"};
  public UIHeaderBar(){}
  
  public String[] getActions() { return actions_ ;}
  public void setActions(String[] actions) { actions_ = actions ;}

  static public class ViewPortletActionListener extends EventListener<UIHeaderBar> {
    public void execute(Event<UIHeaderBar> event) throws Exception {
      UIHeaderBar uiHeaderBar = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = 
        uiHeaderBar.getAncestorOfType(UIBrowseContentPortlet.class) ;
      uiBrowseContentPortlet.removeChild(UIConfigTabPane.class) ;
      UIBrowseContainer uiContainer  = uiBrowseContentPortlet.getChild(UIBrowseContainer.class) ;
      uiContainer.setRendered(true) ; 
    }
  } 

  static public class AddNewConfigActionListener extends EventListener<UIHeaderBar> {
    public void execute(Event<UIHeaderBar> event) throws Exception {
      UIHeaderBar uiHeaderBar = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = 
        uiHeaderBar.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIConfigTabPane uiTabPane = uiBrowseContentPortlet.getChild(UIConfigTabPane.class) ;
      if (uiTabPane == null){
        uiTabPane = uiBrowseContentPortlet.addChild(UIConfigTabPane.class, null, null) ;
      } 
      uiBrowseContentPortlet.getChild(UIBrowseContainer.class).setRendered(false) ;
      uiTabPane.setRendered(true) ;
      uiTabPane.createNewConfig() ;
    }
  }


  static public class ConfigPortletActionListener extends EventListener<UIHeaderBar> {
    public void execute(Event<UIHeaderBar> event) throws Exception {
      UIHeaderBar uiHeaderBar = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = 
        uiHeaderBar.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIConfigTabPane uiTabPane = uiBrowseContentPortlet.getChild(UIConfigTabPane.class) ;
      if (uiTabPane == null){
        uiTabPane = uiBrowseContentPortlet.addChild(UIConfigTabPane.class, null, null) ;
      } 
      uiBrowseContentPortlet.getChild(UIBrowseContainer.class).setRendered(false) ;
      uiTabPane.setRendered(true) ;
      uiTabPane.getCurrentConfig() ;
    }
  }
  
  static public class SwitchActionListener extends EventListener<UIHeaderBar> {
    public void execute(Event<UIHeaderBar> event) throws Exception {
      UIHeaderBar uiHeaderBar = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = 
        uiHeaderBar.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIConfigTabPane uiTabPane = uiBrowseContentPortlet.getChild(UIConfigTabPane.class) ;
      UIBrowseContainer uiContainer = uiBrowseContentPortlet.getChild(UIBrowseContainer.class) ;
      if (uiTabPane == null){
        uiTabPane = uiBrowseContentPortlet.addChild(UIConfigTabPane.class, null, null) ;
      }
      if(uiTabPane.isRendered()) { 
        uiTabPane.setRendered(false) ;
        uiContainer.setRendered(true) ;
      } else {
        uiTabPane.setRendered(true) ;
        uiContainer.setRendered(false) ;
        uiTabPane.getCurrentConfig() ;
      }
    }
  }

}
