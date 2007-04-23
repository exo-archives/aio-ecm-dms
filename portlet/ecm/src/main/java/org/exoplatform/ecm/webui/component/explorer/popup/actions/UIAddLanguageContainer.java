/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

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
    UIDocumentForm uiDocumentForm = createUIComponent(UIDocumentForm.class, null, null) ;
    uiDocumentForm.setTemplateNode(nodeTypeName) ;
    uiDocumentForm.setNode(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
    uiDocumentForm.setIsMultiLanguage(true) ;
    addChild(uiDocumentForm) ;
  }
}
