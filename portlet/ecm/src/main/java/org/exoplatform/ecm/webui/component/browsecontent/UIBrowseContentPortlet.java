/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.security.auth.Refreshable;

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
    addChild(UIBrowseContentHelp.class, null, null) ;
    PortletRequestContext context =  (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popup = addChild(UIPopupAction.class, null, null);
    popup.setId("UICBPopupAction") ;
    popup.getChild(UIPopupWindow.class).setId("UICBPopupWindow") ;
    UIBrowseContainer uiBrowseContainer = addChild(UIBrowseContainer.class, null , null) ;
    addChild(UIConfigTabPane.class, null, null) ;
    try {
      uiBrowseContainer.loadPortletConfig(getPortletPreferences()) ;
    } catch (Exception e) {
      setPorletMode(PortletRequestContext.HELP_MODE) ;
    }
  }

  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/") ;
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIBrowseContentPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    UIConfigTabPane uiTabPane = getChild(UIConfigTabPane.class) ;
    UIBrowseContainer uiContainer = getChild(UIBrowseContainer.class) ;
    UIBrowseContentHelp uiBCHelp = getChild(UIBrowseContentHelp.class) ;
    if(portletReqContext.getApplicationMode() == PortletRequestContext.VIEW_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN VIEW  MODE \n");  
      uiTabPane.setRendered(false) ;
      uiBCHelp.setRendered(false) ;
      uiContainer.setRendered(true) ;
      uiContainer.refreshContent() ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.EDIT_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN EDIT  MODE \n");  
      if(! uiTabPane.isNewConfig()) uiTabPane.getCurrentConfig() ;
      uiTabPane.setRendered(true) ;
      uiBCHelp.setRendered(false) ;
      uiContainer.setRendered(false) ;
    } else if(portletReqContext.getApplicationMode() == PortletRequestContext.HELP_MODE) {
      System.out.println("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
      uiTabPane.setRendered(false) ;
      uiBCHelp.setRendered(true) ;
      uiContainer.setRendered(false) ;
    }
    try {
      super.processRender(app, context) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
  }
  protected void reload() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    WebuiApplication app =  (WebuiApplication)context.getApplication() ;
    processRender(app, context) ;
  }
  protected String getWindowId() {
    PortletRequestContext context =  (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    return ((ExoPortletRequest)context.getRequest()).getWindowID().getUniqueID() ;
  }
  protected void setPorletMode(int mode) {
    PortletRequestContext portletReqContext =  (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    portletReqContext.setApplicationMode(mode) ;
  }
  protected boolean isExitsRepo(String repoName) {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    try {
      rService.getRepository(repoName) ;
      return true ;
    } catch (Exception e) {
      e.printStackTrace() ;
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
