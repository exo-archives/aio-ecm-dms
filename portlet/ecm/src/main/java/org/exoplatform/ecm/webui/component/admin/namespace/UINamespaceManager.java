/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.namespace;


import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 20, 2006
 * 16:37:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UINamespaceManager extends UIContainer {

  public UINamespaceManager() throws Exception {addChild(UINamespaceList.class, null, null) ;}
  
  public void refresh ()throws Exception {
    UINamespaceList list = getChild(UINamespaceList.class) ;
    list.updateGrid() ;
  }
  
  public void initPopup() throws Exception {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, "NamespacePopup") ;
      uiPopup.setWindowSize(600,0) ;
      UINamespaceForm uiNamespaceForm = createUIComponent(UINamespaceForm.class, null, null) ;
      uiPopup.setUIComponent(uiNamespaceForm) ;
      uiPopup.setShow(true) ;
      uiPopup.setResizable(true) ;
      return ;
    } 
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}