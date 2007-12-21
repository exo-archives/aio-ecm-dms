/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
  
  public void initEditPopup(Node actionNode) throws Exception {
    removeChildById("editActionPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "editActionPopup") ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node currentNode = uiExplorer.getCurrentNode() ;    
    UIActionForm uiActionForm = createUIComponent(UIActionForm.class, null, "EditFormAction") ;
    uiActionForm.createNewAction(currentNode, actionNode.getPrimaryNodeType().getName(), false) ;
    uiActionForm.setIsUpdateSelect(false) ;
    uiActionForm.setNode(actionNode) ;
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