/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer ;

import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIDocumentWorkspace extends UIContainer {
  public UIDocumentWorkspace() throws Exception {
    addChild(UIDocumentInfo.class, null, null) ;
    addChild(UISearchResult.class, null, null).setRendered(false) ;
  }
}