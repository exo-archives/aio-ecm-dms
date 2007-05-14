/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.HashMap;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 9, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIRepositoryManager extends UIContainer {
  public UIRepositoryManager() throws Exception {
    addChild(UIRepositoryControl.class, null, null) ;
  }

  public void initPopup(String popupId ,UIComponent uiForm) throws Exception {
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setWindowSize(560,400) ;      
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public void initPopupPermission(String popupId, UIRepositoryForm uiForm) throws Exception {
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = createUIComponent(UIECMPermissionBrowser.class, null, null) ;
    uiECMPermission.setComponent(uiForm, null) ;
    uiPopup.setUIComponent(uiECMPermission);
    uiPopup.setShow(true) ;
    uiPopup.setRendered(true) ;
    uiPopup.setResizable(true) ;
  }
  public static class WorkspaceData extends WorkspaceEntry {
    String name ;
    String description ;
    boolean isDefault ;
    String sourceName ;
    String dbType ;
    boolean isMulti ;
    boolean isUpdateStore ;
    long maxBuffer ;
    String swapPath ;
    String storePath ;
    String filterType ;
    String indexPath ;
    boolean isCache ;
    long maxCacheSize ;
    long cacheLiveTime ;
    
    public WorkspaceData() {}
    
    public WorkspaceData(String name, String description, boolean isDefault, 
        String sourceName, String dbType, boolean isMulti, boolean isUpdateStore,
        long maxBuffer, String swapPath, String storePath, String filterType, 
        String indexPath, boolean isCache, long maxCacheSize, long cacheLiveTime) {
      
      this.name = name ;
      this.description = description ;
      this.isDefault = isDefault ;
      this.sourceName = sourceName ;
      this.dbType = dbType ;
      this.isMulti = isMulti ;
      this.isUpdateStore = isUpdateStore ;
      this.maxBuffer = maxBuffer ;
      this.swapPath = swapPath ;
      this.storePath = storePath ;
      this.filterType = filterType ;
      this.indexPath = indexPath ;
      this.isCache = isCache ;
      this.maxCacheSize = maxCacheSize ;
      this.cacheLiveTime = cacheLiveTime ;
    }
    public String getName() {return name ;}
    public String getDescription() {return description ;}
    public boolean isDefault() {return isDefault ;}
    public String getSourceName() {return sourceName ;}
    public String getDbType() {return dbType ;}
    public boolean isMulti() {return isMulti ;}
    public boolean isUpdateStore() {return isUpdateStore ;}
    public long getMaxBuffer() {return maxBuffer ;}
    public String getSwapPath() {return swapPath ;}
    public String getStorePath() {return storePath ;}
    public String getFilterType() {return filterType ;}
    public String getIndexPath() {return indexPath ;}
    public boolean isCache() {return isCache ;}
    public long getCahceMaxSize() {return maxCacheSize;}
    public long getCacheLiveTime() {return cacheLiveTime ;}
  }
  
  public static class RepositoryData {
    String name ;
    String permissions ;
    String description ;
    boolean isDefault ;
    HashMap<String, WorkspaceData> workspaceMap = new HashMap<String, WorkspaceData>();

    public RepositoryData(String name, HashMap<String, WorkspaceData> workspace, String permissions, String description, boolean isDefault) {
      this.name = name;      
      this.permissions = permissions;
      this.description = description ;
      this.isDefault = isDefault ;
      this.workspaceMap = workspace ;
    }
    
    public HashMap<String, WorkspaceData> getWorkspaceMap(){return workspaceMap ;}
    
    public WorkspaceData getWorkspace(String wsName){return workspaceMap.get(wsName) ;}
    
    public String getName(){return name ;}
    public String getWorkspace(){
      StringBuilder workspaceAll = new StringBuilder() ; 
      for(String s: workspaceMap.keySet()) {
        workspaceAll.append("[").append(s).append("] ") ;
      }
     return workspaceAll.toString() ;
    }
    public String getPermissions(){return permissions ;}
    public String isDefault(){return String.valueOf(isDefault) ;}
  }
}
