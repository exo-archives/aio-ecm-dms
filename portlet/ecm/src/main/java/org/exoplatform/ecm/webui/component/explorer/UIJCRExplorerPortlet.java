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
  
  final static public int INT_BROWSE = 0 ;
  final static public int INT_EXPLORER = 1 ;
  final static public int INT_PREFERENCE = 2 ;
  private int typeView_ ;

  public UIJCRExplorerPortlet() throws Exception {
    addChild(UIDrivesBrowser.class, null, null) ;
    addChild(UIJCRExplorer.class, null, null).setRendered(false) ;
    addChild(UIPreferencesForm.class, null, null).setRendered(false) ;
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      if(getChild(UIEditModeController.class) != null) {
        removeChild(UIEditModeController.class) ;
        if(typeView_ == INT_BROWSE) getChild(UIDrivesBrowser.class).setRendered(true) ;
        else if(typeView_ == INT_EXPLORER) getChild(UIJCRExplorer.class).setRendered(true) ;
        else if(typeView_ == INT_PREFERENCE) getChild(UIPreferencesForm.class).setRendered(true) ;
      }
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      if(getChild(UIDrivesBrowser.class).isRendered()) typeView_ = INT_BROWSE ;
      else if(getChild(UIJCRExplorer.class).isRendered()) typeView_ = INT_EXPLORER ;
      else if(getChild(UIPreferencesForm.class).isRendered()) typeView_ = INT_PREFERENCE ;
      getChild(UIDrivesBrowser.class).setRendered(false) ;
      getChild(UIJCRExplorer.class).setRendered(false) ;
      getChild(UIPreferencesForm.class).setRendered(false) ;
      UIEditModeController uiEditModeController = getChild(UIEditModeController.class) ; 
      if(uiEditModeController == null) {
        uiEditModeController = addChild(UIEditModeController.class, null, null) ;
        uiEditModeController.initEditMode() ;
      }
      uiEditModeController.setRendered(true) ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }
}
