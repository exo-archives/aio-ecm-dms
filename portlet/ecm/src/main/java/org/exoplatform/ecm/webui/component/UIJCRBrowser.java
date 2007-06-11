/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import org.exoplatform.ecm.jcr.ComponentSelector;
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
  private String wsName_ = null ;
  
  public UIJCRBrowser() throws Exception {
    addChild(UITreeJCRExplorer.class, null, null) ;
    addChild(UIDefaultListItem.class, null, null) ;
  }
  
  public void setRootPath(String path) throws Exception{
    getChild(UITreeJCRExplorer.class).setRootPath(path) ;
  }
  
  public void setFilterType(String[] arrType) throws Exception {
    getChild(UITreeList.class).setFilterType(arrType) ;
  }
  
  public void setIsTab(boolean isTab) { 
    getChild(UITreeJCRExplorer.class).setIsTab(isTab) ;
  }
  
  public void setWorkspace(String wsName) { wsName_ = wsName ; }
  public String getWorkspace() { return wsName_ ; }
  
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
