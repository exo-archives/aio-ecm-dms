/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Apr 12, 2007 9:24:36 AM 
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIConfigContainer extends UIContainer {

  public void initNewConfig(String browseType, String workSpace) throws Exception {
    PortletPreferences preference = getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
    if(browseType.equals(Utils.CB_USE_FROM_PATH)) {
      UIPathConfig uiPathConfig = addChild(UIPathConfig.class, null, null) ;
      uiPathConfig.initForm(preference, workSpace, true, true) ;
    }else if(browseType.equals(Utils.CB_USE_JCR_QUERY)) {
      UIQueryConfig uiQueryConfig = addChild(UIQueryConfig.class, null, null) ;
      uiQueryConfig.initForm(preference, workSpace, true, true) ;
    } else if(browseType.equals(Utils.CB_USE_SCRIPT)) {
      UIScriptConfig uiScriptConfig = addChild(UIScriptConfig.class, null, null) ;
      uiScriptConfig.initForm(preference, workSpace, true, true) ;
    }else if(browseType.equals(Utils.CB_USE_DOCUMENT)) {
      UIDocumentConfig uiDocumentConfig =  addChild(UIDocumentConfig.class, null, null) ;
      uiDocumentConfig.initForm(preference, workSpace, true, true) ;
    }
  }
}
