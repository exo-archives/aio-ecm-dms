/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
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
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 9, 2007  
 */

@ComponentConfig(  
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",   
    events = {
      @EventConfig(listeners = UIRepositoryForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.ResetActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.CloseActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.AddWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.RemoveWorkspaceActionListener.class),
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

  protected boolean isAddnew_ = true ;  
  protected String defaulWorkspace_ = null ;
  protected RepositoryEntry repo_ = null;
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
    addChild(new UIFormStringInput(FIELD_SESSIONTIME,FIELD_SESSIONTIME, null).addValidator(EmptyFieldValidator.class) 
        .addValidator(NumberFormatValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_REPCHANNEL,FIELD_REPCHANNEL, null)) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_REPENABLE,FIELD_REPENABLE, null)) ;
    addChild(new UIFormStringInput(FIELD_REPMODE,FIELD_REPMODE, null)) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_REPTESTMODE,FIELD_REPTESTMODE, null)) ;
    addChild(new UIFormStringInput(FIELD_BSEPATH,FIELD_BSEPATH, null)) ;
    addChild(new UIFormStringInput(FIELD_BSEMAXBUFFER,FIELD_BSEMAXBUFFER, null).addValidator(EmptyFieldValidator.class).
        addValidator(NumberFormatValidator.class)) ;
  }  

  public void refresh(RepositoryEntry repo) throws Exception{
    reset() ;
    getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(false) ;
    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setChecked(false) ;
    workspaceMap_.clear() ;
    if(repo != null) {
      if(isAddnew_) {      
        repo_ = null;
        defaulWorkspace_ = null ;
        refreshLabel() ;
        getUIStringInput(FIELD_NAME).setEditable(true) ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
        setActions(new String[] {"Save","AddWorkspace", "Reset", "Close"}) ;
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
        setActions(new String[] {"AddWorkspace", "Reset", "Close"}) ;
      }
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
  protected boolean isDefaultWorkspace(String workspaceName) {
    return workspaceName.equals(defaulWorkspace_) ;
  }
  protected RepositoryEntry getCurrentRepo() {return repo_ ;} 

  protected void setRepo(RepositoryEntry repo) {repo_ = repo ;} 

  protected boolean isExistWorkspace(String workspaceName){
    return workspaceMap_.containsKey(workspaceName) ;
  }

  protected WorkspaceEntry getWorkspace(String workspaceName) {
    return workspaceMap_.get(workspaceName) ;
  }

  protected Map<String, WorkspaceEntry> getWorkspaceMap() {
    return workspaceMap_ ;
  }

  protected void refreshLabel() {
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

  protected boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;    
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }

  protected void saveRepo() throws Exception {    
    InitialContextInitializer ic = (InitialContextInitializer)getApplicationComponent(ExoContainer.class).
    getComponentInstanceOfType(InitialContextInitializer.class) ;
    if(ic != null) ic.recall() ;
    RepositoryService rservice = (RepositoryService)getApplicationComponent(ExoContainer.class).
    getComponentInstanceOfType(RepositoryService.class);
    RepositoryEntry repositoryEntry = getCurrentRepo() ;
    if(isAddnew_){
      for(WorkspaceEntry ws : getWorkspaceMap().values()){   
        getCurrentRepo().addWorkspace(ws) ;
      }
      getCurrentRepo().setSystemWorkspaceName(defaulWorkspace_) ;
      getCurrentRepo().setDefaultWorkspaceName(defaulWorkspace_) ;
      try { rservice.createRepository(getCurrentRepo()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    } else {
      RepositoryImpl defRep = (RepositoryImpl)rservice.getRepository(repositoryEntry.getName()) ;
      for(WorkspaceEntry ws : getWorkspaceMap().values()){      
        if(!defRep.isWorkspaceInitialized(ws.getName())) {
          defRep.configWorkspace(ws);
          defRep.createWorkspace(ws.getName());
        }
      }
    }
    if(rservice.getConfig().isRetainable()) { rservice.getConfig().retain() ;}
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception { repo_ = null ;}

  public static class SaveActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class);
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ;
      RepositoryEntry re ;
      if(uiForm.isAddnew_) re = new RepositoryEntry() ;
      else re = uiForm.repo_ ;
      String repoName = uiForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
      RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
      for(Object obj : rService.getConfig().getRepositoryConfigurations()) { 
        RepositoryEntry repo  = (RepositoryEntry)obj ;
        if(repo.getName().equals(repoName) && uiForm.isAddnew_) {
          Object[] args = new Object[]{repo.getName()}  ;    
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
      }
      if (uiForm.getWorkspaceMap().isEmpty()) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-isrequire", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      if (uiForm.defaulWorkspace_ == null) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-setdefault", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
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
      re.setName(repoName) ;
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
      uiForm.setRepo(re) ;
      uiForm.saveRepo() ;
      UIRepositoryControl uiRepoControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      uiRepoControl.reloadValue() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;    
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoControl) ;
    }
  }
  public static class ResetActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class);
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      if(uiForm.isAddnew_) uiForm.refresh(null) ;
      else uiForm.refresh(uiForm.repo_);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public static class AddWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;      
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class);
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIECMAdminPortlet.class).findFirstComponentOfType(UIPopupAction.class) ;
      UIPopupAction uiWorkspaceAction = uiControl.getChild(UIPopupAction.class) ;
      UIWorkspaceWizard uiWorkspaceWizard = uiWorkspaceAction.activate(UIWorkspaceWizard.class, 600) ; 
      RepositoryService rservice =uiForm.getApplicationComponent(RepositoryService.class) ;
      WorkspaceEntry wsdf = null ;
      RepositoryEntry repoEntry = null ;
      if(uiForm.isAddnew_) {
        repoEntry = rservice.getDefaultRepository().getConfiguration() ;
      } else {
        repoEntry = uiForm.repo_ ;
      }
      for(WorkspaceEntry ws : repoEntry.getWorkspaceEntries()) {
        if(ws.getName().equals(repoEntry.getDefaultWorkspaceName())) {
          wsdf = ws ;
          break ;
        }
      }
      uiWorkspaceWizard.isNewWizard_ = true ;
      uiWorkspaceWizard.refresh(wsdf) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  public static class CloseActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class) ;
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
      String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class) ;
      UIPopupAction uiPopupAction = uiControl.getChild(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      UIWorkspaceWizard uiWorkspaceWizard = uiPopupAction.activate(UIWorkspaceWizard.class, 600) ; 
      uiWorkspaceWizard.isNewWizard_ = false ;
      uiWorkspaceWizard.refresh(uiForm.getWorkspace(workspaceName)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  public static class RemoveWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class) ; 
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ; 
      String workspaceName = event.getRequestContext().getRequestParameter(OBJECTID) ;     
      if(uiForm.isAddnew_) {
        uiForm.workspaceMap_.remove(workspaceName) ;
      } else {
        RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
        ManageableRepository manaRepo = rService.getRepository(uiForm.repo_.getName()) ;
        if(manaRepo.canRemoveWorkspace(workspaceName)) {
          manaRepo.removeWorkspace(workspaceName) ;
          uiForm.workspaceMap_.clear() ;
          for(WorkspaceEntry ws : manaRepo.getConfiguration().getWorkspaceEntries()) {
            uiForm.workspaceMap_.put(ws.getName(), ws) ;
          }
        } else {
          Object[] args = new Object[]{workspaceName}  ;    
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.cannot-delete-workspace", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
      }
      if(uiForm.isDefaultWorkspace(workspaceName)) uiForm.defaulWorkspace_ = null ;
      uiForm.refreshLabel() ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;  
    }
  }
}
