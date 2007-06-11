/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 30, 2007  
 * 9:27:48 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIViewMetadataManager extends UIContainer implements UIPopupComponent {

  final static public String METADATAS_POPUP = "metadataForm" ;
  
  public UIViewMetadataManager() throws Exception {
     addChild(UIViewMetadataContainer.class, null, null) ;
  }
  
  public Node getViewNode(String nodeType) throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getViewNode(nodeType) ; 
  }
  
  public void initMetadataFormPopup(String nodeType) throws Exception {
    removeChildById(METADATAS_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, METADATAS_POPUP) ;
    uiPopup.setWindowSize(650, 450);
    UIViewMetadataForm uiForm = createUIComponent(UIViewMetadataForm.class, null, null) ;
    uiForm.getChildren().clear() ;
    uiForm.setNodeType(nodeType) ;
    uiForm.setIsNotEditNode(true) ;
    uiForm.setPropertyNode(getViewNode(nodeType)) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setRendered(true);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
}
