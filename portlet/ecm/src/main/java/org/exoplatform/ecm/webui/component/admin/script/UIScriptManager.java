/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 27, 2006
 * 09:13:15 AM
 */
@ComponentConfig(template = "system:groovy/webui/component/UITabPane.gtmpl")

public class UIScriptManager extends UIContainer {

  public UIScriptManager() throws Exception {
    addChild(UIECMScripts.class, null , null) ;
    addChild(UICBScripts.class, null, null).setRendered(false) ;
  }

  public void refresh()throws Exception {
    getChild(UIECMScripts.class).refresh() ;
    getChild(UICBScripts.class).refresh() ;
  }
  
  public void removeECMScripForm() {
    getChild(UIECMScripts.class).removeChild(UIPopupWindow.class) ;
  }
  
  public void removeCBScripForm() {
    getChild(UICBScripts.class).removeChild(UIPopupWindow.class) ;
  }
}