/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.HashMap;
import java.util.Set;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryList.RepositoryData;
import org.exoplatform.ecm.webui.component.admin.repository.UIWorkspaceForm.WorkspaceData;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 9, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",   
    events = {
      @EventConfig(listeners = UIRepositoryForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.ResetActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.CloseActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.AddPermissionActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.AddWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.RemoveWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.EditWorkspaceActionListener.class)
    }  
)
public class UIRepositoryForm extends UIForm implements UISelector {  
  final static public String POPUP_PERMISSION = "PopupPermission" ;
  final static public String POPUP_WORKSPACE = "PopupWorkspace" ;
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_PERM = "permission" ; 
  final static public String FIELD_SET = "inputSet" ; 
  final static public String FIELD_WORKSPACE = "workspace" ;
  final static public String FIELD_DESCRIPTION = "description" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;
  public boolean isAddnew_ = true ;  
  private HashMap<String, WorkspaceData> workspaces_ = new HashMap<String, WorkspaceData>() ;  
  
  public UIRepositoryForm() throws Exception { 
    addChild(new UIFormStringInput(FIELD_NAME,FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ;    
    UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(FIELD_SET) ;
    uiInputSet.addUIFormInput(new UIFormStringInput(FIELD_PERM, FIELD_PERM, null).
                               addValidator(EmptyFieldValidator.class).setEditable(false)) ;
    uiInputSet.setActionInfo(FIELD_PERM, new String[] {"AddPermission"}) ;    
    uiInputSet.addUIFormInput(new UIFormInputInfo(FIELD_WORKSPACE, FIELD_WORKSPACE, null)) ;
    addUIComponentInput(uiInputSet) ;
    addChild(new UIFormTextAreaInput(FIELD_DESCRIPTION,FIELD_WORKSPACE, null)) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_ISDEFAULT,FIELD_ISDEFAULT, null).setEditable(false)) ;
    setActions(new String[] {"Save","AddWorkspace", "Reset", "Close"}) ;
  }  
  
  public void refresh(RepositoryData repo){
    if(repo == null) {
      reset() ;    
      isAddnew_ = true ;
    } else {
      getUIStringInput(FIELD_NAME).setValue(repo.getName()) ;
      UIFormInputSetWithAction uiInputSet = getChildById(FIELD_SET) ;
      uiInputSet.getUIStringInput(FIELD_PERM).setValue(repo.getPermissions()) ;
      workspaces_.clear() ;   
      workspaces_ = repo.getWorkspaceMap() ;
      refreshLabel() ;
      isAddnew_ = false ;
    }
  }
  public HashMap<String ,WorkspaceData> getWorkspaceMap() {return workspaces_  ;}
  public void addWorkspaceMap(WorkspaceData ws) {workspaces_.put(ws.getName(), ws) ;}
  
  public boolean isExistWorkspace(String workspaceName){
    return workspaces_.keySet().contains(workspaceName) ;
  }
  
  public WorkspaceData getWorkspace(String workspaceName) {
   return workspaces_.get(workspaceName) ;
  }
  
  public void refreshLabel() {
    StringBuilder labels = new StringBuilder() ;
    for(String s : getWorkspaceMap().keySet()){
      if(labels.length() > 0) labels.append(",") ;
      labels.append(s) ;
    }
    UIFormInputSetWithAction workspaceField = getChildById(UIRepositoryForm.FIELD_SET) ;
    workspaceField.setInfoField(UIRepositoryForm.FIELD_WORKSPACE, labels.toString()) ;
    String[] actionInfor = {"EditWorkspace", "RemoveWorkspace"} ;
    workspaceField.setActionInfo(UIRepositoryForm.FIELD_WORKSPACE, actionInfor) ;
  }
  
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    getUIStringInput(FIELD_PERM).setValue(value) ;
    UIRepositoryManager uiManager = getAncestorOfType(UIRepositoryManager.class) ;
    uiManager.removeChildById(POPUP_PERMISSION) ;
  }
  
  public static class SaveActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      String repoName = uiForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
      String repoDes = uiForm.getUIStringInput(UIRepositoryForm.FIELD_DESCRIPTION).getValue() ;
      String repoPerm = uiForm.getUIStringInput(UIRepositoryForm.FIELD_PERM).getValue() ;
      Set<String> workSpaceSet = uiForm.getWorkspaceMap().keySet() ;      
      boolean isDefault = uiForm.getUIFormCheckBoxInput(UIRepositoryForm.FIELD_ISDEFAULT).isChecked() ;
      UIRepositoryList uiList = uiManager.findFirstComponentOfType(UIRepositoryList.class) ;      
      if(uiForm.isAddnew_) {
        if (uiList.isExistRepo(repoName)) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          Object[] args = new Object[]{repoName}  ;        
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ; 
        }
      }
      if(workSpaceSet.size() <= 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-isrequire", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      RepositoryData repoData = new RepositoryData(repoName,uiForm.getWorkspaceMap(),repoPerm,repoDes, isDefault) ;
      uiList.addRepository(repoData) ;
      uiList.updateGrid() ;
      uiForm.refresh(null) ;
      uiForm.workspaces_.clear() ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      uiManager.removeChildById(UIRepositoryList.ST_ADD) ;
      uiManager.removeChildById(UIRepositoryList.ST_EDIT) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ; 
      
    }
  }
  public static class ResetActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      uiForm.refresh(null) ;
    }
  }
  public static class AddPermissionActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;   
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      uiManager.initPopupPermission(UIRepositoryForm.POPUP_PERMISSION, uiForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;      
    }
  }
  public static class AddWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      UIWorkspaceForm uiWorkspaceForm = uiManager.createUIComponent(UIWorkspaceForm.class, null, null) ;
      uiManager.initPopup(UIRepositoryForm.POPUP_WORKSPACE, uiWorkspaceForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  public static class CloseActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      uiForm.refresh(null) ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      uiForm.workspaces_.clear() ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      uiManager.removeChildById(UIRepositoryList.ST_ADD) ;
      uiManager.removeChildById(UIRepositoryList.ST_EDIT) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;  
    }
  }
  public static class EditWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      UIWorkspaceForm uiWorkspaceForm = uiManager.createUIComponent(UIWorkspaceForm.class, null, null) ;      
      uiWorkspaceForm.refresh(uiForm.getWorkspace(workspaceName)) ;
      uiManager.initPopup(UIRepositoryForm.POPUP_WORKSPACE, uiWorkspaceForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  public static class RemoveWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_PERMISSION) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      uiForm.getWorkspaceMap().remove(workspaceName) ;
      uiForm.refreshLabel() ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;  
    }
  }
}
