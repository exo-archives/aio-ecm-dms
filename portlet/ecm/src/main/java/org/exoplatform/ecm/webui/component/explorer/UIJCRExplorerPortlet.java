/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UIJCRExplorerPortlet extends UIPortletApplication {
  
  public UIJCRExplorerPortlet() throws Exception {
    setMinWidth(585) ;
    addChild(UIDrivesBrowser.class, null, null);
    addChild(UIJCRExplorer.class, null, null).setRendered(false) ;    
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
    } else if(portletReqContext.getApplicationMode() == PortletMode.HELP) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }
  
  public String getPreferenceRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    return repository ;
  }
}
