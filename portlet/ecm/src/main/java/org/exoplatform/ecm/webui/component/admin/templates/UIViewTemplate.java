/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

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
  
  public void refresh() throws Exception {     
    getChild(UIDialogTab.class).updateGrid(nodeTypeName_) ;
    getChild(UIViewTab.class).updateGrid(nodeTypeName_) ;
  }
  public void setNodeTypeName(String nodeType) {
   nodeTypeName_ = nodeType ;
  }
  
  public String getNodeTypeName() {
    return nodeTypeName_ ;
  }
}