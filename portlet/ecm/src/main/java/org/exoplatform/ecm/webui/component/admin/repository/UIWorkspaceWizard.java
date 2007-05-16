/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.ecm.webui.component.admin.repository.UIRepositoryManager.WorkspaceData;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTabPane;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
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
 * May 11, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/admin/UIWorkspaceWizard.gtmpl" ,
    events = {
        @EventConfig(listeners = UIWorkspaceWizard.FinishActionListener.class),
        @EventConfig(listeners = UIWorkspaceWizard.NextActionListener.class ),
        @EventConfig(listeners = UIWorkspaceWizard.BackActionListener.class),
        @EventConfig(listeners = UIWorkspaceWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIWorkspaceWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIWorkspaceWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIWorkspaceWizard.ViewStep4ActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIWorkspaceWizard.CancelActionListener.class)
    }

)
public class UIWorkspaceWizard extends UIFormTabPane {
  private int wizardMaxStep_ = 4 ;
  private int selectedStep_ = 1 ;
  private int currentStep_ = 0 ;
  private boolean isAddnew_ = true ;
  private Map<Integer, String> chidrenMap_ = new HashMap<Integer, String>() ; 

  private Map<Integer, String[]> actionMap_ = new HashMap<Integer, String[]>() ;

  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_DESCRIPTION = "description" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;

  final static public String FIELD_SOURCENAME = "sourceName" ;  
  final static public String FIELD_DBTYPE = "dbType" ;
  final static public String FIELD_ISMULTI = "isMulti" ;

  final static public String FIELD_ISUPDATESTORE = "isUpdateStore" ;
  final static public String FIELD_MAXBUFFER = "maxBuffer" ;
  final static public String FIELD_SWAPPATH = "swapPath" ;


  final static public String FIELD_STOREPATH = "storePath" ;  
  final static public String FIELD_FILTERTYPE = "filterType" ;

  final static public String FIELD_INDEXPATH= "indexPath" ;
  final static public String FIELD_ISCACHE = "isCache" ;
  final static public String FIELD_MAXSIZE = "maxSize" ;
  final static public String FIELD_LIVETIME = "liveTime" ;

  final static public String fIELD_STEP1 = "step1" ;
  final static public String fIELD_STEP2 = "step2" ;
  final static public String fIELD_STEP3 = "step3" ;
  final static public String fIELD_STEP4 = "step4" ;

