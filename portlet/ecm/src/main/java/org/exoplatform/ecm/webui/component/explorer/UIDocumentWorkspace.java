/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer ;

import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIDocumentWorkspace extends UIContainer {
  
  static public String SIMPLE_SEARCH_RESULT = "SimpleSearchResult" ;
  public UIDocumentWorkspace() throws Exception {
    addChild(UIDocumentInfo.class, null, null) ;
    addChild(UIDocumentContainer.class, null, null).setRendered(false) ;
    addChild(UISearchResult.class, null, SIMPLE_SEARCH_RESULT).setRendered(false) ;
  }
}