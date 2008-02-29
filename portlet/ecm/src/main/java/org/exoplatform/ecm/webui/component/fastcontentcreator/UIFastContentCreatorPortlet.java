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
package org.exoplatform.ecm.webui.component.fastcontentcreator;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 4, 2007 3:26:15 PM
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template =  "app:/groovy/webui/component/fastcontentcreator/UIFastContentCreatorPortlet.gtmpl"
)
public class UIFastContentCreatorPortlet extends UIPortletApplication {

  public UIFastContentCreatorPortlet() throws Exception {
  }

  public void initPopupJCRBrowser(String repositoryName, String workspaceName) throws Exception {
    removeChild(UIPopupWindow.class) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, null);
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    if(SessionsUtils.isAnonim()) {      
      uiJCRBrowser.setSessionProvider(SessionsUtils.getAnonimProvider()) ;
    }
    uiJCRBrowser.setRepository(repositoryName) ;
    uiJCRBrowser.setIsDisable(workspaceName, true) ;
    uiJCRBrowser.setShowRootPathSelect(true) ;
    uiPopup.setUIComponent(uiJCRBrowser);
    UIEditModeConfiguration uiEditModeDocumentType = getChild(UIEditModeConfiguration.class) ;
    uiJCRBrowser.setComponent(uiEditModeDocumentType, new String[] {UIEditModeConfiguration.FIELD_SAVEDPATH}) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPopup(UIComponent uiComp) throws Exception {
    removeChildById("PopupComponent") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PopupComponent") ;
    uiPopup.setUIComponent(uiComp) ;
    uiPopup.setWindowSize(640, 300) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIFastContentCreatorPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if (portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      if(getChild(UIEditModeConfiguration.class) != null) {
        removeChild(UIEditModeConfiguration.class) ;
        removeChild(UIPopupWindow.class) ;
      }
      if(getChild(UIFastContentCreatortForm.class) == null) {
        UIFastContentCreatortForm uiDialogForm = createUIComponent(UIFastContentCreatortForm.class, null, null) ;
        PortletRequestContext portletContext = (PortletRequestContext) context ;
        PortletRequest request = portletContext.getRequest() ; 
        PortletPreferences preferences = request.getPreferences() ;
        String prefType = preferences.getValue("type", "") ;
        String repo = preferences.getValue("repository", "") ;
        uiDialogForm.setTemplateNode(prefType) ;
        uiDialogForm.setWorkspace(preferences.getValue("workspace", "")) ;
        uiDialogForm.setStoredPath(preferences.getValue("path", "")) ;
        uiDialogForm.setRepositoryName(repo) ;
        addChild(uiDialogForm) ; 
      }
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      if(getChild(UIFastContentCreatortForm.class) != null) {
        removeChild(UIFastContentCreatortForm.class) ;
      }
      if(getChild(UIEditModeConfiguration.class) == null) {
        UIEditModeConfiguration uiEditMode = addChild(UIEditModeConfiguration.class, null, null) ;
        uiEditMode.initEditMode() ;
      }
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }  
}