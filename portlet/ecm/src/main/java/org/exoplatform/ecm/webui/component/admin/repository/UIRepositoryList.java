/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.repository.UIWorkspaceForm.WorkspaceData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 9, 2007  
 */
@ComponentConfig(
    template = "app:groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIRepositoryList.AddRepoActionListener.class),
        @EventConfig(listeners = UIRepositoryList.EditInfoActionListener.class),
        @EventConfig(listeners = UIRepositoryList.ViewActionListener.class),
        @EventConfig(listeners = UIRepositoryList.DeleteActionListener.class)
    }
) 
public class UIRepositoryList extends UIGrid {

  final static public String[] ACTIONS = {"AddRepo"} ;
  final  static public String ST_ADD = "AddRepoPopup" ;
  final  static public String ST_EDIT = "EditRepoPopup" ;
  private static String[] REPO_BEAN_FIELD = {"name", "workspace", "permissions", "default"} ;
  private static String[] REPO_ACTION = {"EditInfo", "View", "Delete"} ;  

  public HashMap<String, RepositoryData> repositoryMap_ = new HashMap<String, RepositoryData>() ;

  public UIRepositoryList() throws Exception{
    configure("name", REPO_BEAN_FIELD, REPO_ACTION) ;    
    initData() ;
    updateGrid() ;
  }

  public String[] getActions() { return ACTIONS ; }

  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList (getRespositories(), 10) ;
    getUIPageIterator().setPageList(objPageList) ;    
  }

  //TODO Check this code againt when JCR complete
  //repository.getReporitoryList()  
  public void initData() throws Exception {
    List<ManageableRepository> reporitoryList = new ArrayList<ManageableRepository>() ;    
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    ManageableRepository repository = rservice.getRepository() ;          
    reporitoryList.add(repository) ;
    for(ManageableRepository repo : reporitoryList) {     
      List<WorkspaceEntry> wslist = repo.getConfiguration().getWorkspaceEntries() ;
      HashMap<String, WorkspaceData> workSpaces = new HashMap<String, WorkspaceData>() ;
      for (WorkspaceEntry wse : wslist) {
        WorkspaceData wsd = new WorkspaceData(wse.getName(), "", true,true ,wse.getCache().isEnabled(),0,0) ;        
        workSpaces.put(wsd.getName(), wsd) ;
      }     
      String perms = "[*]" ;      
      RepositoryData repoData = new RepositoryData(repo.getConfiguration().getName(),
          workSpaces, perms,"", true) ;
      repositoryMap_.put(repo.getConfiguration().getName(), repoData) ;
    }    
  }

  public List<RepositoryData> getRespositories() throws Exception {
    List<RepositoryData> repoList = new ArrayList<RepositoryData>() ;
    for(String k : repositoryMap_.keySet()) {
      repoList.add(repositoryMap_.get(k)) ;
    }
    return repoList ;
  }

  public HashMap<String, RepositoryData> getRepsitoryMap() {return repositoryMap_ ;}  
  public void addRepository(RepositoryData repoData) {repositoryMap_.put(repoData.getName(), repoData) ;} 
  public boolean isExistRepo(String repoName) {return repositoryMap_.keySet().contains(repoName) ;}
  public RepositoryData getRepositoryData(String repoName) {
    return repositoryMap_.get(repoName) ;
  }
  public static class AddRepoActionListener extends EventListener<UIRepositoryList>{                         
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIRepositoryManager uiRepoManager = event.getSource().getParent() ;
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION);
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE);
      uiRepoManager.removeChildById(UIRepositoryList.ST_EDIT);
      UIRepositoryForm uiForm = uiRepoManager.createUIComponent(UIRepositoryForm.class, null, null) ;
      uiForm.refresh(null) ;
      uiRepoManager.initPopup(UIRepositoryList.ST_ADD, uiForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoManager) ;
    }
  }


  public static class ViewActionListener extends EventListener<UIRepositoryList>{                         
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIRepositoryList uiList = event.getSource() ;
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIECMAdminPortlet uiPortlet = uiList.getAncestorOfType(UIECMAdminPortlet.class) ;
      uiPortlet.setRepoName(repoName) ;
      uiPortlet.setSelectedRepo(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }

  public static class EditInfoActionListener extends EventListener<UIRepositoryList>{                         
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIRepositoryList uiList = event.getSource() ;
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIRepositoryManager uiRepoManager = uiList.getAncestorOfType(UIRepositoryManager.class) ;
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION);
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE);
      uiRepoManager.removeChildById(UIRepositoryList.ST_ADD);
      uiRepoManager.removeChildById(UIRepositoryList.ST_EDIT);
      RepositoryData repo = uiList.getRepositoryData(repoName) ;      
      UIRepositoryForm uiForm = uiRepoManager.createUIComponent(UIRepositoryForm.class, null, null) ;
      uiForm.refresh(repo) ;
      uiRepoManager.initPopup(UIRepositoryList.ST_EDIT, uiForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoManager) ;
    }
  }
  public static class DeleteActionListener extends EventListener<UIRepositoryList>{                         
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIRepositoryList uiList = event.getSource() ;
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(Boolean.parseBoolean(uiList.getRepositoryData(repoName).isDefault())) {
        UIApplication uiApp = uiList.getAncestorOfType(UIApplication.class) ;
        Object[] args = new Object[]{repoName}  ;        
        uiApp.addMessage(new ApplicationMessage("UIRepositoryList.msg.cannot-delete-defult", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      uiList.getRepsitoryMap().remove(repoName) ;
      uiList.updateGrid() ;
      UIRepositoryManager uiRepoManager = uiList.getAncestorOfType(UIRepositoryManager.class) ;
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION);
      uiRepoManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE);
      uiRepoManager.removeChildById(UIRepositoryList.ST_ADD);
      uiRepoManager.removeChildById(UIRepositoryList.ST_EDIT);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoManager) ;
    }
  }

  public static class RepositoryData {
    String name ;
    String workspace ;
    String permissions ;
    String description ;
    boolean isDefault ;
    HashMap<String, WorkspaceData> workspaceMap = new HashMap<String, WorkspaceData>();

    public RepositoryData(String name, HashMap<String, WorkspaceData> workspace, String permissions, String description, boolean isDefault) {
      this.name = name;      
      this.permissions = permissions;
      this.description = description ;
      this.isDefault = isDefault ;
      workspaceMap = workspace ;
      setWorkspace() ;
    } 
    public HashMap<String, WorkspaceData> getWorkspaceMap(){return workspaceMap ;}
    public WorkspaceData getWorkspace(String wsName){return workspaceMap.get(wsName) ;}
    public String getName(){return name ;}
    public String getWorkspace(){
      return workspace ;
    }
    public void setWorkspace() {
      StringBuilder workspaceAll = new StringBuilder() ; 
      for(String s: workspaceMap.keySet()) {
        workspaceAll.append("[").append(s).append("] ") ;
      }
      workspace = workspaceAll.toString() ;
    }
    public String getPermissions(){return permissions ;}
    public String isDefault(){return String.valueOf(isDefault) ;}
  }
}
