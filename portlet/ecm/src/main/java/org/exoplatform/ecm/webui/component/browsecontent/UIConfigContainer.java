/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Apr 12, 2007 9:24:36 AM 
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIConfigContainer extends UIContainer {

  public void initNewConfig(String usercase, String repository, String workspace) throws Exception {
    PortletPreferences preference = getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
    for(UIComponent child : getChildren()) {
      child.setRendered(false) ;
    }
    if(usercase.equals(Utils.CB_USE_FROM_PATH)) {
      UIPathConfig uiPathConfig = getChild(UIPathConfig.class) ;
      if(uiPathConfig == null) uiPathConfig = addChild(UIPathConfig.class, null, null) ;
      uiPathConfig.isEdit_ = true ;
      uiPathConfig.initForm(preference, repository, workspace, true) ;
      uiPathConfig.setRendered(true) ;
      
    }else if(usercase.equals(Utils.CB_USE_JCR_QUERY)) {
      UIQueryConfig uiQueryConfig = getChild(UIQueryConfig.class) ;
      if(uiQueryConfig == null) {
        uiQueryConfig = addChild(UIQueryConfig.class, null, null) ;
      }  
      uiQueryConfig.isEdit_ = true ;
      uiQueryConfig.initForm(preference, repository, workspace, true) ;
      uiQueryConfig.setRendered(true) ;
    } else if(usercase.equals(Utils.CB_USE_SCRIPT)) {
      UIScriptConfig uiScriptConfig = getChild(UIScriptConfig.class) ;
      if(uiScriptConfig == null) {
        uiScriptConfig = addChild(UIScriptConfig.class, null, null) ;
      }
      uiScriptConfig.isEdit_ = true ;
      uiScriptConfig.initForm(preference, repository, workspace, true) ;
      uiScriptConfig.setRendered(true) ;
    }else if(usercase.equals(Utils.CB_USE_DOCUMENT)) {
      UIDocumentConfig uiDocumentConfig = getChild(UIDocumentConfig.class) ;
      if(uiDocumentConfig == null) {
        uiDocumentConfig = addChild(UIDocumentConfig.class, null, null) ;
      }
      uiDocumentConfig.isEdit_ = true ;
      uiDocumentConfig.initForm(preference, repository, workspace, true) ;
      uiDocumentConfig.setRendered(true) ;
      
    }
  }
}
 ;