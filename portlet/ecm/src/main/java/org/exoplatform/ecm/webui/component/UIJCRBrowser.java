/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:12:26 PM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIJCRBrowser extends UIContainer implements ComponentSelector{
  
  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  private String repository_ = null;
  private String wsName_ = null ;
  private boolean isDisable_ = false ;
  private SessionProvider sessionProvider_ ; 
  
  public UIJCRBrowser() throws Exception {
    addChild(UIWorkspaceList.class, null, null) ;
    addChild(UITreeJCRExplorer.class, null, null) ;
    addChild(UIDefaultListItem.class, null, null) ;    
    this.sessionProvider_ = 
      SessionsUtils.getSessionProvider(getApplicationComponent(SessionProviderService.class)) ;
  }
  
  public void setSessionProvider(SessionProvider provider) { this.sessionProvider_ = provider ; }
  public SessionProvider getSessionProvider() { return this.sessionProvider_ ; }
  
  public void setRootPath(String path) throws Exception{
    getChild(UITreeJCRExplorer.class).setRootPath(path) ;
  }
  
  public void setFilterType(String[] arrType) throws Exception {
    getChild(UITreeList.class).setFilterType(arrType) ;
  }
  
  public void setMimeTypes(String[] arrMimeType) {
    getChild(UITreeList.class).setMimeTypes(arrMimeType) ;
  }
  
  public void setIsTab(boolean isTab) { 
    getChild(UITreeJCRExplorer.class).setIsTab(isTab) ;
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    setWorkspace(wsName) ;
    isDisable_ = isDisable ;
    getChild(UIWorkspaceList.class).setIsDisable(wsName, isDisable) ;
  }
  
  public boolean isDisable() { return isDisable_ ; }
  
  public void setWorkspace(String wsName) { wsName_ = wsName ; }
  
  public String getWorkspace() throws Exception { 
    if(wsName_ == null || wsName_.trim().length() ==0) {
      String[] wsNames = 
        getApplicationComponent(RepositoryService.class).getRepository(repository_).getWorkspaceNames();
      return wsNames[0] ;
    }
    return wsName_ ; 
  }
  
  public void setRepository(String repo) {
    repository_ = repo ; 
    try {
      UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class) ;
      uiWorkspaceList.setWorkspaceList(repository_) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public String getRepository() { return repository_ ; }
  
  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }
  
  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }
}
