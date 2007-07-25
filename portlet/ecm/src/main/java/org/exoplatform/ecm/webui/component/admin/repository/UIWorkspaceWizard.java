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
import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryValueSelect.ClassData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.CacheEntry;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.LockManagerEntry;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer;
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
import org.exoplatform.webui.form.UIFormInputInfo;
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
      @EventConfig(listeners = UIWorkspaceWizard.AddPermissionActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.SelectContainerActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.SelectStoreActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.SelectQueryHandlerActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.FinishActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.NextActionListener.class ),
      @EventConfig(listeners = UIWorkspaceWizard.BackActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep1ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep2ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.ViewStep3ActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIWorkspaceWizard.EditPermissionActionListener.class),
      @EventConfig(listeners = UIWorkspaceWizard.RemovePermissionActionListener.class)
    }

)
public class UIWorkspaceWizard extends UIFormTabPane implements UISelector {
  private int wizardMaxStep_ = 3 ;
  private int selectedStep_ = 1 ;
  private int currentStep_ = 0 ;
  private String selectedWsName_ = null ;
  public boolean isNewWizard_ = true ;
  public boolean isNewRepo_ = true ;
  public boolean isCheckValid_ = true ;
  public boolean hasCheckDefault_ = false ;
  public Map<String, String> permissions_ = new HashMap<String, String>() ;

  private Map<Integer, String> chidrenMap_ = new HashMap<Integer, String>() ; 

  private Map<Integer, String[]> actionMap_ = new HashMap<Integer, String[]>() ;

  final static public String POPUPID = "UIPopupWindowInWizard" ;
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_NODETYPE = "autoInitializedRootNt" ;
  final static public String FIELD_PERMISSION = "permission" ;
  final static public String FIELD_TIMEOUT = "setLockTimeOut" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;

  final static public String FIELD_CONTAINER = "container" ;

  final static public String FIELD_SOURCENAME = "sourceName" ;  
  final static public String FIELD_DBTYPE = "dbType" ;

  final static public String FIELD_ISMULTI = "isMulti" ;
  final static public String FIELD_STORETYPE = "storeType" ;
  final static public String FIELD_MAXBUFFER = "maxBuffer" ;
  final static public String FIELD_SWAPPATH = "swapPath" ;
  final static public String FIELD_STOREPATH = "storePath" ;  
  final static public String FIELD_FILTER = "filterType" ;  

  final static public String FIELD_QUERYHANDLER = "queryHandler" ;
  final static public String FIELD_INDEXPATH = "indexPath" ;
  final static public String FIELD_ISCACHE = "isCache" ;
  final static public String FIELD_MAXSIZE = "maxSize" ;
  final static public String FIELD_LIVETIME = "liveTime" ;

  final static public String FIELD_STEP1 = "step1" ;
  final static public String FIELD_STEP2 = "step2" ;
  final static public String FIELD_STEP3 = "step3" ;

  final static public String KEY_CONTAINERTYPE = "org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer" ;
  final static public String KEY_STORETYPE = "org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage" ;
  final static public String KEY_QUERYHANDLER = "org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex" ;


  public UIWorkspaceWizard() throws Exception {
    super("UIWorkspaceWizard");

    chidrenMap_.put(1, FIELD_STEP1) ;
    chidrenMap_.put(2, FIELD_STEP2) ;
    chidrenMap_.put(3, FIELD_STEP3) ;

    actionMap_.put(1, new String[]{"Next", "Cancel"}) ;
    actionMap_.put(2, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(3, new String[]{"Back", "Finish", "Cancel"}) ;

    /*UIWizardStep1 wsStep1 = new UIWizardStep1(FIELD_STEP1) ;
    addChild(wsStep1) ;*/
    UIFormInputSetWithAction step1 = new UIFormInputSetWithAction(FIELD_STEP1) ;
    step1.addChild(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(ECMNameValidator.class)) ;
    step1.addChild(new UIFormSelectBox(FIELD_NODETYPE, FIELD_NODETYPE, getNodeType())) ;
    UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(FIELD_ISDEFAULT, FIELD_ISDEFAULT, null) ;
    step1.addChild(checkbox) ;
    step1.addUIFormInput(new UIFormInputInfo(FIELD_PERMISSION, FIELD_PERMISSION, null)) ;
    step1.setActionInfo(FIELD_PERMISSION, new String[]{"AddPermission"}) ;
    step1.setFieldActions(FIELD_PERMISSION, new String[]{"AddPermission"}) ;
    step1.showActionInfo(true) ;
    step1.addChild(new UIFormStringInput(FIELD_TIMEOUT, FIELD_TIMEOUT, null).addValidator(EmptyFieldValidator.class).
        addValidator(NumberFormatValidator.class)) ; 
    UIFormInputSetWithAction step2 = new UIFormInputSetWithAction(FIELD_STEP2) ;
    step2.addChild(new UIFormStringInput(FIELD_CONTAINER, FIELD_CONTAINER, null)) ;
    step2.setActionInfo(FIELD_CONTAINER, new String[]{"SelectContainer"}) ;
    step2.addChild(new UIFormStringInput(FIELD_SOURCENAME, FIELD_SOURCENAME, null)) ;
    step2.addChild(new UIFormSelectBox(FIELD_DBTYPE, FIELD_DBTYPE, getDbType())) ;
    step2.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISMULTI, FIELD_ISMULTI, null)) ;
    step2.addChild(new UIFormStringInput(FIELD_STORETYPE, FIELD_STORETYPE, null)) ;
    step2.setActionInfo(FIELD_STORETYPE, new String[]{"SelectStore"}) ;
    step2.addChild(new UIFormSelectBox(FIELD_FILTER, FIELD_FILTER, getFilterType())) ;
    step2.addChild(new UIFormStringInput(FIELD_MAXBUFFER, FIELD_MAXBUFFER, null).addValidator(NumberFormatValidator.class)) ;
    step2.addChild(new UIFormStringInput(FIELD_SWAPPATH, FIELD_SWAPPATH, null)) ;
    step2.addChild(new UIFormStringInput(FIELD_STOREPATH, FIELD_STOREPATH, null)) ;

