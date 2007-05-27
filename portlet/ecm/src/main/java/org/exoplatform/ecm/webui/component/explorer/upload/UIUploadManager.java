/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.upload;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 24, 2007 2:12:48 PM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIUploadManager extends UIContainer implements UIPopupComponent {

  final static public String EXTARNAL_METADATA_POPUP = "AddMetadataPopup" ;
  
  public UIUploadManager() throws Exception {
    addChild(UIUploadForm.class, null, null) ;
    addChild(UIUploadContainer.class, null, null).setRendered(false) ;
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
  public void initMetadataPopup() throws Exception {
    removeChildById(EXTARNAL_METADATA_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, EXTARNAL_METADATA_POPUP) ;
    uiPopup.setWindowSize(400, 400);
    UIExternalMetadataForm uiExternalMetadataForm = createUIComponent(UIExternalMetadataForm.class, null, null) ;
    uiPopup.setUIComponent(uiExternalMetadataForm) ;
    uiExternalMetadataForm.renderExternalList() ;
    uiPopup.setRendered(true);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
}
