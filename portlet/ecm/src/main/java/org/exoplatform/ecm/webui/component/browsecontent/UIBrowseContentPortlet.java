/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.portletcontainer.ExoPortletRequest;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 14, 2006 5:15:47 PM 
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)

public class UIBrowseContentPortlet extends UIPortletApplication  {

  @SuppressWarnings("unused") 
  public UIBrowseContentPortlet() throws Exception {
    PortletRequestContext context =  (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popup = addChild(UIPopupAction.class, null, "UICBPopupAction");
    popup.getChild(UIPopupWindow.class).setId("UICBPopupWindow") ;
    addChild(UIBrowseContainer.class, null , null) ;
    UIConfigTabPane uiTabPane = createUIComponent(UIConfigTabPane.class, null, null) ;
    if(!isExitsRepo(getPortletPreferences().getValue(Utils.REPOSITORY, ""))) {
      uiTabPane.setNewConfig(true) ;
      uiTabPane.showNewConfigForm(true);
    } else {
      getChild(UIBrowseContainer.class).loadPortletConfig(getPortletPreferences()) ;
    }
    addChild(uiTabPane) ;
    uiTabPane.setRendered(false) ;
  }

  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIBrowseContentPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    UIConfigTabPane uiTabPane = getChild(UIConfigTabPane.class) ;
    UIBrowseContainer uiContainer = getChild(UIBrowseContainer.class) ;
    if(portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN VIEW  MODE \n");  
      if(isExitsRepo(getPortletPreferences().getValue(Utils.REPOSITORY, ""))) {
        uiTabPane.setRendered(false) ;
        uiContainer.refreshContent() ;
        uiContainer.setRendered(true) ;
      } else {
        uiContainer.setRendered(false) ;
      }
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN EDIT  MODE \n");  
      if(! uiTabPane.isNewConfig()) uiTabPane.getCurrentConfig() ;
      uiTabPane.setRendered(true) ;
      uiContainer.setRendered(false) ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    }
    super.processRender(app, context) ;
  }
  protected String getWindowId() {
    PortletRequestContext context =  (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    return ((ExoPortletRequest)context.getRequest()).getWindowID().getUniqueID() ;
  }
  protected boolean isExitsRepo(String repoName) {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    try {
      rService.getRepository(repoName) ;
      return true ;
    } catch (Exception e) {
      return false ;
    }
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }

  public String getPreferenceRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }
}
