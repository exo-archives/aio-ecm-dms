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
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryValueSelect.ClassData;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
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
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.SelectActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.ResetActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.CloseActionListener.class),
      @EventConfig(listeners = UIRepositoryForm.AddWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.ShowHiddenActionListener.class),
      @EventConfig(listeners = UIRepositoryForm.RemoveWorkspaceActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRepositoryForm.EditWorkspaceActionListener.class)
    }  
)
public class UIRepositoryForm extends UIForm implements UIPopupComponent {  
  final  static public String ST_ADD = "AddRepoPopup" ;
  final  static public String ST_EDIT = "EditRepoPopup" ;
  final static public String POPUP_WORKSPACE = "PopupWorkspace" ;
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_WSINPUTSET = "wsInputSet" ; 
  final static public String FIELD_WORKSPACE = "workspace" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;
  final static public String FIELD_ACCESSCONTROL = "accessControl" ;
  final static public String FIELD_AUTHINPUTSET = "authInputSet" ;
  final static public String FIELD_AUTHENTICATION = "authenticationPolicy" ;

  final static public String FIELD_SCURITY  = "securityDomain" ;
  final static public String FIELD_SESSIONTIME = "sessionTime" ;

  final static public String FIELD_REPCHANNEL = "channelConfig" ;
  final static public String FIELD_REPENABLE = "enableReplication" ;
  final static public String FIELD_REPMODE = "repMode" ;
  final static public String FIELD_REPTESTMODE = "repTestMode" ;

  final static public String FIELD_BSEPATH = "directoryPath" ;
  final static public String FIELD_BSEMAXBUFFER = "maxBufferSize" ;
  final static public String KEY_AUTHENTICATIONPOLICY = "org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy" ;
  protected boolean isAddnew_ = true ;  
  protected String defaulWorkspace_ = null ;
  protected String repoName_ = null ;
  protected Map<String, WorkspaceEntry> workspaceMap_ = new HashMap<String, WorkspaceEntry>() ; 

