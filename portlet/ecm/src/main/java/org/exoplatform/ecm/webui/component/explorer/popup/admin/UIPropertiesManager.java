/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;


import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 17, 2006
 * 10:07:15 AM
 */
@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")

public class UIPropertiesManager extends UIContainer implements UIPopupComponent {

  public void activate() throws Exception {
    addChild(UIPropertyTab.class, null, null)  ;
    addChild(UIPropertyForm.class, null, null).setRendered(false) ;
  }
  
  public void deActivate() throws Exception {}
}

