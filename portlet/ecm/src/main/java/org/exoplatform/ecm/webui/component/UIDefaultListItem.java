/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 9, 2006
 * 10:10:03 AM 
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/UITreeList.gtmpl",
    events = {
        @EventConfig(listeners = UIDefaultListItem.SelectActionListener.class)
    }
)
public class UIDefaultListItem extends UITreeList {

  public UIDefaultListItem() throws Exception {}

  static public class SelectActionListener extends EventListener<UIDefaultListItem> {
    public void execute(Event<UIDefaultListItem> event) throws Exception {
      UIDefaultListItem uiDefault = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRBrowser uiJCRBrowser = uiDefault.getParent() ;
      String returnField = uiJCRBrowser.getReturnField() ;
      ((UISelector)uiJCRBrowser.getReturnComponent()).updateSelect(returnField, value) ;
      UIJCRExplorer uiExplorer = uiJCRBrowser.getAncestorOfType(UIJCRExplorer.class) ;
      if(uiExplorer != null) {
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ; 
        if(uiExplorer.isHidePopup_) return ;
        uiJCRBrowser.getAncestorOfType(UIJCRExplorer.class).updateAjax(event) ;
      }
    }
  }
}
