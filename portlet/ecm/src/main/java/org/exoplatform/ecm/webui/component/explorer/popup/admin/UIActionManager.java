/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 8, 2006
 * 9:39:58 AM 
 */
@ComponentConfig(template = "system:groovy/webui/component/UITabPane.gtmpl")
public class UIActionManager extends UIContainer implements UIPopupComponent {
  
  public UIActionManager() throws Exception {
    addChild(UIActionListContainer.class, null, null) ;
    addChild(UIActionContainer.class, null, null).setRendered(false) ;
  }
  
  public void activate() throws Exception {
    UIActionTypeForm uiActionTypeForm = findFirstComponentOfType(UIActionTypeForm.class) ;
    uiActionTypeForm.update() ;
    UIActionList uiActionList = findFirstComponentOfType(UIActionList.class) ;
    uiActionList.updateGrid(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
  }
  
  public void deActivate() throws Exception {
  }
  
  public void setDefaultConfig() throws Exception {
    UIActionTypeForm uiActionType = getChild(UIActionContainer.class).getChild(UIActionTypeForm.class) ;
    uiActionType.setDefaultActionType() ;
  }
  
  static public class CancelActionListener extends EventListener<UIActionManager> {
    public void execute(Event<UIActionManager> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}
