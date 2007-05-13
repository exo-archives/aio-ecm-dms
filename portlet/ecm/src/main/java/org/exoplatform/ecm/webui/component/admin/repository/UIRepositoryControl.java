/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryManager.RepositoryData;
import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryManager.WorkspaceData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIDropDownItemSelector;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 11, 2007  
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/admin/UIRepositoryControl.gtmpl",
    events = {
        @EventConfig(listeners = UIRepositoryControl.ChangeOptionActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.EditRepositoryActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.RemoveRepositoryActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.AddRepositoryActionListener.class)
    }
)

public class UIRepositoryControl extends UIContainer {

  public HashMap<String, RepositoryData> repositoryMap_ = new HashMap<String, RepositoryData>() ;
  
  public UIRepositoryControl() throws Exception{
    UIDropDownItemSelector uiDropDown= addChild(UIDropDownItemSelector.class, null, null) ;
    initData() ;
    uiDropDown.setOptions(getRepoItem()) ;
    uiDropDown.setSelected(0) ;
    uiDropDown.setOnChange("ChangeOption") ;
    uiDropDown.setOnServer(true) ;
  }

  public void initData() throws Exception {
    List<ManageableRepository> reporitoryList = new ArrayList<ManageableRepository>() ;    
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    ManageableRepository repository = rservice.getRepository() ;          
    reporitoryList.add(repository) ;
    for(ManageableRepository repo : reporitoryList) {     
      List<WorkspaceEntry> wslist = repo.getConfiguration().getWorkspaceEntries() ;
      HashMap<String, WorkspaceData> workSpaces = new HashMap<String, WorkspaceData>() ;
      for (WorkspaceEntry wse : wslist) {
        String name = wse.getName() ;
        String description = "" ;
        boolean isDefault = true ;
        String sourceName = "jdbcjcr" ;
        String dbType = "mysq" ;
        boolean isMulti = true ;
        boolean isUpdateStore = true ; 
        long bufferValue = 204800 ;
        String swapPath ="../temp/swap/production" ;
        String storePath ="../temp/values/production" ;
        String filterType = "Binary" ;
        String indexPath = "../temp/jcrlucenedb/index" ;
        boolean isCache = true ;
        long maxSizeValue = 5000 ;
        long liveTimeValue = 30000 ;
        
        WorkspaceData wsd = new WorkspaceData(name, description, isDefault, sourceName, dbType,
            isMulti,isUpdateStore, bufferValue, swapPath, storePath, filterType, indexPath, 
            isCache,maxSizeValue, liveTimeValue ) ; 
        workSpaces.put(name, wsd) ;
      }     
      String perms = "[*]" ;      
      RepositoryData repoData = new RepositoryData(repo.getConfiguration().getName(),
          workSpaces, perms,"", true) ;
      repositoryMap_.put(repo.getConfiguration().getName(), repoData) ;
    }    
  }

  public List<SelectItemOption<String>> getRepoItem(){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(String repoName : repositoryMap_.keySet()) {
      options.add(new SelectItemOption<String>(repoName, repoName)) ;
    }
    return options ;
  }
  
  public RepositoryData getRepositoryData(String repoName) {
    return repositoryMap_.get(repoName) ;
  }
  
  public void removeRepository(String repoName) {
    repositoryMap_.remove(repoName) ;
  }
  
  public boolean isDefaultRepo(String repoName) {
    RepositoryData repo = getRepositoryData(repoName) ;
    if(repo != null) return Boolean.parseBoolean(repo.isDefault()) ;
    return false ;
  }
  public HashMap<String, RepositoryData> getRepsitoryMap() {return repositoryMap_ ;}  
  
  public void addRepository(RepositoryData repoData) {repositoryMap_.put(repoData.getName(), repoData) ;} 
  
  public boolean isExistRepo(String repoName) {return repositoryMap_.keySet().contains(repoName) ;}
  
  public static class ChangeOptionActionListener extends EventListener<UIRepositoryControl>{
    public void execute(Event<UIRepositoryControl> arg0) throws Exception {
      System.out.println("Come here");
    }
    
  }
 
  public static class EditRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiSelect.getSelected() ;
      UIRepositoryManager uiRepoManager = uiControl.getAncestorOfType(UIRepositoryManager.class) ;
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION);
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE);
      uiRepoManager.removeChildById(UIRepositoryForm.ST_ADD);
      uiRepoManager.removeChildById(UIRepositoryForm.ST_EDIT);
      RepositoryData repo = uiControl.getRepositoryData(repoName) ;      
      UIRepositoryForm uiForm = uiRepoManager.createUIComponent(UIRepositoryForm.class, null, null) ;
      uiForm.refresh(repo) ;
      uiRepoManager.initPopup(UIRepositoryForm.ST_EDIT, uiForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoManager) ;
    }
  }
  
  public static class RemoveRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiSelect.getSelected() ;
      if(uiControl.isDefaultRepo(repoName)) {
        UIApplication uiApp = uiControl.getAncestorOfType(UIApplication.class) ;
        Object[] args = new Object[]{repoName} ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryControl.msg.cannot-deleteRepo", args)) ;        
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
        return  ;
      }
      uiControl.removeRepository(repoName) ;
      uiSelect.setOptions(uiControl.getRepoItem()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ;
    }
  }
  public static class AddRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIRepositoryManager uiManager = uiControl.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION);
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE);
      uiManager.removeChildById(UIRepositoryForm.ST_EDIT);
      UIRepositoryForm uiForm = uiManager.createUIComponent(UIRepositoryForm.class, null, null) ;
      uiForm.refresh(null) ;
      uiManager.initPopup(UIRepositoryForm.ST_ADD, uiForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }


}
