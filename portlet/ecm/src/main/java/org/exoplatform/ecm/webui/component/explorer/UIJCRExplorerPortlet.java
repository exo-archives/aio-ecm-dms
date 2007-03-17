package org.exoplatform.ecm.webui.component.explorer;

import org.exoplatform.webui.application.Application;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIPortletApplication;
import org.exoplatform.webui.component.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template =  "system:/groovy/webui/component/UIApplication.gtmpl"
)
public class UIJCRExplorerPortlet extends UIPortletApplication {  

  public UIJCRExplorerPortlet() throws Exception {
    addChild(UIDrivesBrowser.class, null, null) ;
    addChild(UIJCRExplorer.class, null, null).setRendered(false) ;
    addChild(UIPreferencesForm.class, null, null).setRendered(false) ;
  }
  
  public void processRender(Application app, RequestContext context) throws Exception {
    context.importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    super.processRender(app, context);
  }
}