  public UIRepositoryForm() throws Exception { 
    addChild(new UIFormStringInput(FIELD_NAME,FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ; 
    UIFormInputSetWithAction workspaceField = new UIFormInputSetWithAction(FIELD_WSINPUTSET) ;
    workspaceField.addUIFormInput(new UIFormInputInfo(FIELD_WORKSPACE, FIELD_WORKSPACE, null)) ;
    workspaceField.setActionInfo(FIELD_WORKSPACE, new String[]{"EditWorkspace", "RemoveWorkspace"}) ;
    addUIComponentInput(workspaceField) ;
    addChild(new UIFormCheckBoxInput<String>(FIELD_ISDEFAULT,FIELD_ISDEFAULT, null).setEditable(false)) ;
    addChild(new UIFormStringInput(FIELD_ACCESSCONTROL,FIELD_ACCESSCONTROL, null).addValidator(EmptyFieldValidator.class)) ;  
    UIFormInputSetWithAction autField = new UIFormInputSetWithAction(FIELD_AUTHINPUTSET) ;
    autField.addChild(new UIFormStringInput(FIELD_AUTHENTICATION, FIELD_AUTHENTICATION, null).addValidator(EmptyFieldValidator.class)) ;
    autField.setActionInfo(FIELD_AUTHENTICATION, new String[]{"Select"}) ;
    addChild(autField) ;
    addChild(new UIFormStringInput(FIELD_SCURITY,FIELD_SCURITY, null).addValidator(EmptyFieldValidator.class)) ;    
    addChild(new UIFormStringInput(FIELD_SESSIONTIME,FIELD_SESSIONTIME, null)) ;
//    UIFormCheckBoxInput<Boolean> bseCheckbox = new UIFormCheckBoxInput<Boolean>(FIELD_REPENABLE,FIELD_REPENABLE, null) ;
//    bseCheckbox.setOnChange("ShowHidden") ;
//    addChild(bseCheckbox) ;
//    addChild(new UIFormStringInput(FIELD_REPCHANNEL,FIELD_REPCHANNEL, null).setRendered(false)) ;
//    addChild(new UIFormStringInput(FIELD_REPMODE,FIELD_REPMODE, null).setRendered(false)) ;
//    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_REPTESTMODE,FIELD_REPTESTMODE, null).setRendered(false)) ;
//    addChild(new UIFormStringInput(FIELD_BSEPATH,FIELD_BSEPATH, null).setRendered(false)) ;
//    addChild(new UIFormStringInput(FIELD_BSEMAXBUFFER,FIELD_BSEMAXBUFFER, null).addValidator(NumberFormatValidator.class).setRendered(false)) ;
  }  

  public void refresh(RepositoryEntry repo) throws Exception{
    reset() ;
    getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
//    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(false) ;
//    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setChecked(false) ;
    UIFormInputSetWithAction autField = getChildById(FIELD_AUTHINPUTSET) ;
    workspaceMap_.clear() ;
    if(repo != null) {
      if(isAddnew_) {      
        repoName_ = null ;
        defaulWorkspace_ = null ;
        refreshWorkspaceList() ;
        getUIStringInput(FIELD_NAME).setEditable(true) ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(false) ;
        setActions(new String[] {"Save","AddWorkspace", "Reset", "Close"}) ;
//        getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(false) ;
//        getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setRendered(false) ;
//        getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setRendered(false) ;
//        getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setRendered(false) ;
//        getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setRendered(false) ;
//        getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setRendered(false);
      } else {
        repoName_ = repo.getName() ;
        defaulWorkspace_ = repo.getDefaultWorkspaceName() ;
        for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
          workspaceMap_.put(ws.getName(), ws) ;
        }
        getUIStringInput(FIELD_NAME).setEditable(false) ;
        getUIStringInput(FIELD_NAME).setValue(repo.getName()) ;
        refreshWorkspaceList() ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(isDefaultRepo(repo.getName())) ;
        getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(false);
        autField.setActionInfo(FIELD_AUTHENTICATION, null) ;
        setActions(new String[] {"AddWorkspace", "Close"}) ;
/*        
        //TODO: JCR doesn't support to use ReplicationEntry
        ReplicationEntry re = repo.getReplication() ;
        if(re != null) {
          getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setChecked(re.isEnabled()) ;
          if(re.isEnabled()) {
            getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setRendered(true) ;
            getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setValue(re.getChannelConfig()) ;  
            getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setRendered(true) ;
            getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setValue(re.getMode()) ;  
            getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setRendered(true) ;
            getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setChecked(re.isTestMode()) ;
            BinarySwapEntry bse = repo.getBinaryTemp() ;
            if(bse != null) {
              getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setRendered(true) ;
              getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setValue(bse.getDirectoryPath()) ;   
              getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setRendered(true);
              getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setValue(bse.getMaxBufferSize()) ;
            } 
          }
        }
*/        
      }
      getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).setValue(repo.getAccessControl()) ;      
      autField.getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).setValue(repo.getAuthenticationPolicy()) ;      
      getUIStringInput(UIRepositoryForm.FIELD_SCURITY).setValue(repo.getSecurityDomain()) ;
      getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).setValue(String.valueOf(repo.getSessionTimeOut())) ;
    }
  }
  protected void lockForm(boolean isLock) throws Exception {
    boolean editable = !isLock ;
    UIFormInputSetWithAction autField = getChildById(FIELD_AUTHINPUTSET) ;
//    UIFormInputSetWithAction workspaceField = getChildById(FIELD_WSINPUTSET) ;
   // workspaceField.setIsView(isLock);
    if(isLock) {
      autField.setActionInfo(FIELD_AUTHENTICATION, null) ;
    } else {
      autField.setActionInfo(FIELD_AUTHENTICATION, new String[]{"Select"}) ;
    }
//    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).setEnable(editable) ;
    getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).setEditable(editable); 
    autField.getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).setEditable(editable);     
    getUIStringInput(UIRepositoryForm.FIELD_SCURITY).setEditable(editable); 
    getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).setEditable(editable);
