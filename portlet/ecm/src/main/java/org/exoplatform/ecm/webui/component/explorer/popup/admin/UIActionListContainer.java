/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 7, 2007 1:35:17 PM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIActionListContainer extends UIContainer {
  
  public UIActionListContainer() throws Exception {
    addChild(UIActionList.class, null, null) ;
  }
  
  public void initEditPopup(String actionName) throws Exception {
    removeChildById("editActionPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "editActionPopup") ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node currentNode = uiExplorer.getCurrentNode() ;
    Node selectedNode = currentNode.getNode(actionName) ;
    UIActionForm uiActionForm = createUIComponent(UIActionForm.class, null, "EditFormAction") ;
    uiActionForm.createNewAction(currentNode, selectedNode.getPrimaryNodeType().getName(), false) ;
    uiActionForm.setIsUpdateSelect(false) ;
    uiActionForm.setNode(selectedNode) ;
    uiActionForm.setIsEditInList(true) ;
    uiPopup.setWindowSize(650, 450);
    uiPopup.setUIComponent(uiActionForm) ;
    uiPopup.setRendered(true);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPopup(UIComponent uiComp) throws Exception {
    removeChildById("PopupComponent") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PopupComponent") ;
    uiPopup.setUIComponent(uiComp) ;
    uiPopup.setWindowSize(640, 300) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
}