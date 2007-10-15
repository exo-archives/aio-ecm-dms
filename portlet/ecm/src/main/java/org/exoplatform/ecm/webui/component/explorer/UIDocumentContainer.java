/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 15, 2007 10:05:43 AM
 */
@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")

public class UIDocumentContainer extends UIContainer {
  
  public UIDocumentContainer() throws Exception {
    addChild(UIDocumentWithTree.class, null, null) ;
    addChild(UIDocumentInfo.class, null, null).setRendered(false) ;
  }
}
