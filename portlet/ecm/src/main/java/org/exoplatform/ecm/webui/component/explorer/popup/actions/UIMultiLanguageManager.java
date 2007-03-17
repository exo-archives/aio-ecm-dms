/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 11:23:26 AM
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIECMTabPane.gtmpl")
public class UIMultiLanguageManager extends UIContainer implements UIPopupComponent {

  public UIMultiLanguageManager() throws Exception {
    addChild(UIMultiLanguageForm.class, null, null) ;
    addChild(UIAddLanguageContainer.class, null, null).setRendered(false) ;
  }

  public void activate() throws Exception {
    UIMultiLanguageForm uiForm = getChild(UIMultiLanguageForm.class) ;
    uiForm.updateSelect(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
  }
  public void deActivate() throws Exception {}

}
