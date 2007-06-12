/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.CacheEntry;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 11, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/admin/UIWorkspaceWizard.gtmpl",    
    events = {
      @EventConfig(listeners = UIWorkspaceWizard.FinishActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.NextActionListener.class ),
      @EventConfig(listeners = UIWorkspaceWizard.BackActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep1ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep2ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep3ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.SetDefaultActionListener.class,  phase=Phase.DECODE),
      @EventConfig(listeners = UIWorkspaceWizard.CancelActionListener.class, phase=Phase.DECODE)
    }

)
public class UIWorkspaceWizard extends UIFormTabPane implements UIPopupComponent {
  private int wizardMaxStep_ = 3 ;
  private int selectedStep_ = 1 ;
  private int currentStep_ = 0 ;
  public boolean isNewWizard_ = true ;
  public boolean isNewRepo_ = true ;

  private Map<Integer, String> chidrenMap_ = new HashMap<Integer, String>() ; 

  private Map<Integer, String[]> actionMap_ = new HashMap<Integer, String[]>() ;

  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_NODETYPE = "autoInitializedRootNt" ;
  final static public String FIELD_TIMEOUT = "setLockTimeOut" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;

  final static public String FIELD_SOURCENAME = "sourceName" ;  
  final static public String FIELD_DBTYPE = "dbType" ;
  final static public String FIELD_ISMULTI = "isMulti" ;
  final static public String FIELD_ISUPDATESTORE = "isUpdateStore" ;
  final static public String FIELD_MAXBUFFER = "maxBuffer" ;
  final static public String FIELD_SWAPPATH = "swapPath" ;
  final static public String FIELD_STOREPATH = "storePath" ;  
  final static public String FIELD_FILTER = "filterType" ;  

  final static public String FIELD_INDEXPATH= "indexPath" ;
  final static public String FIELD_ISCACHE = "isCache" ;
  final static public String FIELD_MAXSIZE = "maxSize" ;
  final static public String FIELD_LIVETIME = "liveTime" ;

  final static public String fIELD_STEP1 = "step1" ;
  final static public String fIELD_STEP2 = "step2" ;
  final static public String fIELD_STEP3 = "step3" ;

