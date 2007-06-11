package org.exoplatform.ecm.webui.component.admin.views;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 25, 2006
 * 11:45:11 AM 
 */

@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")
public class UIViewManager extends UIContainer {
  public UIViewManager() throws Exception{
    addChild(UIViewContainer.class, null, null) ;
    UITemplateContainer uiECMTemp = addChild(UITemplateContainer.class, null, "ECMTemplate") ;
    uiECMTemp.addChild(UIECMTemplateList.class, null, null) ;
    uiECMTemp.setRendered(false) ;
    UITemplateContainer uiCBTemp = addChild(UITemplateContainer.class, null, "CBTemplate") ;
    uiCBTemp.addChild(UICBTemplateList.class, null, null) ;
    uiCBTemp.setRendered(false) ;
  }
}
