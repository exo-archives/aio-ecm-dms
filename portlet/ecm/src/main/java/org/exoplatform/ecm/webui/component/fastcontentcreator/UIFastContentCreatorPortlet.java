/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.fastcontentcreator;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
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
    uiJCRBrowser.setRepository(repositoryName) ;
    uiJCRBrowser.setWorkspace(workspaceName) ;
    uiPopup.setUIComponent(uiJCRBrowser);
    UIEditModeConfiguration uiEditModeDocumentType = getChild(UIEditModeConfiguration.class) ;
    uiJCRBrowser.setComponent(uiEditModeDocumentType, new String[] {UIEditModeConfiguration.FIELD_SAVEDPATH}) ;
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
      }
      if(getChild(UIFastContentCreatortForm.class) == null) {
        UIFastContentCreatortForm uiDialogForm = createUIComponent(UIFastContentCreatortForm.class, null, null) ;
        PortletRequestContext portletContext = (PortletRequestContext) context ;
        PortletRequest request = portletContext.getRequest() ; 
        PortletPreferences preferences = request.getPreferences() ;
        String prefType = preferences.getValue("type", "") ;
        uiDialogForm.setTemplateNode(prefType) ;
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
