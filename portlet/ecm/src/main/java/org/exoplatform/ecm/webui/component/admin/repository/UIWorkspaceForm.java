/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
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
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",   
    events = {
      @EventConfig(listeners = UIWorkspaceForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIWorkspaceForm.ResetActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIWorkspaceForm.CloseActionListener.class)
    }  
)
public class UIWorkspaceForm extends UIForm {  
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_DESCRIPTION = "description" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;
  final static public String FIELD_ENABLECACHE = "enableCache" ;
  final static public String FIELD_MAXSIZE = "maxSize" ;
  final static public String FIELD_LIVETIME = "liveTime" ;
  final static public String FIELD_ISQUERYHANDLER = "queryHandler" ;
  public boolean isAddnew_ = true ;


  public UIWorkspaceForm() throws Exception { 
    addChild(new UIFormStringInput(FIELD_NAME,FIELD_NAME, null).addValidator(EmptyFieldValidator.class)) ;    
    addChild(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISDEFAULT, FIELD_ISDEFAULT, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISQUERYHANDLER, FIELD_ISQUERYHANDLER, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ENABLECACHE,FIELD_ENABLECACHE, null)) ;
    addChild(new UIFormStringInput(FIELD_MAXSIZE, FIELD_MAXSIZE, null).addValidator(NumberFormatValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_LIVETIME, FIELD_LIVETIME, null).addValidator(NumberFormatValidator.class)) ;
    setActions(new String[] {"Save", "Reset", "Close"}) ;
  }  

  public void refresh(WorkspaceData workspace){
    if(workspace == null) { 
      reset() ; 
      isAddnew_ = true ;
    } else {      
      getUIStringInput(FIELD_NAME).setValue(workspace.getName()) ;
      getUIFormTextAreaInput(FIELD_DESCRIPTION).setValue(workspace.getDescription()) ;
      getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(workspace.isDefault()) ;
      getUIFormCheckBoxInput(FIELD_ISQUERYHANDLER).setChecked(workspace.isQueryHandler()) ;
      getUIFormCheckBoxInput(FIELD_ENABLECACHE).setChecked(workspace.isEnableCache()) ;
      getUIStringInput(FIELD_MAXSIZE).setValue(String.valueOf(workspace.getCahceMaxSize())) ;
      getUIStringInput(FIELD_LIVETIME).setValue(String.valueOf(workspace.getCacheLiveTime())) ;
      isAddnew_ = false ;
    }
  }

  public static class WorkspaceData extends WorkspaceEntry {
    String name ;
    String description ;
    boolean isDefault ;
    boolean isQueryHandler ;
    boolean enableCache ;
    long maxCacheSize ;
    long cacheLiveTime ;
    public WorkspaceData() {}
    public WorkspaceData(String name, String des, boolean isDefault, 
        boolean isHandler, boolean enableCache,long maxSize, long liveTime) {
      this.name = name ;
      description = des ;
      this.isDefault = isDefault ;
      isQueryHandler = isHandler ;
      this.enableCache = enableCache ;
      maxCacheSize = maxSize ;
      cacheLiveTime = liveTime ;
    }
    public String getName() {return name ;}
    public String getDescription() {return description ;}
    public boolean isDefault() {return isDefault ;}
    public boolean isQueryHandler() {return isQueryHandler ;}
    public boolean isEnableCache() {return enableCache ;}
    public long getCahceMaxSize() {return maxCacheSize;}
    public long getCacheLiveTime() {return cacheLiveTime ;}

  }

  public static class SaveActionListener extends EventListener<UIWorkspaceForm>{
    public void execute(Event<UIWorkspaceForm> event) throws Exception{
      UIWorkspaceForm uiWSForm = event.getSource() ;
      String wsName = uiWSForm.getUIStringInput(UIWorkspaceForm.FIELD_NAME).getValue() ;
      String wsDes = uiWSForm.getUIFormTextAreaInput(UIWorkspaceForm.FIELD_DESCRIPTION).getValue() ;
      boolean wsIsdefault = uiWSForm.getUIFormCheckBoxInput(UIWorkspaceForm.FIELD_ISDEFAULT).isChecked() ;
      boolean isQueryHandler =  uiWSForm.getUIFormCheckBoxInput(UIWorkspaceForm.FIELD_ISQUERYHANDLER).isChecked() ;
      boolean enableCache =  uiWSForm.getUIFormCheckBoxInput(UIWorkspaceForm.FIELD_ENABLECACHE).isChecked() ;
      UIFormStringInput maxSize = uiWSForm.getUIStringInput(UIWorkspaceForm.FIELD_MAXSIZE) ;
      UIFormStringInput liveTime = uiWSForm.getUIStringInput(UIWorkspaceForm.FIELD_LIVETIME) ;
      long maxSizeValue = 0 ;
      long liveTimeValue = 0 ;
      if(enableCache){
        UIApplication uiApp = uiWSForm.getAncestorOfType(UIApplication.class) ;
        if((maxSize.getValue() == null) ||(maxSize.getValue().trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceForm.msg.maxSize-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        if((liveTime.getValue() == null) ||(liveTime.getValue().trim().length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceForm.msg.liveTime-require", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }
        maxSizeValue = Integer.parseInt(maxSize.getValue()) ;
        liveTimeValue = Integer.parseInt(liveTime.getValue()) ;
      }
      UIRepositoryManager uiManager = uiWSForm.getAncestorOfType(UIRepositoryManager.class) ;
      UIRepositoryForm uiRepoForm = uiManager.findFirstComponentOfType(UIRepositoryForm.class) ;
      if(uiWSForm.isAddnew_) {
        if(uiRepoForm.isExistWorkspace(wsName)){
          UIApplication uiApp = uiWSForm.getAncestorOfType(UIApplication.class) ;
          Object[] args = new Object[]{wsName}  ;        
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceForm.msg.wsname-exist", args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
          return ;
        }          
      } 
      WorkspaceData wsdata = new WorkspaceData(wsName, wsDes, wsIsdefault, isQueryHandler, 
                                               enableCache, maxSizeValue, liveTimeValue) ;
      uiRepoForm.addWorkspaceMap(wsdata) ;      
      uiRepoForm.refreshLabel() ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;  
    }
  }
  public static class ResetActionListener extends EventListener<UIWorkspaceForm>{
    public void execute(Event<UIWorkspaceForm> event) throws Exception{
      UIWorkspaceForm uiForm = event.getSource() ;
      uiForm.refresh(null) ;
    }
  }
  public static class CloseActionListener extends EventListener<UIWorkspaceForm>{
    public void execute(Event<UIWorkspaceForm> event) throws Exception{
      UIWorkspaceForm uiForm = event.getSource() ;
      uiForm.refresh(null) ;
      UIRepositoryManager uiManager = uiForm.getAncestorOfType(UIRepositoryManager.class) ;
      uiManager.removeChildById(UIRepositoryForm.POPUP_WORKSPACE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;  
    }
  }

}
