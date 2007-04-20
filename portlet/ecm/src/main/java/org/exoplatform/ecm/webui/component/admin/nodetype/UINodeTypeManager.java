/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 20, 2006
 * 2:20:55 PM 
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UINodeTypeManager extends UIContainer {

  final static public String IMPORT_POPUP = "NodeTypeImportPopup" ;
  final static public String EXPORT_POPUP = "NodeTypeExportPopup" ;

  public UINodeTypeManager() throws Exception {
    addChild(UINodeTypeList.class, null, "ListNodeType") ;
  }

  public void setExportPopup() throws Exception {
    removeChildById(EXPORT_POPUP) ;
    UIPopupWindow  uiPopup = addChild(UIPopupWindow.class, null, EXPORT_POPUP);
    uiPopup.setWindowSize(500, 400);    
    UINodeTypeExport uiExport = uiPopup.createUIComponent(UINodeTypeExport.class, null, null) ;
    uiExport.update() ;
    uiPopup.setUIComponent(uiExport) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public void setImportPopup() throws Exception {
    removeChildById(IMPORT_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, IMPORT_POPUP);
    uiPopup.setWindowSize(500, 400);    
    UINodeTypeImportPopup uiImportPopup =
      uiPopup.createUIComponent(UINodeTypeImportPopup.class, null, null) ;
    uiPopup.setUIComponent(uiImportPopup) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPopup(boolean isView) throws Exception {
    String popupID = "NodeTypePopup" ;
    if(isView) popupID = "ViewNodeTypePopup" ;
    removeChildById("NodeTypePopup") ;
    removeChildById("ViewNodeTypePopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupID) ;
    UINodeTypeForm uiForm = createUIComponent(UINodeTypeForm.class, null, null) ;
    uiForm.update(null, false) ;
    uiPopup.setWindowSize(660, 400) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
}
