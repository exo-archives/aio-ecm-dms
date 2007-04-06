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
    addChild(UIPopupAction.class, null, null) ;
    addChild(UIHeaderBar.class, null, null) ;
    addChild(UIBrowseContainer.class, null, UIPortletApplication.VIEW_MODE) ;
    addChild(UIConfigTabPane.class, null, UIPortletApplication.EDIT_MODE).setRendered(false);
    
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN VIEW MODE \n");
      
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN EDIT MODE \n");
            
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
  
  static public class UIViewMode extends UIContainer {
    public UIViewMode() throws Exception {
      
    }
  }
}