  public UIWorkspaceWizard() throws Exception {
    super("UIWorkspaceWizard");

    chidrenMap_.put(1, fIELD_STEP1) ;
    chidrenMap_.put(2, fIELD_STEP2) ;
    chidrenMap_.put(3, fIELD_STEP3) ;
    chidrenMap_.put(4, fIELD_STEP4) ;

    actionMap_.put(1, new String[]{"Next", "Cancel"}) ;
    actionMap_.put(2, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(3, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(4, new String[]{"Back", "Finish", "Cancel"}) ;

    UIFormInputSet step1 = new UIFormInputSet(fIELD_STEP1) ;
    step1.addChild(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ;
    step1.addChild(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    step1.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISDEFAULT, FIELD_ISDEFAULT, null)) ;

    UIFormInputSet step2 = new UIFormInputSet(fIELD_STEP2) ;
    step2.addChild(new UIFormStringInput(FIELD_SOURCENAME, FIELD_SOURCENAME, null)) ;
    step2.addChild(new UIFormSelectBox(FIELD_DBTYPE, FIELD_DBTYPE, getDbType())) ;
    step2.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISMULTI, FIELD_ISMULTI, null)) ;

    UIFormInputSet step3 = new UIFormInputSet(fIELD_STEP3) ;
    step3.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISUPDATESTORE, FIELD_ISUPDATESTORE, null)) ;
    step3.addChild(new UIFormStringInput(FIELD_MAXBUFFER, FIELD_MAXBUFFER, null).addValidator(NumberFormatValidator.class)) ;
    step3.addChild(new UIFormStringInput(FIELD_SWAPPATH, FIELD_SWAPPATH, null)) ;

    UIFormInputSet step4 = new UIFormInputSet(fIELD_STEP4) ;
    step4.addChild(new UIFormStringInput(FIELD_STOREPATH, FIELD_STOREPATH, null)) ;
    step4.addChild(new UIFormSelectBox(FIELD_FILTERTYPE, FIELD_FILTERTYPE, getFilterType())) ;
    step4.addChild(new UIFormStringInput(FIELD_INDEXPATH, FIELD_INDEXPATH, null)) ;
    step4.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISCACHE, FIELD_ISCACHE, null)) ;
    step4.addChild(new UIFormStringInput(FIELD_MAXSIZE, FIELD_MAXSIZE, null).addValidator(NumberFormatValidator.class)) ;
    step4.addChild(new UIFormStringInput(FIELD_LIVETIME, FIELD_LIVETIME, null).addValidator(NumberFormatValidator.class)) ;

    addUIComponentInput(step1) ;
    addUIComponentInput(step2) ;
    addUIComponentInput(step3) ;
    addUIComponentInput(step4) ;

    setRenderedChild(getCurrentChild()) ;
  }

  public void storeChildrenMap() {

  }

  public List<SelectItemOption<String>> getDbType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("generic", "generic")) ; 
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
 
  public void refresh(WorkspaceData workSpace) {
    if(workSpace == null) { 
      reset() ; 
      isAddnew_ = true ;
    } else {      
      isAddnew_ = false ;
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
  
  public static class ViewStep1ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.viewStep(1) ;
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
    }
    
  }
  
  public static class ViewStep2ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.viewStep(2) ;
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
    }
    
  }
  
  public static class ViewStep3ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.viewStep(3) ; 
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
    }
    
  }
  
  public static class ViewStep4ActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.viewStep(4) ;
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
    }
    
  }
  
  public static class FinishActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      
      UIFormInputSet uiWSFormStep1 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP1) ;
      String name = uiWSFormStep1.getUIStringInput(UIWorkspaceWizard.FIELD_NAME).getValue() ;
      String description = uiWSFormStep1.getUIFormTextAreaInput(UIWorkspaceWizard.FIELD_DESCRIPTION).getValue() ;
      boolean isDefault = uiWSFormStep1.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISDEFAULT).isChecked() ;
      
      UIFormInputSet uiWSFormStep2 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP2) ;
      String sourceName = uiWSFormStep2.getUIStringInput(UIWorkspaceWizard.FIELD_SOURCENAME).getValue() ;
      String dbType =  uiWSFormStep2.getUIFormSelectBox(UIWorkspaceWizard.FIELD_DBTYPE).getValue() ;
      boolean isMulti = uiWSFormStep2.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISMULTI).isChecked() ;
      
      UIFormInputSet uiWSFormStep3 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP3) ;
      boolean isUpdateStore = uiWSFormStep3.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISUPDATESTORE).isChecked() ;
      String maxBuffer =  uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_MAXBUFFER).getValue() ;
      String swapPath =  uiWSFormStep3.getUIStringInput(UIWorkspaceWizard.FIELD_SWAPPATH).getValue() ;
      
      UIFormInputSet uiWSFormStep4 = uiFormWizard.getChildById(UIWorkspaceWizard.fIELD_STEP4) ;
      String storePath = uiWSFormStep4.getUIStringInput(UIWorkspaceWizard.FIELD_STOREPATH).getValue() ;
      String filterType = uiWSFormStep4.getUIFormSelectBox(UIWorkspaceWizard.FIELD_FILTERTYPE).getValue() ;
      String indexPath = uiWSFormStep4.getUIStringInput(UIWorkspaceWizard.FIELD_INDEXPATH).getValue() ;
      boolean isCache = uiWSFormStep4.getUIFormCheckBoxInput(UIWorkspaceWizard.FIELD_ISCACHE).isChecked() ;
      String maxSize = uiWSFormStep4.getUIStringInput(UIWorkspaceWizard.FIELD_MAXSIZE).getValue() ;
      String liveTime = uiWSFormStep4.getUIStringInput(UIWorkspaceWizard.FIELD_LIVETIME).getValue() ;
     
      long bufferValue = 0 ;
      long maxSizeValue = 0 ;
      long liveTimeValue = 0 ;
      if(isUpdateStore) {
        UIApplication uiApp = uiWSFormStep1.getAncestorOfType(UIApplication.class) ;
        if((maxBuffer == null) ||(maxBuffer.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.buffer-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
      }
      if(isCache){
        UIApplication uiApp = uiWSFormStep1.getAncestorOfType(UIApplication.class) ;
        if((maxSize == null) ||(maxSize.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.maxSize-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if((liveTime == null) ||(liveTime.trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceWizard.msg.liveTime-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        bufferValue = Integer.parseInt(maxBuffer) ;
        maxSizeValue = Integer.parseInt(maxSize) ;
        liveTimeValue = Integer.parseInt(liveTime) ;
      }
      
      UIRepositoryManager uiManager = uiWSFormStep1.getAncestorOfType(UIRepositoryManager.class) ;
      UIRepositoryForm uiRepoForm = uiManager.findFirstComponentOfType(UIRepositoryForm.class) ;
      if(uiFormWizard.isAddnew_) {
        if(uiRepoForm.isExistWorkspace(name)){
          UIApplication uiApp = uiWSFormStep1.getAncestorOfType(UIApplication.class) ;
          Object[] args = new Object[]{name}  ;        
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceForm.msg.wsname-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }          
      } 
     WorkspaceData wsdata = new WorkspaceData(name, description, isDefault, sourceName, dbType,
         isMulti,isUpdateStore, bufferValue, swapPath, storePath, filterType, indexPath, 
         isCache,maxSizeValue, liveTimeValue ) ;
      uiRepoForm.addWorkspaceMap(wsdata) ;      
      uiRepoForm.refreshLabel() ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ; 
    }
  }
  
  public static class NextActionListener extends EventListener<UIWorkspaceWizard>{
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
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
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
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
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getChildById(UIRepositoryForm.POPUP_WORKSPACE)) ; 
    }
  }

  public static class CancelActionListener extends EventListener<UIWorkspaceWizard> {
    public void execute(Event<UIWorkspaceWizard> event) throws Exception {
      UIWorkspaceWizard uiFormWizard = event.getSource() ;
      uiFormWizard.refresh(null) ;
      UIRepositoryManager uiManager = uiFormWizard.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;  
    }
  }

}
