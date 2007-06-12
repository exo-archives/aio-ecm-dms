/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 03, 2006
 * 9:43:23 AM 
 */
@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")

public class UIViewTemplate extends UIContainer {
  private String nodeTypeName_ ;
  
  public UIViewTemplate() throws Exception {
    addChild(UITemplateEditForm.class, null, null) ;
    addChild(UIDialogTab.class, null, null).setRendered(false) ;
    addChild(UIViewTab.class, null, null).setRendered(false) ;
  }
  
  private String getRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }
  
  public void refresh() throws Exception {
    getChild(UIDialogTab.class).updateGrid(nodeTypeName_, getRepository()) ;
    getChild(UIViewTab.class).updateGrid(nodeTypeName_) ;
  }
  public void setNodeTypeName(String nodeType) {
   nodeTypeName_ = nodeType ;
  }
  
  public String getNodeTypeName() {
    return nodeTypeName_ ;
  }
}