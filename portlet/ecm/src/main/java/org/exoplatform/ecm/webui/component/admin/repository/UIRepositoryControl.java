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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
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
        @EventConfig(listeners = UIRepositoryControl.SelectRepoActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.EditRepositoryActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.RemoveRepositoryActionListener.class, confirm="UIRepositoryControl.msg.confirm-delete"),
        @EventConfig(listeners = UIRepositoryControl.AddRepositoryActionListener.class)
    }
)

public class UIRepositoryControl extends UIContainer {


  private Map<String, RepositoryEntry> repoMap_ = new HashMap<String, RepositoryEntry>() ;

  public UIRepositoryControl() throws Exception{
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupControl");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowControl") ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    for(Object re : rservice.getConfig().getRepositoryConfigurations()) {
      repoMap_.put(((RepositoryEntry)re).getName(), ((RepositoryEntry)re)) ;
    }
    UIDropDownItemSelector uiDopDownSelector = addChild(UIDropDownItemSelector.class, null, null) ;
    uiDopDownSelector.setOptions(getRepoItem()) ;
    uiDopDownSelector.setTitle("Repository");
    uiDopDownSelector.setOnServer(true);
    uiDopDownSelector.setOnChange("SelectRepo");
    uiDopDownSelector.setSelected(0) ;
  }

  public List<SelectItemOption<String>> getRepoItem(){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(String name : repoMap_.keySet()) {
      options.add(new SelectItemOption<String>(name, name)) ;
    }
    return options ;
  }

  public boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }

  public boolean isExistRepo(String repoName) {return repoMap_.containsKey(repoName) ;}

  public void removeRepo(String repoName) {repoMap_.remove(repoName) ;}

  public void addRepo(RepositoryEntry re) {repoMap_.put(re.getName(), re) ;}

  public RepositoryEntry getRepo(String name) {return repoMap_.get(name) ;}

  public void initPopup(String popupId ,UIComponent uiForm) throws Exception {
    removeChildById(popupId) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, popupId) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setWindowSize(560,400) ;      
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public static class SelectRepoActionListener extends EventListener<UIRepositoryControl>{
    public void execute(Event<UIRepositoryControl> arg0) throws Exception {
      System.out.println("Come here");
    }

  }

  public static class EditRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiSelect.getSelectedValue() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.refresh(uiControl.getRepo(repoName)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class RemoveRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiSelect.getSelectedValue() ;
      if(uiControl.isDefaultRepo(repoName)) {
        UIApplication uiApp = uiControl.getAncestorOfType(UIApplication.class) ;
        Object[] args = new Object[]{repoName} ;
        uiApp.addMessage(new ApplicationMessage("UIRepositoryControl.msg.cannot-deleteRepo", args)) ;        
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
        return  ;
      }
      uiControl.removeRepo(repoName) ;
      uiSelect.setOptions(uiControl.getRepoItem()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ;
    }
  }

  public static class AddRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.refresh(uiControl.getRepo(null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;    }
  }


}
