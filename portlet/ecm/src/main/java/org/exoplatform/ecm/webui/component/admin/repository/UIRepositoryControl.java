/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIDropDownItemSelector;
import org.exoplatform.webui.component.UIPopupWindow;
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
        @EventConfig(listeners = UIRepositoryControl.EditRepositoryActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.RemoveRepositoryActionListener.class, confirm="UIRepositoryControl.msg.confirm-delete"),
        @EventConfig(listeners = UIRepositoryControl.AddRepositoryActionListener.class)
    }
)

public class UIRepositoryControl extends UIContainer {


  protected String repoName_  ;
  private Map<String, RepositoryEntry> repoMap_ = new HashMap<String, RepositoryEntry>() ;

  public UIRepositoryControl() throws Exception{
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupControl");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowControl") ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    String defaultName = rservice.getDefaultRepository().getConfiguration().getName() ;
    for(Object obj : rservice.getConfig().getRepositoryConfigurations()) { 
      RepositoryEntry repo  = (RepositoryEntry)obj ;
      repoMap_.put(repo.getName(), repo) ;
    }
   UIRepositorySelectForm uiSelectForm = createUIComponent(UIRepositorySelectForm.class, null, null) ;
   uiSelectForm.setOptionValue(getRepoItem()) ;
   uiSelectForm.setSelectedValue(defaultName) ;
   addChild(uiSelectForm) ;
   
   /* UIDropDownItemSelector uiDopDownSelector = addChild(UIDropDownItemSelector.class, null, null) ;
    uiDopDownSelector.setId("UIDropDownRepositoryControl") ;
    uiDopDownSelector.setOptions(getRepoItem()) ;    
    uiDopDownSelector.setOnServer(true);
    uiDopDownSelector.setSelected(defaultName) ;
    uiDopDownSelector.setOnChange("SelectRepo");*/  
    repoName_ = defaultName ;
  }

  protected Map<String, RepositoryEntry> getRepoMap(){return repoMap_ ;}

  protected List<SelectItemOption<String>> getRepoItem(){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(String name : getRepoMap().keySet()) { 
      options.add(new SelectItemOption<String>(name , name)) ;
    }
    return options ;
  }

  protected boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }

  protected void reloadValue(){
    getChild(UIRepositorySelectForm.class).setOptionValue(getRepoItem()) ;
  }
  /*protected void setSelectedValue(String repoName) {
    UIDropDownItemSelector uiDopDownSelector = getChild(UIDropDownItemSelector.class) ;
    uiDopDownSelector.setSelected(repoName) ;
  }*/
  
  public static class EditRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      /*UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;    */  
      String repoName = uiControl.getChild(UIRepositorySelectForm.class).getSelectedValue() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.isAddnew_ = false ;
      RepositoryService rservice = uiControl.getApplicationComponent(RepositoryService.class) ;
      ManageableRepository repo = rservice.getRepository(repoName) ;
      uiForm.refresh(repo.getConfiguration()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class RemoveRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {     
      UIRepositoryControl uiControl = event.getSource() ;
      //UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiControl.getChild(UIRepositorySelectForm.class).getSelectedValue() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.isAddnew_ = false ;
      RepositoryService rservice = uiControl.getApplicationComponent(RepositoryService.class) ;
      if(rservice.getDefaultRepository().getConfiguration().getName().equals(repoName)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        Object[] args = new Object[]{repoName}  ;        
        uiApp.addMessage(new ApplicationMessage("UIRepositoryControl.msg.cannot-deleteRepo", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      
      ManageableRepository repo = rservice.getRepository(repoName) ;
      uiForm.refresh(repo.getConfiguration()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class AddRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.isAddnew_ = true ;
      uiForm.refresh(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }


}
