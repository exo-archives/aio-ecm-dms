package org.exoplatform.ecm.webui.component.explorer;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIPortletApplication;
import org.exoplatform.webui.component.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template =  "system:/groovy/webui/component/UIApplication.gtmpl"
)
public class UIJCRExplorerPortlet extends UIPortletApplication {
  
  public UIJCRExplorerPortlet() throws Exception {    
    addChild(UIDrivesBrowser.class, null, null);
    addChild(UIJCRExplorer.class, null, null).setRendered(false) ;
    addChild(UIPreferencesForm.class, null, null).setRendered(false) ;
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }
}
