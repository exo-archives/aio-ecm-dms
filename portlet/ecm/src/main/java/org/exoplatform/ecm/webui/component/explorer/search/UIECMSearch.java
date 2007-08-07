/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : le bien thuy  
 *          lebienthuyt@gmail.com
 * Oct 2, 2006
 * 10:08:51 AM 
 * Editor: pham tuan Oct 27, 2006
 */

@ComponentConfig( template = "system:groovy/webui/core/UITabPane.gtmpl" )
public class UIECMSearch extends UIContainer implements UIPopupComponent {
  public UIECMSearch() throws Exception {
    addChild(UISearchContainer.class, null, null) ;
    addChild(UIJCRAdvancedSearch.class, null, null).setRendered(false);
    addChild(UISavedQuery.class, null, null).setRendered(false) ;
    addChild(UISearchResult.class, null, "AdvancedSearchResult").setRendered(false) ;
  }

  public void activate() throws Exception {
    UIJCRAdvancedSearch advanceSearch = getChild(UIJCRAdvancedSearch.class);
    advanceSearch.update(null);
    UISavedQuery uiQuery = getChild(UISavedQuery.class);
    uiQuery.updateGrid();
  }

  public void deActivate() throws Exception {
  }
}