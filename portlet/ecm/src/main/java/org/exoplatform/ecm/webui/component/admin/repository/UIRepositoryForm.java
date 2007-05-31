/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.BinarySwapEntry;
import org.exoplatform.services.jcr.config.ReplicationEntry;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.config.RepositoryServiceConfigurationImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.component.validator.NumberFormatValidator;
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
    template = "system:/groovy/webui/component/UIForm.gtmpl",   
    events = {
      @EventConfig(listeners = UIRepositoryForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.ResetActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.CloseActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.AddWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, confirm = "UIRepositoryForm.msg.confirm-delete", listeners = UIRepositoryForm.RemoveWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.EditWorkspaceActionListener.class)
    }  
)
public class UIRepositoryForm extends UIForm implements UIPopupComponent {  
  final  static public String ST_ADD = "AddRepoPopup" ;
  final  static public String ST_EDIT = "EditRepoPopup" ;
  final static public String POPUP_WORKSPACE = "PopupWorkspace" ;
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_SET = "inputSet" ; 
  final static public String FIELD_WORKSPACE = "workspace" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;
  final static public String FIELD_ACCESSCONTROL = "accessControl" ;
  final static public String FIELD_AUTHENTICATION = "authenticationPolicy" ;
  final static public String FIELD_SCURITY  = "securityDomain" ;
  final static public String FIELD_SESSIONTIME = "sessionTime" ;

  final static public String FIELD_REPCHANNEL = "channelConfig" ;
  final static public String FIELD_REPENABLE = "enableReplication" ;
  final static public String FIELD_REPMODE = "repMode" ;
  final static public String FIELD_REPTESTMODE = "repTestMode" ;

  final static public String FIELD_BSEPATH = "directoryPath" ;
  final static public String FIELD_BSEMAXBUFFER = "maxBufferSize" ;

  public boolean isAddnew_ = true ;  
  public String defaulWorkspace_ = null ;
  private RepositoryEntry repo_ = null;
  private Map<String, WorkspaceEntry> workspaceMap_ = new HashMap<String, WorkspaceEntry>() ; 

