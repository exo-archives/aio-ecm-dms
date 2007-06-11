/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 11:27:32 AM
 */
@ComponentConfig (lifecycle = UIContainerLifecycle.class) 
public class UIAddLanguageContainer extends UIContainer {

  public String nodeTypeName_ = null;
  
  public UIAddLanguageContainer() throws Exception {
    addChild(UILanguageTypeForm.class, null, null) ;
  }
  
  public void setComponentDisplay(String nodeTypeName) throws Exception {
    nodeTypeName_ = nodeTypeName ;
    UILanguageDialogForm uiDialogForm = createUIComponent(UILanguageDialogForm.class, null, null) ;
    uiDialogForm.setTemplateNode(nodeTypeName) ;
    uiDialogForm.setNode(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
    addChild(uiDialogForm) ;
  }
}