    UIFormInputSetWithAction step3 = new UIFormInputSetWithAction(FIELD_STEP3) ;
    step3.addChild(new UIFormStringInput(FIELD_QUERYHANDLER, FIELD_QUERYHANDLER, null)) ;
    step3.setActionInfo(FIELD_QUERYHANDLER, new String[]{"SelectQueryHandler"}) ;

    step3.addChild(new UIFormStringInput(FIELD_INDEXPATH, FIELD_INDEXPATH, null)) ;
    step3.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISCACHE, FIELD_ISCACHE, null)) ;
    step3.addChild(new UIFormStringInput(FIELD_MAXSIZE, FIELD_MAXSIZE, null).addValidator(NumberFormatValidator.class)) ;
    step3.addChild(new UIFormStringInput(FIELD_LIVETIME, FIELD_LIVETIME, null).addValidator(NumberFormatValidator.class)) ;

    addUIComponentInput(step1) ;
    addUIComponentInput(step2) ;
    addUIComponentInput(step3) ;
    setRenderedChild(getCurrentChild()) ;
  }
  public void setPermissionMap(String permission) {
    if (permission != null) {
      List<String> userList = new ArrayList<String>() ;
      for(String perm : permission.split(";")) {
        String userName = perm.substring(0,perm.lastIndexOf(" ")) ;
        if(!userList.contains(userName)) userList.add(userName) ;      
      }
      for(String user : userList) {
        StringBuilder sb = new StringBuilder() ;
        for(String perm : permission.split(";")) {
          if(perm.contains(user)) {
            if(sb.length() > 1) sb.append(";") ;
            sb.append(perm) ;
          }
        }
        permissions_.put(user, sb.toString()) ;
      }
    }
  }
  private List<SelectItemOption<String>>  getNodeType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.NT_UNSTRUCTURED, Utils.NT_UNSTRUCTURED)) ;
    options.add(new SelectItemOption<String>(Utils.NT_FOLDER, Utils.NT_FOLDER)) ;
    return options ;
  }
  public void refreshPermissionList() {
    StringBuilder labels = new StringBuilder() ;
    for(String perm : permissions_.keySet()){
      if(labels.length() > 0) labels.append(",") ;
      labels.append(perm) ;
    }
    UIFormInputSetWithAction step1 = getChildById(FIELD_STEP1) ;
    step1.setInfoField(FIELD_PERMISSION, labels.toString()) ;
    String[] actionInfor = {"EditPermission", "RemovePermission"} ;
    step1.setActionInfo(FIELD_PERMISSION, actionInfor) ;
  }

  protected void removePopup(String id) {
    getAncestorOfType(UIWorkspaceWizardContainer.class).removePopup(id) ;
  }
  private List<SelectItemOption<String>> getDbType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String dataType : JDBCWorkspaceDataContainer.DB_DIALECTS) {
      options.add(new SelectItemOption<String>(dataType, dataType)) ;
    }
    return options ;
  }
  public List<SelectItemOption<String>> getFilterType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("Binary", "Binary")) ;
    return options ;
  }
  protected void lockForm(boolean isLock) {
    boolean isEdiable = !isLock ;
    UIFormInputSetWithAction wsStep1 = getChildById(UIWorkspaceWizard.FIELD_STEP1) ;
    wsStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).setEditable(isEdiable) ;
    wsStep1.getUIFormSelectBox(UIWorkspaceWizard.FIELD_NODETYPE).setEnable(isEdiable) ;
    wsStep1.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISDEFAULT).setEnable(isEdiable) ;
    wsStep1.getUIStringInput(UIWorkspaceWizard.FIELD_TIMEOUT).setEditable(isEdiable) ;
    wsStep1.showActionInfo(isEdiable) ;
    UIFormInputSet wsStep2 = getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_CONTAINER).setEditable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).setEditable(isEdiable) ;
    wsStep2.getUIFormSelectBox(UIWorkspaceWizard.FIELD_DBTYPE).setEnable(isEdiable) ;
    wsStep2.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISMULTI).setEnable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STORETYPE).setEditable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).setEditable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_FILTER).setEnable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).setEditable(isEdiable) ;
    wsStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).setEditable(isEdiable) ;

    UIFormInputSet wsStep3 = getChildById(UIWorkspaceWizard.FIELD_STEP3) ;
    wsStep3.getUIStringInput(UIWorkspaceWizard.FIELD_QUERYHANDLER).setEditable(isEdiable) ;
    wsStep3.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).setEditable(isEdiable) ;
    wsStep3.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISCACHE).setEnable(isEdiable) ;
    wsStep3.getUIStringInput(UIWorkspaceWizard.FIELD_MAXSIZE).setEditable(isEdiable) ;
    wsStep3.getUIStringInput(UIWorkspaceWizard.FIELD_LIVETIME).setEditable(isEdiable) ;
  }
  public void setCurrentSep(int step){ currentStep_ = step ;}

  public int getCurrentStep() { return currentStep_; }
  public void setSelectedStep(int step){ selectedStep_ = step ;}
  public int getSelectedStep() { return selectedStep_; }

  public int getMaxStep(){return wizardMaxStep_ ;}

  public String[] getActions(){return actionMap_.get(selectedStep_) ;}

  public String getCurrentChild() {return chidrenMap_.get(selectedStep_) ;}

  public String[] getCurrentAction() {return actionMap_.get(selectedStep_) ;}

  @SuppressWarnings("unchecked")
  protected void refresh(WorkspaceEntry workSpace) throws Exception{
    reset() ;
    UIFormInputSetWithAction uiWSFormStep1 = getChildById(FIELD_STEP1) ;
    UIFormInputSet uiWSFormStep2 = getChildById(FIELD_STEP2) ;
    UIFormInputSet uiWSFormStep3 = getChildById(FIELD_STEP3) ;
    uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(false) ;
    UIRepositoryForm uiRepoForm = getAncestorOfType(UIECMAdminPortlet.class).findFirstComponentOfType(UIRepositoryForm.class) ;
    String repoName = uiRepoForm.getUIStringInput(UIRepositoryForm.FIELD_NAME).getValue() ;
    if(workSpace != null) {
      String name = workSpace.getName() ;
      boolean isDefaultWS = uiRepoForm.isDefaultWorkspace(name) ;
      List<SelectItemOption<String>> nodeTypeOptions = new ArrayList<SelectItemOption<String>>() ;
      nodeTypeOptions = getNodeType() ;
      String  lockTime = "0";
      if(workSpace.getLockManager() != null) {
        lockTime  = String.valueOf(workSpace.getLockManager().getTimeout()) ; 
      }
      String autoInitNodeType = workSpace.getAutoInitializedRootNt() ;
      String permission = workSpace.getAutoInitPermissions() ;
      String swapPath = "" ;
      String sourceName = "" ;
      String dbType = "" ;
      boolean isMutil = false ;
      ContainerEntry container = workSpace.getContainer() ;
      if(container != null) {
        swapPath = container.getParameterValue("swap-directory") ;
        sourceName = container.getParameterValue("sourceName") ;
        dbType = container.getParameterValue("dialect") ;
        isMutil = Boolean.parseBoolean(container.getParameterValue("multi-db")) ;
      }
      String maxBuffer = container.getParameterValue("max-buffer-size") ;
      String storeType = "" ;    
      String storePath = "" ;
      String filterType = "" ;
      String queryHandlerType = ""  ;
      String indexDir ="" ;
      boolean isEnableCache = true ;
      String maxCache ="" ;
      String liveTime ="" ;
      ArrayList<ValueStorageEntry> valueStore = container.getValueStorages() ;
      if(valueStore != null) {
        storeType = valueStore.get(0).getType() ;    
        storePath = valueStore.get(0).getParameterValue("path") ;
        filterType = valueStore.get(0).getFilters().get(0).getPropertyType() ;
      }
      QueryHandlerEntry queryHandler = workSpace.getQueryHandler() ;
      if(queryHandler != null) {
        queryHandlerType = queryHandler.getType() ;
        indexDir = queryHandler.getParameterValue("indexDir") ;
      }
      CacheEntry cache  = workSpace.getCache() ;
      if(cache != null) {
        isEnableCache = cache.isEnabled() ;
        maxCache =  cache.getParameterValue("maxSize")  ;
        liveTime = cache.getParameterValue("liveTime") ;
      }

      if(isNewWizard_) { 
        StringBuilder sb1 = new StringBuilder() ;
        StringBuilder sb2 = new StringBuilder() ;
        if(isNewRepo_) {
          uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEditable(true) ;
          sb1.append(swapPath.substring(0, swapPath.lastIndexOf("/")+1)).append(repoName).append("/") ;
          sb2.append(storePath.substring(0, storePath.lastIndexOf("/")+1)).append(repoName).append("/") ;
        } else {
          sb1.append(swapPath.substring(0, swapPath.lastIndexOf("/")+1));
          sb2.append(storePath.substring(0, storePath.lastIndexOf("/")+1));
        }
        uiWSFormStep2.getUIStringInput(FIELD_SWAPPATH).setValue(sb1.toString()) ;
        uiWSFormStep2.getUIStringInput(FIELD_STOREPATH).setValue(sb2.toString()) ;
        uiWSFormStep2.getUIFormSelectBox(FIELD_FILTER).setValue(filterType) ;
      } else {
        uiWSFormStep1.getUIStringInput(FIELD_NAME).setValue(name) ;
        setPermissionMap(permission) ;
        refreshPermissionList() ;
        uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(isDefaultWS) ;
        uiWSFormStep1.getUIFormSelectBox(FIELD_NODETYPE).setOptions(nodeTypeOptions) ;
        uiWSFormStep2.getUIStringInput(FIELD_SWAPPATH).setValue(swapPath) ;         
        uiWSFormStep2.getUIStringInput(FIELD_STOREPATH).setValue(storePath) ;
        uiWSFormStep2.getUIFormSelectBox(FIELD_FILTER).setValue(filterType) ;
      }
      uiWSFormStep1.getUIFormSelectBox(FIELD_NODETYPE).setValue(autoInitNodeType) ;      
      uiWSFormStep1.getUIStringInput(FIELD_TIMEOUT).setValue(lockTime) ;
      uiWSFormStep2.getUIStringInput(FIELD_CONTAINER).setValue(container.getType()) ;
      uiWSFormStep2.getUIStringInput(FIELD_SOURCENAME).setValue(sourceName) ;
      uiWSFormStep2.getUIFormSelectBox(FIELD_DBTYPE).setValue(dbType) ;
      uiWSFormStep2.getUIFormCheckBoxInput(FIELD_ISMULTI).setChecked(isMutil) ;
      uiWSFormStep2.getUIStringInput(FIELD_STORETYPE).setValue(storeType) ;
      uiWSFormStep2.getUIStringInput(FIELD_MAXBUFFER).setValue(maxBuffer) ;      
      uiWSFormStep3.getUIStringInput(FIELD_QUERYHANDLER).setValue(queryHandlerType);
      uiWSFormStep3.getUIStringInput(FIELD_INDEXPATH).setValue(indexDir) ;      
      uiWSFormStep3.getUIFormCheckBoxInput(FIELD_ISCACHE).setChecked(isEnableCache) ;
      uiWSFormStep3.getUIStringInput(FIELD_MAXSIZE).setValue(maxCache) ;
      uiWSFormStep3.getUIStringInput(FIELD_LIVETIME).setValue(liveTime) ;
    }
    if(isNewRepo_) {
      lockForm(false) ;
    } else {
      if(isNewWizard_) {
        lockForm(false) ;
        uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(false) ;
      } else {lockForm(true) ;}
    } 
    if( isNewWizard_)  isCheckValid_ = true ;
    else {
      if( isNewRepo_) {
        isCheckValid_ = true ;
        if(workSpace != null) selectedWsName_ = workSpace.getName() ;
      } else {
        isCheckValid_ = false ;
      }
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
  protected boolean isEmpty(String value) {
    return (value == null) || (value.trim().length() == 0) ;
  }

  protected String autoInitStorePath(String storePath, String repoName, String wsName) {
    StringBuilder sb  = new StringBuilder() ;
    sb.append(storePath.substring(0, storePath.lastIndexOf("/")+1)).append(repoName).append("/").append(wsName) ;
    return sb.toString() ;
  }
  protected String autoInitSwapPath(String swapPath, String repoName, String wsName) {
    StringBuilder sb  = new StringBuilder() ;
    sb.append(swapPath.substring(0, swapPath.lastIndexOf("/")+1)).append(repoName).append("/").append(wsName) ;
    return sb.toString() ;
  }
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    UIFormInputSetWithAction uiFormAction = getChildById(FIELD_STEP1) ;
    UIFormStringInput permissionField = uiFormAction.getUIStringInput(FIELD_PERMISSION) ;
    permissionField.setValue(value) ;
  }
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
      UIFormInputSetWithAction uiWSFormStep1 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP1) ;
      String wsName = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      boolean isDefault = uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).isChecked() ;
      String nodeType = uiWSFormStep1.getUIFormSelectBox(FIELD_NODETYPE).getValue() ;
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      if(uiFormWizard.isCheckValid_) {
        if(uiWSFormStep1.isRendered()) {
          if(uiFormWizard.isEmpty(wsName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.name-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if(uiFormWizard.isNewWizard_) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.isExistWorkspace(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }          
          } else if(uiFormWizard.isNewRepo_ ) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.getWorkspaceMap().containsKey(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }  
          }
          if(isDefault && !Utils.NT_UNSTRUCTURED.equals(nodeType)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.nodeType-invalid",null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(uiFormWizard.isNewWizard_ && uiFormWizard.permissions_.isEmpty()) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.permission-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if(!Utils.isNameEmpty(swapPath)) {
            if(!swapPath.contains(wsName))  swapPath = swapPath + wsName ;

          }
          if(!Utils.isNameEmpty(storePath)) {
            if(!storePath.contains(wsName))  storePath = storePath + wsName ;
          }
          uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).setValue(swapPath) ;
          uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).setValue(storePath) ;
        }
      }
      if(uiFormWizard.isNewWizard_){
        String swapPathAuto = swapPath ;
        String storePathAuto = storePath ;
        swapPathAuto = swapPath.substring(0,swapPath.lastIndexOf("/")+1) + wsName ;
        storePathAuto = storePath.substring(0,storePath.lastIndexOf("/")+1) + wsName ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).setValue(swapPathAuto) ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).setValue(storePathAuto) ;
      }
      uiFormWizard.viewStep(2) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public static class ViewStep3ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      UIFormInputSetWithAction uiWSFormStep1 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP1) ;
      String wsName = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String containerType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_CONTAINER).getValue() ;
      String storeType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STORETYPE).getValue() ;

      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;
      if(uiFormWizard.isCheckValid_) {
        if(uiWSFormStep1.isRendered()) {
          if(uiFormWizard.isEmpty(wsName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.name-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if(uiFormWizard.isNewWizard_) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.isExistWorkspace(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }          
          } else if(uiFormWizard.isNewRepo_ ) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.getWorkspaceMap().containsKey(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }  
          }
          if(uiFormWizard.isNewWizard_ && uiFormWizard.permissions_.isEmpty()) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.permission-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
         
        }
        if(uiWSFormStep2.isRendered()) {

          if(uiFormWizard.isEmpty(containerType)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.containerName-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
          if(uiFormWizard.isEmpty(sourceName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.sourceName-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
          if(uiFormWizard.isEmpty(storeType)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.storeType-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } 
          if(uiFormWizard.isEmpty(maxBuffer)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(Integer.parseInt(maxBuffer) <= 0) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(Utils.isNameEmpty(swapPath)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.swapPath-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } 
          if(uiFormWizard.isEmpty(storePath)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.storePath-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
        }
      }
      if(uiFormWizard.isNewWizard_){
        String swapPathAuto = swapPath ;
        String storePathAuto = storePath ;
        swapPathAuto = swapPath.substring(0,swapPath.lastIndexOf("/")+1) + wsName ;
        storePathAuto = storePath.substring(0,storePath.lastIndexOf("/")+1) + wsName ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).setValue(swapPathAuto) ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).setValue(storePathAuto) ;
      }
      uiFormWizard.viewStep(3) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  public static class FinishActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.removePopup(UIWorkspaceWizard.POPUPID) ;
      long lockTimeOutValue = 0 ;
      long bufferValue = 0 ;
      long maxSizeValue = 0 ;
      long liveTimeValue = 0 ;

      UIFormInputSetWithAction uiWSFormStep1 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP1) ;
      String name = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      String initNodeType = uiWSFormStep1.getUIFormSelectBox(UIWorkspaceWizard.FIELD_NODETYPE).getValue() ;
      boolean isDefault = uiWSFormStep1.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISDEFAULT).isChecked() ;
      String lockTimeOut = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_TIMEOUT).getValue() ;

      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
      String containerType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_CONTAINER).getValue() ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String dbType =  uiWSFormStep2.getUIFormSelectBox(UIWorkspaceWizard.FIELD_DBTYPE).getValue() ;
      boolean isMulti = uiWSFormStep2.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISMULTI).isChecked() ;
      String storeType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STORETYPE).getValue() ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String filterType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_FILTER).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;

      UIFormInputSet uiWSFormStep3 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP3) ;
      String queryHandlerType = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_QUERYHANDLER).getValue() ;
      String indexPath = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).getValue() ;
      boolean isCache = uiWSFormStep3.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISCACHE).isChecked() ;
      String maxSize = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_MAXSIZE).getValue() ;
      String liveTime = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_LIVETIME).getValue() ;
      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
      UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
      if(uiFormWizard.isCheckValid_){
        if(uiFormWizard.isEmpty(queryHandlerType)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.queryHandlerType-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        if(uiFormWizard.isEmpty(indexPath)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.indexPath-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        if(isCache){
          if(uiFormWizard.isEmpty(maxSize)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.maxSize-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(Integer.parseInt(maxBuffer) <= 0) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(uiFormWizard.isEmpty(liveTime)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.liveTime-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          maxSizeValue = Integer.parseInt(maxSize) ;
          liveTimeValue = Integer.parseInt(liveTime) ;
        }
        lockTimeOutValue = Integer.parseInt(lockTimeOut) ;
        bufferValue = Integer.parseInt(maxBuffer) ;
        if(uiFormWizard.isNewWizard_) {
          if(uiRepoForm.isExistWorkspace(name)){
            Object[] args = new Object[]{name}  ;        
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }          
        } 
      }
      WorkspaceEntry workspaceEntry = new WorkspaceEntry(name, initNodeType);
      StringBuilder permSb = new StringBuilder() ;
      for(String s : uiFormWizard.permissions_.values()) {
        permSb.append(s) ;
      }
      workspaceEntry.setAutoInitPermissions(permSb.toString()) ;
      LockManagerEntry lockEntry = new LockManagerEntry() ;
      lockEntry.setTimeout(lockTimeOutValue) ;
      workspaceEntry.setLockManager(lockEntry) ;
      workspaceEntry.setContainer(newContainerEntry(containerType, sourceName, dbType, isMulti,storeType, filterType, bufferValue, swapPath, storePath, true));
      workspaceEntry.setCache(newCacheEntry(isCache, maxSizeValue, liveTimeValue)) ;
      workspaceEntry.setQueryHandler(newQueryHandlerEntry(queryHandlerType, indexPath)) ;

      if(uiRepoForm.isAddnew_) {
        if(isDefault) uiRepoForm.defaulWorkspace_ = name ;
        if(uiFormWizard.isNewWizard_) {
          uiRepoForm.getWorkspaceMap().put(name, workspaceEntry) ;
        } else {
          uiRepoForm.getWorkspaceMap().remove(uiFormWizard.selectedWsName_) ;
          uiRepoForm.getWorkspaceMap().put(name, workspaceEntry) ;
        }
        uiRepoForm.refreshWorkspaceList() ;  
      }

      if(!uiRepoForm.isAddnew_ && uiFormWizard.isNewWizard_) {
        InitialContextInitializer ic = (InitialContextInitializer)uiFormWizard.getApplicationComponent(ExoContainer.class).
        getComponentInstanceOfType(InitialContextInitializer.class) ;
        if(ic != null) ic.recall() ;
        RepositoryService rService = (RepositoryService)uiFormWizard.getApplicationComponent(ExoContainer.class).
        getComponentInstanceOfType(RepositoryService.class);
        ManageableRepository manageRepository = rService.getRepository(uiRepoForm.repoName_);
        try {
          manageRepository.configWorkspace(workspaceEntry) ;
          manageRepository.createWorkspace(workspaceEntry.getName()) ;
          if(rService.getConfig().isRetainable()) {
            rService.getConfig().retain() ;
          }
          uiRepoForm.workspaceMap_.clear() ;
          for(WorkspaceEntry ws : manageRepository.getConfiguration().getWorkspaceEntries()) {
            uiRepoForm.workspaceMap_.put(ws.getName(), ws) ;
          }
          uiRepoForm.refreshWorkspaceList() ;   
        }
        catch (Exception e) {
          e.printStackTrace() ;
          return;
        }
      }
      UIPopupAction uiPopupAction = uiFormWizard.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoForm) ;
    }

    @SuppressWarnings("unchecked")
    private ContainerEntry newContainerEntry(String containerType, String sourceName, String dbType, boolean  isMulti,
        String storeType, String filterType, long bufferValue, String swapPath, String storePath, boolean isUpdateStore) {
      List containerParams = new ArrayList();
      containerParams.add(new SimpleParameterEntry("sourceName", sourceName)) ;
      containerParams.add(new SimpleParameterEntry("dialect", dbType)) ;
      containerParams.add(new SimpleParameterEntry("multi-db", String.valueOf(isMulti))) ;
      containerParams.add(new SimpleParameterEntry("update-storage", String.valueOf(isUpdateStore))) ;
      containerParams.add(new SimpleParameterEntry("max-buffer-size", String.valueOf(bufferValue))) ;
      containerParams.add(new SimpleParameterEntry("swap-directory", swapPath)) ;
      ContainerEntry containerEntry = new ContainerEntry(containerType, (ArrayList) containerParams) ;      
      containerEntry.setParameters(containerParams);

      ArrayList<ValueStorageFilterEntry> vsparams = new ArrayList<ValueStorageFilterEntry>();
      ValueStorageFilterEntry filterEntry = new ValueStorageFilterEntry();
      filterEntry.setPropertyType(filterType);
      vsparams.add(filterEntry);

      ValueStorageEntry valueStorageEntry = new ValueStorageEntry(storeType,
          vsparams);
      ArrayList<SimpleParameterEntry> spe = new ArrayList<SimpleParameterEntry>();
      spe.add(new SimpleParameterEntry("path", storePath));

      valueStorageEntry.setParameters(spe);
      valueStorageEntry.setFilters(vsparams);
      ArrayList list = new ArrayList(1);
      list.add(valueStorageEntry);
      containerEntry.setValueStorages(list);
      return containerEntry ;
    }

    @SuppressWarnings("unused")
    private ValueStorageEntry newValueStorageEntry(String storeType, String value, String filter) {
      ArrayList<ValueStorageFilterEntry> vsparams = new ArrayList<ValueStorageFilterEntry>();
      ValueStorageEntry valueStorageEntry = new ValueStorageEntry(storeType, vsparams)  ;
      return valueStorageEntry ;
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
    private QueryHandlerEntry newQueryHandlerEntry(String queryHandlerType, String indexPath) {
      ArrayList<SimpleParameterEntry> queryParams = new ArrayList<SimpleParameterEntry>() ;
      queryParams.add(new SimpleParameterEntry("indexDir", indexPath)) ;
      QueryHandlerEntry queryHandler = new QueryHandlerEntry(queryHandlerType, queryParams) ;
      queryHandler.setType(queryHandlerType) ;
      queryHandler.setParameters(queryParams) ;
      return queryHandler ;
    }
  }

  public static class NextActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.removePopup(POPUPID) ;
      UIFormInputSetWithAction uiWSFormStep1 = uiFormWizard.getChildById(FIELD_STEP1) ;
      String wsName = uiWSFormStep1.getUIStringInput(FIELD_NAME).getValue() ;
      boolean isDefault = uiWSFormStep1.getUIFormCheckBoxInput(FIELD_ISDEFAULT).isChecked() ;
      String nodeType = uiWSFormStep1.getUIFormSelectBox(FIELD_NODETYPE).getValue() ;
      String lockTimeOut = uiWSFormStep1.getUIStringInput(FIELD_TIMEOUT).getValue() ;
      String perm = null ;
      StringBuffer sb = new StringBuffer() ;        
      for(String s :  uiFormWizard.permissions_.values()) {
        sb.append(s) ;
      }
      if(sb != null) perm = sb.toString() ;
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
      String containerName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_CONTAINER).getValue() ;
      String storeType = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STORETYPE).getValue() ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String storePath = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String swapPath =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      String maxBuffer =  uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;

      UIFormInputSet uiWSFormStep3 = uiFormWizard.getChildById(UIWorkspaceWizard.FIELD_STEP3) ;
      String indexPath = uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).getValue() ;

      UIApplication uiApp = uiFormWizard.getAncestorOfType(UIApplication.class) ;
      if(uiFormWizard.isCheckValid_) {
        if(uiWSFormStep1.isRendered()) {
          if(uiFormWizard.isEmpty(wsName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.name-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if(uiFormWizard.isNewWizard_) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.isExistWorkspace(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }  
          } else if(uiFormWizard.isNewRepo_ ) {
            UIRepositoryFormContainer formContainer = uiFormWizard.getAncestorOfType(UIRepositoryFormContainer.class) ;
            UIRepositoryForm uiRepoForm = formContainer.findFirstComponentOfType(UIRepositoryForm.class) ;
            if(uiRepoForm.getWorkspaceMap().containsKey(wsName)){
              Object[] args = new Object[]{wsName}  ;        
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.wsname-exist", args)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
              return ;
            }  
          }
          if(isDefault && !Utils.NT_UNSTRUCTURED.equals(nodeType)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.nodeType-invalid",null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(uiFormWizard.isNewWizard_ && uiFormWizard.isEmpty(perm)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.permission-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if(Utils.isNameEmpty(lockTimeOut)) {
            uiWSFormStep1.getUIStringInput(FIELD_TIMEOUT).setValue("0") ;
          } else {
            for(int i = 0; i < lockTimeOut.length(); i++) {
              char c = lockTimeOut.charAt(i);
              if(Character.isDigit(c)) continue ;
              Object[] args = { lockTimeOut } ;
              uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.invalid-input", args, 
                  ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
          } 
        }
        if(uiWSFormStep2.isRendered()) {
          if(uiFormWizard.isEmpty(containerName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.containerName-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
          if(uiFormWizard.isEmpty(sourceName)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.sourceName-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
          if(uiFormWizard.isEmpty(storeType)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.storeType-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }  
          if(uiFormWizard.isEmpty(maxBuffer)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-require", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(Integer.parseInt(maxBuffer) <= 0) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-zero", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
            return ;
          }
          if(uiFormWizard.isEmpty(swapPath)) {
            uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.swapPath-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } 
          if(uiFormWizard.isEmpty(storePath)) {
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
      }
      if(uiFormWizard.isNewWizard_){
        String swapPathAuto = swapPath ;
        String storePathAuto = storePath ;
        swapPathAuto = swapPath.substring(0,swapPath.lastIndexOf("/")+1) + wsName ;
        storePathAuto = storePath.substring(0,storePath.lastIndexOf("/")+1) + wsName ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).setValue(swapPathAuto) ;
        uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).setValue(storePathAuto) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIWorkspaceWizardContainer.class)) ; 
    }
  }

  public static class BackActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.removePopup(UIWorkspaceWizard.POPUPID) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormWizard.getAncestorOfType(UIWorkspaceWizardContainer.class)) ;
    }
  }
  public static class AddPermissionActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiWizardForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiWizardForm.getAncestorOfType(UIWorkspaceWizardContainer.class).
      getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIPermissionContainer.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }

  }
  public static class EditPermissionActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiForm = event.getSource() ;
      String permName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIWorkspaceWizardContainer.class).
      getChild(UIPopupAction.class) ;
      UIPermissionContainer uiContainer = uiPopupAction.activate(UIPermissionContainer.class, 600) ;
      uiContainer.setValues(permName, uiForm.permissions_.get(permName)) ;
      uiContainer.lockForm(!uiForm.isNewRepo_ && !uiForm.isNewWizard_) ;
      uiForm.refreshPermissionList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIWorkspaceWizardContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class RemovePermissionActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiForm = event.getSource() ;
      String permName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.permissions_.remove(permName) ;
      uiForm.refreshPermissionList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIWorkspaceWizardContainer.class)) ;
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

  public void setContainerName(String value) {
    UIFormInputSet uiWSFormStep2 =  getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
    uiWSFormStep2.getUIStringInput(FIELD_CONTAINER).setValue(value) ;
  }
  public void setStoreTypeName(String value) {
    UIFormInputSet uiWSFormStep2 =  getChildById(UIWorkspaceWizard.FIELD_STEP2) ;
    uiWSFormStep2.getUIStringInput(FIELD_STORETYPE).setValue(value) ;
  }
  public void setQueryHandlerName(String value) {
    UIFormInputSet uiWSFormStep3 =  getChildById(UIWorkspaceWizard.FIELD_STEP3) ;
    uiWSFormStep3.getUIStringInput(FIELD_QUERYHANDLER).setValue(value) ;
  }

  public static class SelectContainerActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiWizard = event.getSource() ;
      UIPopupAction uiPopup = uiWizard.getAncestorOfType(UIWorkspaceWizardContainer.class).
      getChild(UIPopupAction.class);
      UIRepositoryValueSelect uiSelect = uiPopup.activate(UIRepositoryValueSelect.class, 500) ;
      uiSelect.isSetContainer_ = true ;
      List<ClassData> datas = new ArrayList<ClassData>() ;
      datas.add(new ClassData(UIWorkspaceWizard.KEY_CONTAINERTYPE)) ;
      uiSelect.updateGrid(datas) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    } 
  }
  public static class SelectStoreActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiWizard = event.getSource() ;
      UIPopupAction uiPopup = uiWizard.getAncestorOfType(UIWorkspaceWizardContainer.class).
      getChild(UIPopupAction.class);
      UIRepositoryValueSelect uiSelect = uiPopup.activate(UIRepositoryValueSelect.class, 500) ;
      uiSelect.isSetStoreType_ = true ;
      List<ClassData> datas = new ArrayList<ClassData>() ;
      datas.add(new ClassData(UIWorkspaceWizard.KEY_STORETYPE)) ;
      uiSelect.updateGrid(datas) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    } 
  }

  public static class SelectQueryHandlerActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiWizard = event.getSource() ;
      UIPopupAction uiPopup = uiWizard.getAncestorOfType(UIWorkspaceWizardContainer.class).
      getChild(UIPopupAction.class);
      UIRepositoryValueSelect uiSelect = uiPopup.activate(UIRepositoryValueSelect.class, 500) ;
      uiSelect.isSetQueryHandler_ = true ;
      List<ClassData> datas = new ArrayList<ClassData>() ;
      datas.add(new ClassData(UIWorkspaceWizard.KEY_QUERYHANDLER)) ;
      uiSelect.updateGrid(datas) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    } 
  }

}
