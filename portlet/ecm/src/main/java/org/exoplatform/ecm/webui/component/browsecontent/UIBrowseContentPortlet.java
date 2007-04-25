/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPortletApplication;
import org.exoplatform.webui.component.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 14, 2006 5:15:47 PM 
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "system:/groovy/webui/component/UIApplication.gtmpl"
)

public class UIBrowseContentPortlet extends UIPortletApplication  {

  @SuppressWarnings("unused") 
  public UIBrowseContentPortlet() throws Exception {
    ManageViewService vservice = getApplicationComponent(ManageViewService.class) ;
    addChild(UIPopupAction.class, null, "UICBPopupAction") ;
    UIBrowseContainer uiContainer = createUIComponent(UIBrowseContainer.class, null, null) ;
    uiContainer.loadPortletConfig(getPortletPreferences()) ;
    addChild(uiContainer) ;
    UIConfigTabPane uiConfig = createUIComponent(UIConfigTabPane.class, null, null) ;
    addChild(uiConfig) ;
    uiConfig.getCurrentConfig() ;
    uiConfig.setRendered(false) ;
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    UIBrowseContainer uiContainer = getChild(UIBrowseContainer.class) ;
    UIConfigTabPane uiTabPane = getChild(UIConfigTabPane.class) ;
    if(portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      uiTabPane.setRendered(false) ;
      uiContainer.setRendered(true) ;
      getChild(UIBrowseContainer.class).getSession().refresh(true) ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      uiTabPane.setRendered(true) ;
      uiContainer.setRendered(false) ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }
}
