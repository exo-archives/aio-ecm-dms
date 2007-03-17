/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.folksonomy;

import javax.jcr.Node;

import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 11, 2007  
 * 2:22:08 PM
 */
@ComponentConfig( 
    lifecycle = UIContainerLifecycle.class,
    events = @EventConfig(listeners = UIFolksonomyManager.EditStyleActionListener.class)
)
public class UIFolksonomyManager extends UIContainer {
  
  public UIFolksonomyManager() throws Exception {
    addChild(UITagStyleList.class, null, null) ;
  }
  
  public void initTaggingFormPopup(Node selectedTagStyle) throws Exception {
    removeChildById("FolksonomyPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "FolksonomyPopup") ;
    uiPopup.setWindowSize(600, 500) ;
    UITagStyleForm uiForm = createUIComponent(UITagStyleForm.class, null, null) ;
    uiForm.setTagStyle(selectedTagStyle) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
  
  public Node getSelectedTagStyle(String tagStyleName) throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    for(Node tagStyle: folksonomyService.getAllTagStyle()) {
      if(tagStyle.getName().equals(tagStyleName)) return tagStyle ;
    }
    return null ;
  }
  
  static public class EditStyleActionListener extends EventListener<UIFolksonomyManager> {
    public void execute(Event<UIFolksonomyManager> event) throws Exception {
      UIFolksonomyManager uiManager = event.getSource() ;
      String selectedName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node selectedTagStyle = uiManager.getSelectedTagStyle(selectedName) ;
      uiManager.initTaggingFormPopup(selectedTagStyle) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