  public UIRepositoryForm() throws Exception { 
    addChild(new UIFormStringInput(FIELD_NAME,FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ; 
    UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(FIELD_SET) ;
    uiInputSet.addUIFormInput(new UIFormInputInfo(FIELD_WORKSPACE, FIELD_WORKSPACE, null)) ;
    addUIComponentInput(uiInputSet) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_ISDEFAULT,FIELD_ISDEFAULT, null).setEditable(false)) ;
    addChild(new UIFormStringInput(FIELD_ACCESSCONTROL,FIELD_ACCESSCONTROL, null).addValidator(EmptyFieldValidator.class)) ;    
    addChild(new UIFormStringInput(FIELD_AUTHENTICATION,FIELD_AUTHENTICATION, null).addValidator(EmptyFieldValidator.class)) ;    
    addChild(new UIFormStringInput(FIELD_SCURITY,FIELD_SCURITY, null).addValidator(EmptyFieldValidator.class)) ;    
    addChild(new UIFormStringInput(FIELD_SESSIONTIME,FIELD_SESSIONTIME, null).addValidator(NumberFormatValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_REPCHANNEL,FIELD_REPCHANNEL, null)) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_REPENABLE,FIELD_REPENABLE, null)) ;
    addChild(new UIFormStringInput(FIELD_REPMODE,FIELD_REPMODE, null)) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_REPTESTMODE,FIELD_REPTESTMODE, null)) ;
    addChild(new UIFormStringInput(FIELD_BSEPATH,FIELD_BSEPATH, null)) ;
    addChild(new UIFormStringInput(FIELD_BSEMAXBUFFER,FIELD_BSEMAXBUFFER, null).addValidator(EmptyFieldValidator.class).
        addValidator(NumberFormatValidator.class)) ;
    setActions(new String[] {"Save","AddWorkspace", "Reset", "Close"}) ;
  }  

  public void refresh(ManageableRepository manaRepo) throws Exception{

    reset() ;
    workspaceMap_.clear() ;
    getUIStringInput(FIELD_NAME).setEditable(true) ;
    if(manaRepo != null) {
      RepositoryEntry repo = manaRepo.getConfiguration() ;
      if(isAddnew_) {      
        repo_ = null;
        defaulWorkspace_ = null ;
        refreshLabel() ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(isDefaultRepo(repo.getName())) ;
        getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).setValue(repo.getAccessControl()) ;      
        getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).setValue(repo.getAuthenticationPolicy()) ;      
        getUIStringInput(UIRepositoryForm.FIELD_SCURITY).setValue(repo.getSecurityDomain()) ;
        getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).setValue(String.valueOf(repo.getSessionTimeOut())) ;
        ReplicationEntry re = repo.getReplication() ;
        if(re != null) {
          getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setValue(re.getChannelConfig()) ;      
          getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(re.isEnabled()) ;
          getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setValue(re.getMode()) ;     
          getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setChecked(re.isTestMode()) ;
        }
        BinarySwapEntry bse = repo.getBinaryTemp() ;
        if(bse != null) {
          getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setValue(bse.getDirectoryPath()) ;     
          getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setValue(bse.getMaxBufferSize()) ;
        }
      } else {
        repo_ = repo ;
        defaulWorkspace_ = repo.getDefaultWorkspaceName() ;
        for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
          workspaceMap_.put(ws.getName(), ws) ;
        }
        getUIStringInput(FIELD_NAME).setEditable(false) ;
        getUIStringInput(FIELD_NAME).setValue(repo.getName()) ;
        refreshLabel() ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(isDefaultRepo(repo.getName())) ;
        getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).setValue(repo.getAccessControl()) ;      
        getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).setValue(repo.getAuthenticationPolicy()) ;      
        getUIStringInput(UIRepositoryForm.FIELD_SCURITY).setValue(repo.getSecurityDomain()) ;
        getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).setValue(String.valueOf(repo.getSessionTimeOut())) ;
        ReplicationEntry re = repo.getReplication() ;
        if(re != null) {
          getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setValue(re.getChannelConfig()) ;      
          getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(re.isEnabled()) ;
          getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setValue(re.getMode()) ;     
          getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setChecked(re.isTestMode()) ;
        }
        BinarySwapEntry bse = repo.getBinaryTemp() ;
        if(bse != null) {
          getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setValue(bse.getDirectoryPath()) ;     
          getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setValue(bse.getMaxBufferSize()) ;
        }   
      }
    }
  }

  public RepositoryEntry getRepo() {return repo_ ;} 

  public boolean isExistWorkspace(String workspaceName){
    return workspaceMap_.containsKey(workspaceName) ;
  }

  public WorkspaceEntry getWorkspace(String workspaceName) {
    return workspaceMap_.get(workspaceName) ;
  }

  public Map<String, WorkspaceEntry> getWorkspaceMap() {
    return workspaceMap_ ;
  }

  public void removeWorkspace(String workspaceName) {
    workspaceMap_.remove(workspaceName) ;
  }

  public void refreshLabel() {
    StringBuilder labels = new StringBuilder() ;
    for(String wsName : workspaceMap_.keySet()){
      if(labels.length() > 0) labels.append(",") ;
      labels.append(wsName) ;
    }
    UIFormInputSetWithAction workspaceField = getChildById(UIRepositoryForm.FIELD_SET) ;
    workspaceField.setInfoField(UIRepositoryForm.FIELD_WORKSPACE, labels.toString()) ;
    String[] actionInfor = {"EditWorkspace", "RemoveWorkspace"} ;
    workspaceField.setActionInfo(UIRepositoryForm.FIELD_WORKSPACE, actionInfor) ;
  }

  public boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;    
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }
  public boolean isDefaultWorkspace(String workspaceName) {
    return workspaceName.equals(defaulWorkspace_) ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    repo_ = null ;
  }

  public static class SaveActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      if (uiForm.getWorkspaceMap().isEmpty()) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-isrequire", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      RepositoryService rservice = uiForm.getApplicationComponent(RepositoryService.class) ;
      RepositoryServiceConfigurationImpl config = (RepositoryServiceConfigurationImpl)rservice.getConfig() ;
      ManageableRepository defRep = (ManageableRepository) rservice.getRepository(uiControl.repoName_);
      if(config.canSave()) {
        for(WorkspaceEntry ws : uiForm.getWorkspaceMap().values()) {
          if(!defRep.isWorkspaceInitialized(ws.getName())) {
            defRep.configWorkspace(ws);
            defRep.createWorkspace(ws.getName());

          }
        }
        config.saveConfiguration() ;
      }
      /*String repoName = uiForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
      String acess = uiForm.getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).getValue() ;
      String authen = uiForm.getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).getValue() ;
      String security = uiForm.getUIStringInput(UIRepositoryForm.FIELD_SCURITY).getValue() ;
      String session = uiForm.getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).getValue() ;
      String chanel = uiForm.getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).getValue() ;     
      Boolean repEnable = uiForm.getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).isChecked() ;
      String mode = uiForm.getUIStringInput(UIRepositoryForm.FIELD_REPMODE).getValue() ;     
      Boolean testMode = uiForm.getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).isChecked() ;
      String path = uiForm.getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).getValue() ;     
      String buffer = uiForm.getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).getValue() ;   
      RepositoryEntry re = new RepositoryEntry() ;
      re.setName(repoName) ;
      for(WorkspaceEntry ws : uiForm.getWorkspaceMap().values()){re.addWorkspace(ws) ;}
      re.setDefaultWorkspaceName(uiForm.defaulWorkspace_) ;
      re.setAccessControl(acess) ;
      re.setAuthenticationPolicy(authen) ;
      ReplicationEntry repl = new ReplicationEntry() ;
      repl.setChannelConfig(chanel) ;
      repl.setEnabled(repEnable) ;
      repl.setMode(mode) ;
      repl.setTestMode(testMode) ;
      re.setReplication(repl) ;
      BinarySwapEntry bse = new BinarySwapEntry() ;
      bse.setDirectoryPath(path) ;
      bse.setMaxBufferSize(buffer) ;
      re.setBinaryTemp(bse) ;
      re.setSecurityDomain(security) ;
      re.setSessionTimeOut(Long.parseLong(session)) ;
      uiControl.reloadValue() ;*/
      
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;    
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ; 
    }
  }
  public static class ResetActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      //uiForm.refresh(uiForm.repo_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public static class AddWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;      
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiPopupAction = uiControl.getChild(UIPopupAction.class) ;
      UIWorkspaceWizard uiWorkspaceWizard = uiPopupAction.activate(UIWorkspaceWizard.class, 600) ; 
      RepositoryService rservice =uiForm.getApplicationComponent(RepositoryService.class) ;
      WorkspaceEntry wsdf = null ;
      for(WorkspaceEntry ws : rservice.getDefaultRepository().getConfiguration().getWorkspaceEntries()) {
        if(ws.getName().equals(rservice.getDefaultRepository().getConfiguration().getDefaultWorkspaceName())) {
          wsdf = ws ;
          break ;
        }
      }
      uiWorkspaceWizard.isAddnew_ = true ;
      uiWorkspaceWizard.refresh(wsdf) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  public static class CloseActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      uiForm.refresh(null) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  public static class EditWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      /*String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiPopupAction = uiControl.getChild(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      UIWorkspaceWizard uiWorkspaceWizard = uiPopupAction.activate(UIWorkspaceWizard.class, 600) ; 
      uiWorkspaceWizard.refresh(uiForm.getWorkspace(workspaceName)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;*/
    }
  }
  public static class RemoveWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;     
      if((!uiForm.isAddnew_)&&(uiForm.isDefaultWorkspace(workspaceName))) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        Object[] args = new Object[]{workspaceName}  ;        
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.default-workspace", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      uiForm.removeWorkspace(workspaceName) ;
      uiForm.refreshLabel() ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;  
    }
  }
}