//    getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).setEditable(editable) ;
//    getUIStringInput(UIRepositoryForm.FIELD_REPMODE).setEditable(editable) ;
//    getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).setEnable(editable) ;;
//    getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).setEditable(editable) ;
//    getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).setEditable(editable) ;
  }
  protected boolean isDefaultWorkspace(String workspaceName) {
    return workspaceName.equals(defaulWorkspace_) ;
  }
  protected boolean isExistWorkspace(String workspaceName){
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    for(RepositoryEntry repo : rservice.getConfig().getRepositoryConfigurations() ) {
      for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
        if( ws.getName().equals(workspaceName)) return true ;
      }
    }
    return false ; 
  }

  protected WorkspaceEntry getWorkspace(String workspaceName) {
    return workspaceMap_.get(workspaceName) ;
  }

  protected Map<String, WorkspaceEntry> getWorkspaceMap() {
    return workspaceMap_ ;
  }

  protected void refreshWorkspaceList() {
    StringBuilder labels = new StringBuilder() ;
    for(String wsName : workspaceMap_.keySet()){
      if(labels.length() > 0) labels.append(",") ;
      labels.append(wsName) ;
    }
    UIFormInputSetWithAction workspaceField = getChildById(UIRepositoryForm.FIELD_WSINPUTSET) ;
    workspaceField.setInfoField(UIRepositoryForm.FIELD_WORKSPACE, labels.toString()) ;
  }

  protected boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;    
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }

  protected void saveRepo(RepositoryEntry repositoryEntry) throws Exception {    
    InitialContextInitializer ic = (InitialContextInitializer)getApplicationComponent(ExoContainer.class).
    getComponentInstanceOfType(InitialContextInitializer.class) ;
    RegistryService registryService = getApplicationComponent(RegistryService.class) ;
    if(ic != null) ic.recall() ;
    RepositoryService rService = (RepositoryService)getApplicationComponent(ExoContainer.class).
    getComponentInstanceOfType(RepositoryService.class);
    if(isAddnew_){
      try { 
        rService.createRepository(repositoryEntry) ;
        for(WorkspaceEntry ws : getWorkspaceMap().values()) {
          if(ws.getName().equals(repositoryEntry.getSystemWorkspaceName())) {
            registryService.addRegistryLocation(repositoryEntry.getName(), ws.getName()) ;
          }
        }
        for(WorkspaceEntry ws : getWorkspaceMap().values()) {
          if(!rService.getRepository(repositoryEntry.getName()).isWorkspaceInitialized(ws.getName())) {
            rService.getRepository(repositoryEntry.getName()).configWorkspace(ws);
            rService.getRepository(repositoryEntry.getName()).createWorkspace(ws.getName()) ;
          }
        }
      } catch (Exception e) {
        e.printStackTrace() ;
        return ;
      }
      initServices(repositoryEntry.getName()) ;        
      if(rService.getConfig().isRetainable()) {
        rService.getConfig().retain() ;
      }
    } 
  }

  private void initServices(String repository) throws Exception{
    try {
      getApplicationComponent(RegistryService.class).start() ;
      getApplicationComponent(NodeHierarchyCreator.class).init(repository) ;
      getApplicationComponent(CategoriesService.class).init(repository) ;
      getApplicationComponent(ManageDriveService.class).init(repository) ;
      getApplicationComponent(FolksonomyService.class).init(repository) ;
      getApplicationComponent(MetadataService.class).init(repository) ;
      getApplicationComponent(QueryService.class).init(repository) ;
      getApplicationComponent(RelationsService.class).init(repository) ;
      getApplicationComponent(ScriptService.class).initRepo(repository) ;
      getApplicationComponent(TemplateService.class).init(repository) ;
      getApplicationComponent(ManageViewService.class).init(repository) ;
      getApplicationComponent(ActionServiceContainer.class).init(repository) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  protected void ShowHidden() {
//    getUIStringInput(FIELD_REPCHANNEL).setRendered(!getUIStringInput(FIELD_REPCHANNEL).isRendered()) ;
//    getUIStringInput(FIELD_REPMODE).setRendered(!getUIStringInput(FIELD_REPMODE).isRendered()) ;
//    getUIFormCheckBoxInput(FIELD_REPTESTMODE).setRendered(!getUIFormCheckBoxInput(FIELD_REPTESTMODE).isRendered()) ;
//    getUIStringInput(FIELD_BSEPATH).setRendered(!getUIStringInput(FIELD_BSEPATH).isRendered()) ;
//    getUIStringInput(FIELD_BSEMAXBUFFER).setRendered(!getUIStringInput(FIELD_BSEMAXBUFFER).isRendered()) ;
  }
  
  public void activate() throws Exception {}

  public void deActivate() throws Exception { repoName_ = null ;}


  public static class SelectActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryFormContainer uiRepoContainer = event.getSource().getAncestorOfType(UIRepositoryFormContainer.class) ;
      UIRepositoryValueSelect uiSelect = uiRepoContainer.getChild(UIPopupAction.class).activate(UIRepositoryValueSelect.class, 500) ;
      uiSelect.isSetAuthentication_  = true ;
      List<ClassData> datas = new ArrayList<ClassData>() ;
      datas.add(new ClassData(UIRepositoryForm.KEY_AUTHENTICATIONPOLICY)) ;
      uiSelect.updateGrid(datas) ;
    }
  }
  public static class ShowHiddenActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm =  event.getSource() ;
      uiForm.ShowHidden() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  public static class SaveActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class);
      UIPopupAction uiWizardPopup = uiControl.getChild(UIPopupAction.class) ;
      uiWizardPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWizardPopup) ;
      RepositoryEntry re = new RepositoryEntry() ;
      String repoName = uiForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
      RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      for(RepositoryEntry repo : rService.getConfig().getRepositoryConfigurations()) { 
        if(repo.getName().equals(repoName) && uiForm.isAddnew_) {
          Object[] args = new Object[]{repo.getName()}  ;    
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
      }
      if(!Utils.isNameValid(repoName, Utils.SPECIALCHARACTER)) {        
        Object[] args = new Object[]{repoName}  ;    
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-not-alow", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      if (uiForm.getWorkspaceMap().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-isrequire", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      if (uiForm.defaulWorkspace_ == null) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-setdefault", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      String acess = uiForm.getUIStringInput(UIRepositoryForm.FIELD_ACCESSCONTROL).getValue() ;
      UIFormInputSetWithAction autField = uiForm.getChildById(UIRepositoryForm.FIELD_AUTHINPUTSET) ;
      String authen = autField.getUIStringInput(UIRepositoryForm.FIELD_AUTHENTICATION).getValue() ;
      if(Utils.isNameEmpty(authen)) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.authen-isrequire", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      String security = uiForm.getUIStringInput(UIRepositoryForm.FIELD_SCURITY).getValue() ;
      String sessionTimeOut = uiForm.getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).getValue() ;
      if(Utils.isNameEmpty(sessionTimeOut)) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.sessionTime-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      try {
        Long.parseLong(sessionTimeOut) ;
      } catch (NumberFormatException nfe) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.sessionTime-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      re.setName(repoName) ;
      re.setAccessControl(acess) ;
      re.setAuthenticationPolicy(authen) ;
      re.setSecurityDomain(security) ;
      re.setSessionTimeOut(Long.parseLong(sessionTimeOut)) ;
//      Boolean repEnable = uiForm.getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPENABLE).isChecked() ;
//      if(repEnable) {
//        String chanel = uiForm.getUIStringInput(UIRepositoryForm.FIELD_REPCHANNEL).getValue() ;     
//        String mode = uiForm.getUIStringInput(UIRepositoryForm.FIELD_REPMODE).getValue() ;     
//        Boolean testMode = uiForm.getUIFormCheckBoxInput(UIRepositoryForm.FIELD_REPTESTMODE).isChecked() ;
//        String path = uiForm.getUIStringInput(UIRepositoryForm.FIELD_BSEPATH).getValue() ;     
//        String buffer = uiForm.getUIStringInput(UIRepositoryForm.FIELD_BSEMAXBUFFER).getValue() ;   
//        if(Utils.isNameEmpty(chanel)){
//          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-bseChanel", null)) ;
//          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
//          return ;
//        }
//        if(Utils.isNameEmpty(mode)){
//          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-bseMode", null)) ;
//          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
//          return ;
//        }
//        if(Utils.isNameEmpty(path)){
//          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-bsePath", null)) ;
//          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
//          return ;
//        }
//        if(Utils.isNameEmpty(buffer)){
//          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.workspace-bseBuffer", null)) ;
//          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
//          return ;
//        }
//        ReplicationEntry repl = new ReplicationEntry() ;
//        repl.setChannelConfig(chanel) ;
//        repl.setEnabled(repEnable) ;
//        repl.setMode(mode) ;
//        repl.setTestMode(testMode) ;
//        re.setReplication(repl) ;
//        BinarySwapEntry bse = new BinarySwapEntry() ;
//        bse.setDirectoryPath(path) ;
//        bse.setMaxBufferSize(buffer) ;
//        re.setBinaryTemp(bse) ;        
//      }
      re.setSystemWorkspaceName(uiForm.defaulWorkspace_) ;
      re.setDefaultWorkspaceName(uiForm.defaulWorkspace_) ;
      re.addWorkspace(uiForm.getWorkspace(uiForm.defaulWorkspace_)) ;
      uiForm.saveRepo(re) ;
      UIRepositoryControl uiRepoControl = uiForm.getAncestorOfType(UIECMAdminPortlet.class).
      findFirstComponentOfType(UIRepositoryControl.class) ;
      uiRepoControl.reloadValue(true, rService) ;
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
      RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
      if(uiForm.isAddnew_) uiForm.refresh(rService.getDefaultRepository().getConfiguration()) ;
      else uiForm.refresh(rService.getRepository(uiForm.repoName_).getConfiguration());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public static class AddWorkspaceActionListener extends EventListener<UIRepositoryForm>{
    public void execute(Event<UIRepositoryForm> event) throws Exception{
      UIRepositoryForm uiForm = event.getSource() ;  
      String repoName = uiForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
      String sessionTime = uiForm.getUIStringInput(UIRepositoryForm.FIELD_SESSIONTIME).getValue() ;
      RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      for(RepositoryEntry repo : rService.getConfig().getRepositoryConfigurations()) { 
        if(repo.getName().equals(repoName) && uiForm.isAddnew_) {
          Object[] args = new Object[]{repo.getName()}  ;    
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
      }
      if(!Utils.isNameValid(repoName, Utils.SPECIALCHARACTER)) {      
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.repoName-not-alow", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      if(Utils.isNameEmpty(sessionTime)) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.sessionTime-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      try {
        Long.parseLong(sessionTime.trim()) ;
      } catch (NumberFormatException nfe) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.sessionTime-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }
      UIRepositoryFormContainer uiControl = uiForm.getAncestorOfType(UIRepositoryFormContainer.class);
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIECMAdminPortlet.class).findFirstComponentOfType(UIPopupAction.class) ;
      UIPopupAction uiWorkspaceAction = uiControl.getChild(UIPopupAction.class) ;
      UIWorkspaceWizardContainer uiWsContainer = uiWorkspaceAction.activate(UIWorkspaceWizardContainer.class, 700) ; 
      WorkspaceEntry wsdf = null ;
      RepositoryEntry  repoEntry = rService.getDefaultRepository().getConfiguration() ;
      for(WorkspaceEntry ws : repoEntry.getWorkspaceEntries()) {
        if(ws.getName().equals(repoEntry.getDefaultWorkspaceName())) {
          wsdf = ws ;
          break ;
        }
      }
      uiWsContainer.initWizard(uiForm.isAddnew_, true, wsdf) ;      
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
      UIWorkspaceWizardContainer uiWsContainer = uiPopupAction.activate(UIWorkspaceWizardContainer.class, 600) ; 
      uiWsContainer.initWizard(uiForm.isAddnew_, false, uiForm.getWorkspace(workspaceName)) ;
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
      if(!uiForm.isAddnew_) {
        RepositoryService rService = uiForm.getApplicationComponent(RepositoryService.class) ;
        ManageableRepository manaRepo = rService.getRepository(uiForm.repoName_) ;
        if(manaRepo.canRemoveWorkspace(workspaceName)) {
          manaRepo.removeWorkspace(workspaceName) ;
          InitialContextInitializer ic = (InitialContextInitializer)uiForm.getApplicationComponent(ExoContainer.class).
          getComponentInstanceOfType(InitialContextInitializer.class) ;
          if(ic != null) ic.recall() ;
          if(rService.getConfig().isRetainable()) {
            rService.getConfig().retain() ;
          }
          uiForm.workspaceMap_.clear() ;
          for(WorkspaceEntry ws : manaRepo.getConfiguration().getWorkspaceEntries()) {
            uiForm.workspaceMap_.put(ws.getName(), ws) ;
          } 
          uiForm.refreshWorkspaceList() ;
        }else {
          Object[] args = {workspaceName}  ;    
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIRepositoryForm.msg.cannot-delete-workspace", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
      } else {
        uiForm.workspaceMap_.remove(workspaceName) ;
        if(uiForm.isDefaultWorkspace(workspaceName)) uiForm.defaulWorkspace_ = null ;
        uiForm.refreshWorkspaceList() ;      
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIRepositoryFormContainer.class)) ;  
    }
  }
  public void setAuthentication(String value) {
    UIFormInputSetWithAction autField =  getChildById(FIELD_AUTHINPUTSET) ;
    autField.getUIStringInput(FIELD_AUTHENTICATION).setValue(value);
  }
}
