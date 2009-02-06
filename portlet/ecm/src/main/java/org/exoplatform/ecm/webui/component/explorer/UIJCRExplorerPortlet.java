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

import org.exoplatform.ecm.webui.utils.Utils;
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
  final static public String REPOSITORY =  "repository";
  final static public String CATEGORY_MANDATORY =  "categoryMandatoryWhenFileUpload";
  final static public String ISDIRECTLY_DRIVE =  "isDirectlyDrive";
  final static public String DRIVE_NAME =  "driveName";
  
  private boolean flagSelect = false;
  
  public UIJCRExplorerPortlet() throws Exception {
    UIJcrExplorerContainer explorerContainer = addChild(UIJcrExplorerContainer.class, null, null);
    explorerContainer.resert();
    explorerContainer.init();
    addChild(UIJcrExplorerEditContainer.class, null, null).setRendered(false);
  }
  
  public boolean isFlagSelect() {
    return flagSelect;
  }

  public void setFlagSelect(boolean flagSelect) {
    this.flagSelect = flagSelect;
  }

  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      String isDirectlyDrive =  portletPref.getValue("isDirectlyDrive", "").trim();
      if (isDirectlyDrive.equals("false")) {
        getChild(UIJcrExplorerContainer.class).setFlag(true);
        if (!isFlagSelect()) {
          UIDrivesBrowserContainer browserContainer = getChild(UIJcrExplorerContainer.class).getChild(UIDrivesBrowserContainer.class); 
          if (browserContainer != null) {
            browserContainer.setRendered(true);
          } else getChild(UIJcrExplorerContainer.class).addChild(UIDrivesBrowserContainer.class, null, null).setRendered(true);
          getChild(UIJcrExplorerContainer.class).getChild(UIJCRExplorer.class).setRendered(false);   
        } else {
          UIDrivesBrowserContainer driveBrowserContainer = getChild(UIJcrExplorerContainer.class).getChild(UIDrivesBrowserContainer.class);
          if (driveBrowserContainer == null) {
            getChild(UIJcrExplorerContainer.class).resert();
            getChild(UIJcrExplorerContainer.class).addChild(UIDrivesBrowserContainer.class, null, null).setRendered(false);
            getChild(UIJcrExplorerContainer.class).addChild(UIJCRExplorer.class, null, null);    
          }
        }
      } else {
        getChild(UIJcrExplorerContainer.class).resert();
        getChild(UIJcrExplorerContainer.class).init();
      }
      getChild(UIJcrExplorerContainer.class).setRendered(true);
      getChild(UIJcrExplorerEditContainer.class).setRendered(false);
    } else if(portletReqContext.getApplicationMode() == PortletMode.HELP) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    } else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      getChild(UIJcrExplorerContainer.class).setRendered(false);
      getChild(UIJcrExplorerEditContainer.class).setRendered(true);
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
