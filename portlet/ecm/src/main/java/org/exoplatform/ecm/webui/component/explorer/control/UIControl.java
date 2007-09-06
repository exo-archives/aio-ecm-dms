/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.control ;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          nguyenkequanghung@yahoo.com
 * oct 5, 2006
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UIControl extends UIContainer {
  
  public UIControl() throws Exception {
    addChild(UIViewBar.class, null, null) ;
    addChild(UIAddressBar.class, null, null) ;
    addChild(UIActionBar.class, null, null) ;    
  }
}