  public UIWorkspaceWizard() throws Exception {
    super("UIWorkspaceWizard");

    chidrenMap_.put(1, fIELD_STEP1) ;
    chidrenMap_.put(2, fIELD_STEP2) ;
    chidrenMap_.put(3, fIELD_STEP3) ;

    actionMap_.put(1, new String[]{"Next", "Cancel"}) ;
    actionMap_.put(2, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(3, new String[]{"Back", "Finish", "Cancel"}) ;

    UIFormInputSet step1 = new UIFormInputSet(fIELD_STEP1) ;
    step1.addChild(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ;
    step1.addChild(new UIFormSelectBox(FIELD_NODETYPE, FIELD_NODETYPE, getNodeType())) ;
    step1.addChild(new UIFormStringInput(FIELD_TIMEOUT, FIELD_TIMEOUT, null).addValidator(EmptyFieldValidator.class).
        addValidator(NumberFormatValidator.class)) ;
    UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(FIELD_ISDEFAULT, FIELD_ISDEFAULT, null) ;
    checkbox.setOnChange("SetDefault") ;
    step1.addChild(checkbox) ;
    UIFormInputSet step2 = new UIFormInputSet(fIELD_STEP2) ;
    step2.addChild(new UIFormStringInput(FIELD_SOURCENAME, FIELD_SOURCENAME, null)) ;
    step2.addChild(new UIFormSelectBox(FIELD_DBTYPE, FIELD_DBTYPE, getDbType())) ;
    step2.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISMULTI, FIELD_ISMULTI, null)) ;
    step2.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISUPDATESTORE, FIELD_ISUPDATESTORE, null)) ;
    step2.addChild(new UIFormStringInput(FIELD_MAXBUFFER, FIELD_MAXBUFFER, null).addValidator(NumberFormatValidator.class)) ;
    step2.addChild(new UIFormStringInput(FIELD_SWAPPATH, FIELD_SWAPPATH, null)) ;
    step2.addChild(new UIFormStringInput(FIELD_STOREPATH, FIELD_STOREPATH, null)) ;
    step2.addChild(new UIFormSelectBox(FIELD_FILTER, FIELD_FILTER, getFilterType())) ;

    UIFormInputSet step3 = new UIFormInputSet(fIELD_STEP3) ;
    step3.addChild(new UIFormStringInput(FIELD_INDEXPATH, FIELD_INDEXPATH, null)) ;
    step3.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISCACHE, FIELD_ISCACHE, null)) ;
    step3.addChild(new UIFormStringInput(FIELD_MAXSIZE, FIELD_MAXSIZE, null).addValidator(NumberFormatValidator.class)) ;
    step3.addChild(new UIFormStringInput(FIELD_LIVETIME, FIELD_LIVETIME, null).addValidator(NumberFormatValidator.class)) ;

    addUIComponentInput(step1) ;
    addUIComponentInput(step2) ;
    addUIComponentInput(step3) ;

    setRenderedChild(getCurrentChild()) ;
  }

  public List<SelectItemOption<String>>  getNodeType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("nt:unstructured", "nt:unstructured")) ;
    options.add(new SelectItemOption<String>("nt:folder", "nt:folder")) ;
    return options ;
  }

  public List<SelectItemOption<String>> getDbType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("generic", "generic")) ; 
    options.add(new SelectItemOption<String>("mysql", "mysql")) ; 
    return options ;
  }
  public List<SelectItemOption<String>> getFilterType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("Binary", "Binary")) ;
    return options ;
  }

  public void setCurrentSep(int step){ currentStep_ = step ;}

  public int getCurrentStep() { return currentStep_; }
  public void setSelectedStep(int step){ selectedStep_ = step ;}
  public int getSelectedStep() { return selectedStep_; }

  public int getMaxStep(){return wizardMaxStep_ ;}

  public String[] getActions(){return actionMap_.get(selectedStep_) ;}

  public String getCurrentChild() {return chidrenMap_.get(selectedStep_) ;}

  public String[] getCurrentAction() {return actionMap_.get(selectedStep_) ;}

  protected void refresh(WorkspaceEntry workSpace) throws Exception{
    reset() ;
    UIFormInputSet uiWSFormStep1 = getChildById(fIELD_STEP1) ;
    UIFormInputSet uiWSFormStep2 = getChildById(fIELD_STEP2) ;
    UIFormInputSet uiWSFormStep3 = getChildById(fIELD_STEP3) ;
    uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
    UIRepositoryForm uiRepoForm = getAncestorOfType(UIECMAdminPortlet.class).findFirstComponentOfType(UIRepositoryForm.class) ;
    isNewRepo_ = uiRepoForm.isAddnew_ ;
    String repoName = uiRepoForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
    if(workSpace != null) {
      ContainerEntry container = workSpace.getContainer() ;
      if(isNewWizard_) { 
        uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).setEditable(true) ;
        String swapPath = container.getParameterValue("swap-directory") ;
        ArrayList<ValueStorageEntry> valueStore = container.getValueStorages() ;
        String storePath = valueStore.get(0).getParameterValue("path") ;
        StringBuilder sb1 = new StringBuilder() ;
        StringBuilder sb2 = new StringBuilder() ;
        if(isNewRepo_) {
          uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(true) ;
          sb1.append(swapPath.substring(0, swapPath.lastIndexOf("/")+1)).append(repoName).append("/") ;
          sb2.append(storePath.substring(0, storePath.lastIndexOf("/")+1)).append(repoName).append("/") ;
        } else {
          uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(false) ;
          sb1.append(swapPath.substring(0, swapPath.lastIndexOf("/")+1));
          sb2.append(storePath.substring(0, storePath.lastIndexOf("/")+1));
        }
        uiWSFormStep2.getUIStringInput(FIELD_SWAPPATH).setValue(sb1.toString()) ;
        uiWSFormStep2.getUIStringInput(FIELD_STOREPATH).setValue(sb2.toString()) ;
        ArrayList<ValueStorageFilterEntry> valueFilters = valueStore.get(0).getFilters() ;     
        uiWSFormStep2.getUIFormSelectBox(FIELD_FILTER).setValue(valueFilters.get(0).getPropertyType()) ;
      } else {
        uiWSFormStep1.getUIStringInput(FIELD_NAME).setValue(workSpace.getName()) ;
        uiWSFormStep1.getUIStringInput(FIELD_NAME).setEditable(false) ;
        uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(uiRepoForm.isDefaultWorkspace(workSpace.getName())) ;
        if(isNewRepo_) uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(true) ;
        else uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(false) ;
        String swapPath = container.getParameterValue("swap-directory") ;
        uiWSFormStep2.getUIStringInput(FIELD_SWAPPATH).setValue(swapPath) ;
        ArrayList<ValueStorageEntry> valueStore = container.getValueStorages() ;
        String storePath = valueStore.get(0).getParameterValue("path") ;
        uiWSFormStep2.getUIStringInput(FIELD_STOREPATH).setValue(storePath) ;
        ArrayList<ValueStorageFilterEntry> valueFilters = valueStore.get(0).getFilters() ;     
        uiWSFormStep2.getUIFormSelectBox(FIELD_FILTER).setValue(valueFilters.get(0).getPropertyType()) ;
      }
      uiWSFormStep1.getUIFormSelectBox(FIELD_NODETYPE).setValue(workSpace.getAutoInitializedRootNt()) ;      
      uiWSFormStep1.getUIStringInput(FIELD_TIMEOUT).setValue(String.valueOf(workSpace.getLockTimeOut())) ;
      uiWSFormStep2.getUIStringInput(FIELD_SOURCENAME).setValue(container.getParameterValue("sourceName")) ;
      uiWSFormStep2.getUIFormSelectBox(FIELD_DBTYPE).setValue(container.getParameterValue("db-type")) ;
      uiWSFormStep2.getUIFormCheckBoxInput(FIELD_ISMULTI).setChecked(Boolean.parseBoolean(container.getParameterValue("multi-db"))) ;
      uiWSFormStep2.getUIFormCheckBoxInput(FIELD_ISUPDATESTORE).setChecked(Boolean.parseBoolean(container.getParameterValue("update-storage"))) ;
      uiWSFormStep2.getUIStringInput(FIELD_MAXBUFFER).setValue(container.getParameterValue("max-buffer-size")) ;
      QueryHandlerEntry queryHandler = workSpace.getQueryHandler() ;
      uiWSFormStep3.getUIStringInput(FIELD_INDEXPATH).setValue(queryHandler.getParameterValue("indexDir")) ;
      CacheEntry cache = workSpace.getCache() ;
      uiWSFormStep3.getUIFormCheckBoxInput(FIELD_ISCACHE).setChecked(cache.isEnabled()) ;
      uiWSFormStep3.getUIStringInput(FIELD_MAXSIZE).setValue(cache.getParameterValue("maxSize")) ;
      uiWSFormStep3.getUIStringInput(FIELD_LIVETIME).setValue(cache.getParameterValue("liveTime")) ;
    }
  }
  public String url(String name) throws Exception {
    UIComponent renderedChild = getChild(currentStep_);
    if(!(renderedChild instanceof UIForm)) return super.event(name);

    org.exoplatform.webui.config.Event event = config.getUIComponentEventConfig(name) ;
    if(event == null) return "??config??" ;

    UIForm uiForm = (UIForm) renderedChild;
    return uiForm.event(name);
  }

  public int getNumberSteps() {return wizardMaxStep_ ;}

  public void viewStep(int step) {   
    selectedStep_ = step ;
    currentStep_ = step - 1 ;    
    List<UIComponent> children = getChildren(); 
    for(int i=0; i<children.size(); i++){
      if(i == getCurrentStep()) {
        children.get(i).setRendered(true);
      } else {
        children.get(i).setRendered(false);
      }
    }
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}

  public static class ViewStep1ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.viewStep(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ; 
    }
  }

  public static class ViewStep2ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;      
      uiFormWizard.viewStep(2) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ;
    }

  }

  public static class ViewStep3ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP2) ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;
      if(uiWSFormStep2.isRendered()) {
        if((sourceName == null) || (sourceName.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.sourceName-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        if((maxBuffer == null) ||(maxBuffer.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if(Integer.parseInt(maxBuffer) <= 0) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if((swapPath == null) || (swapPath.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.swapPath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } 
        if((storePath == null) || (storePath.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.storePath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
      }
      uiFormWizard.viewStep(3) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ;
    }

  }
  public static class SetDefaultActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiWizard = event.getSource() ;
      UIRepositoryFormContainer formContainer = uiWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
      UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
      UIFormInputSet uiWSFormStep1 = uiWizard.getChildById(UIWorkspaceWizard.fIELD_STEP1) ;
      boolean isDefault = uiWSFormStep1.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISDEFAULT).isChecked() ;
      if(isDefault){
        uiRepoForm.defaulWorkspace_ = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      } else {
        uiRepoForm.defaulWorkspace_ = null ;
      }
    }
  }
  public static class FinishActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      long lockTimeOutValue = 0 ;
      long bufferValue = 0 ;
      long maxSizeValue = 0 ;
      long liveTimeValue = 0 ;

      UIFormInputSet uiWSFormStep1 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP1) ;
      String name = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      String initNodeType = uiWSFormStep1.getUIFormSelectBox(UIWorkspaceWizard.FIELD_NODETYPE).getValue() ;
      String lockTimeOut = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_TIMEOUT).getValue() ;

      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP2) ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String dbType =  uiWSFormStep2.getUIFormSelectBox(UIWorkspaceWizard.FIELD_DBTYPE).getValue() ;
      boolean isMulti = uiWSFormStep2.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISMULTI).isChecked() ;
      boolean isUpdateStore = uiWSFormStep2.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISUPDATESTORE).isChecked() ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String filterType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_FILTER).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;

      UIFormInputSet uiWSFormStep3 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP3) ;
      String indexPath = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).getValue() ;
      boolean isCache = uiWSFormStep3.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISCACHE).isChecked() ;
      String maxSize = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_MAXSIZE).getValue() ;
      String liveTime = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_LIVETIME).getValue() ;
      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      if((indexPath == null) || (indexPath.trim().length() == 0)) {
        uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.indexPath-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(isCache){
        if((maxSize == null) ||(maxSize.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.maxSize-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if(Integer.parseInt(maxBuffer) <= 0) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if((liveTime == null) ||(liveTime.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.liveTime-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        maxSizeValue = Integer.parseInt(maxSize) ;
        liveTimeValue = Integer.parseInt(liveTime) ;
      }
      lockTimeOutValue = Integer.parseInt(lockTimeOut) ;
      bufferValue = Integer.parseInt(maxBuffer) ;
      UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
      UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
      if(uiFormWizard.isNewWizard_) {
        if(uiRepoForm.isExistWorkspace(name)){
          Object[] args = new Object[]{name}  ;        
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceForm.msg.wsname-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }          
      } 

      WorkspaceEntry workspaceEntry = new WorkspaceEntry(name, initNodeType);
      workspaceEntry.setLockTimeOut(lockTimeOutValue) ;
      workspaceEntry.setContainer(newContainerEntry(sourceName, dbType, isMulti, isUpdateStore,
          bufferValue, swapPath, filterType, storePath));
      workspaceEntry.setCache(newCacheEntry(isCache, maxSizeValue, liveTimeValue)) ;
      workspaceEntry.setQueryHandler(newQueryHandlerEntry(indexPath)) ;
      if(uiFormWizard.isNewWizard_ && !uiFormWizard.isNewRepo_) {
        InitialContextInitializer ic = (InitialContextInitializer)uiFormWizard.getApplicationComponent(ExoContainer.class).
        getComponentInstanceOfType(InitialContextInitializer.class) ;
        if(ic != null) ic.recall() ;
        RepositoryService rService = (RepositoryService)uiFormWizard.getApplicationComponent(ExoContainer.class).
        getComponentInstanceOfType(RepositoryService.class);
        ManageableRepository manageRepository = rService.getRepository(uiRepoForm.repoName_);
        try {
          manageRepository.configWorkspace(workspaceEntry) ;
          manageRepository.createWorkspace(workspaceEntry.getName()) ;
        }
        catch (Exception e) {
          e.printStackTrace() ;
          return;
        }
      }
      uiRepoForm.getWorkspaceMap().put(name, workspaceEntry) ;
      uiRepoForm.refreshWorkspaceList() ;      
      UIPopupAction uiPopupAction = uiFormWizard.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoForm) ;
    }

    @SuppressWarnings("unchecked")
    private ContainerEntry newContainerEntry(String sourceName, String dbType, boolean  isMulti,
        boolean isUpdateStore, long bufferValue, String swapPath, String filterType, String storePath) {
      List containerParams = new ArrayList();
      containerParams.add(new SimpleParameterEntry("sourceName", sourceName)) ;
      containerParams.add(new SimpleParameterEntry("db-type", dbType)) ;
      containerParams.add(new SimpleParameterEntry("multi-db", String.valueOf(isMulti))) ;
      containerParams.add(new SimpleParameterEntry("update-storage", String.valueOf(isUpdateStore))) ;
      containerParams.add(new SimpleParameterEntry("max-buffer-size", String.valueOf(bufferValue))) ;
      containerParams.add(new SimpleParameterEntry("swap-directory", swapPath)) ;
      ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer", (ArrayList) containerParams) ;      

      ArrayList<ValueStorageFilterEntry> vsparams = new ArrayList<ValueStorageFilterEntry>();
      ValueStorageFilterEntry filterEntry = new ValueStorageFilterEntry();
      filterEntry.setPropertyType(filterType);
      vsparams.add(filterEntry);

      ValueStorageEntry valueStorageEntry = new ValueStorageEntry("org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage",
          vsparams);
      ArrayList<SimpleParameterEntry> spe = new ArrayList<SimpleParameterEntry>();
      spe.add(new SimpleParameterEntry("path", storePath));

      valueStorageEntry.setParameters(spe);
      valueStorageEntry.setFilters(vsparams);
      containerEntry.setParameters(containerParams);
      ArrayList list = new ArrayList(1);
      list.add(valueStorageEntry);
      containerEntry.setValueStorages(list);
      return containerEntry ;
    }
    private CacheEntry newCacheEntry(boolean isCache, long maxSizeValue, long liveTimeValue) {
      CacheEntry cache = new CacheEntry() ;
      cache.setEnabled(isCache) ;      
      ArrayList<SimpleParameterEntry> cacheParams = new ArrayList<SimpleParameterEntry>() ;
      cacheParams.add(new SimpleParameterEntry("maxSize", String.valueOf(maxSizeValue))) ;
      cacheParams.add(new SimpleParameterEntry("liveTime", String.valueOf(liveTimeValue))) ;
      cache.setParameters(cacheParams) ;
      return cache ;
    }
    private QueryHandlerEntry newQueryHandlerEntry(String indexPath) {
      ArrayList<SimpleParameterEntry> queryParams = new ArrayList<SimpleParameterEntry>() ;
      queryParams.add(new SimpleParameterEntry("indexDir", indexPath)) ;
      QueryHandlerEntry queryHandler = new QueryHandlerEntry("org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex", queryParams) ;
      queryHandler.setParameters(queryParams) ;
      return queryHandler ;
    }
  }

  public static class NextActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP2) ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;

      UIFormInputSet uiWSFormStep3 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP3) ;
      String indexPath = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).getValue() ;

      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;

      if(uiWSFormStep2.isRendered()) {
        if((sourceName == null) || (sourceName.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.sourceName-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        if((maxBuffer == null) ||(maxBuffer.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if(Integer.parseInt(maxBuffer) <= 0) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if((swapPath == null) || (swapPath.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.swapPath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } 
        if((storePath == null) || (storePath.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.storePath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
      }
      if(uiWSFormStep3.isRendered()) {
        if((indexPath == null) || (indexPath.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.indexPath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }

      int step = uiFormWizard.getCurrentStep() ;
      List<UIComponent> children = uiFormWizard.getChildren() ;
      if(step < uiFormWizard.getMaxStep()) {
        step++ ;
        uiFormWizard.setCurrentSep(step) ;
        for(int i = 0 ; i< children.size(); i++) {
          if(i == step) {
            children.get(i).setRendered(true);
            uiFormWizard.setSelectedStep(step+1) ;
          } else {
            children.get(i).setRendered(false);
          } 
        }
      }     
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ; 
    }
  }

  public static class BackActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      int step = uiFormWizard.getCurrentStep() ;
      List<UIComponent> children = uiFormWizard.getChildren() ;
      if(step > 0) {
        step-- ;
        uiFormWizard.setCurrentSep(step) ;
        for(int i = 0 ; i< children.size(); i++) {
          if(i == step) {
            children.get(i).setRendered(true);
            uiFormWizard.setSelectedStep(step+1) ;
          } else {
            children.get(i).setRendered(false);
          } 
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public static class CancelActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.refresh(null) ;
      UIPopupAction uiPopupAction = uiFormWizard.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
