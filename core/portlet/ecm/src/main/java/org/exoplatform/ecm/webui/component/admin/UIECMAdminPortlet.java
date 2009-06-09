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
package org.exoplatform.ecm.webui.component.admin;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryControl;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Jul 27, 2006
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/webui/component/admin/UIECMAdminPortlet.gtmpl",
    events = { @EventConfig(listeners = UIECMAdminPortlet.ShowHideActionListener.class)}
)
public class UIECMAdminPortlet extends UIPortletApplication {

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger(UIECMAdminPortlet.class);
  
  private boolean isShowSideBar = true ;
  private boolean isSelectedRepo_ = true ;
  private String repoName_ = "" ;
  
  public UIECMAdminPortlet() throws Exception {
    addChild(UIRepositoryControl.class, null, null) ;
    UIPopupContainer uiPopupAction = addChild(UIPopupContainer.class, null, "UIECMAdminUIPopupAction");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIECMAdminUIPopupWindow") ;
    String repo = getPreferenceRepository() ;
    try{
      getApplicationComponent(RepositoryService.class).getRepository(repo) ;
      addChild(UIECMAdminControlPanel.class, null, null) ;
      addChild(UIECMAdminWorkingArea.class, null, null);
    } catch(Exception e) {
      LOG.error("An expected error occured while initializing the portlet", e);
    }        
  }
  
  public void initChilds() throws Exception{
    UIECMAdminControlPanel controlPanel = getChild(UIECMAdminControlPanel.class) ;
    if(controlPanel == null) addChild(UIECMAdminControlPanel.class, null, null) ;
    
    UIECMAdminWorkingArea workingArea = getChild(UIECMAdminWorkingArea.class) ;
    if(workingArea == null){
      addChild(UIECMAdminWorkingArea.class, null, null) ;
    } else {
      workingArea.init() ;      
    }
  }
  public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if(popupMess == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    popupMess.processRender(context);
  }
  
  public ManageableRepository getRepository() throws Exception {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    return rservice.getRepository(repoName_) ;
  }
  
  public boolean isShowSideBar() { return isShowSideBar ; }
  public void setShowSideBar(boolean bl) { this.isShowSideBar = bl ; }
  
  public boolean isSelectedRepo() { return isSelectedRepo_ ; }
  public void setSelectedRepo(boolean bl) { this.isSelectedRepo_ = bl ; }
  
  public String getRepoName() {return repoName_ ;}
  public void setRepoName(String name){repoName_ = name ;}
  
  static public class ShowHideActionListener extends EventListener<UIECMAdminPortlet> {
    public void execute(Event<UIECMAdminPortlet> event) throws Exception {
      UIECMAdminPortlet uiECMAdminPortlet = event.getSource() ;
      uiECMAdminPortlet.setShowSideBar(!uiECMAdminPortlet.isShowSideBar) ;
    }
  }
  
  public String getPreferenceRepository() {
    PortletPreferences portletPref = getPortletPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    return repository ;
  }
  
  public String getPreferenceWorkspace() {
    PortletPreferences portletPref = getPortletPreferences() ;
    String workspace = portletPref.getValue(Utils.WORKSPACE_NAME, "") ;
    return workspace ;
  }
  
  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }
}