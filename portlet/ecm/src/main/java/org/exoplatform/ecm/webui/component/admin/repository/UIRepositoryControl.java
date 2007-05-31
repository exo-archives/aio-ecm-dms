/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
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


  public String repoName_  ;

  public UIRepositoryControl() throws Exception{
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupControl");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowControl") ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    String defaultName = rservice.getDefaultRepository().getConfiguration().getName() ;
    UIDropDownItemSelector uiDopDownSelector = addChild(UIDropDownItemSelector.class, null, null) ;
    uiDopDownSelector.setId("UIDropDownRepositoryControl") ;
    uiDopDownSelector.setOptions(getRepoItem()) ;    
    uiDopDownSelector.setOnServer(true);
    uiDopDownSelector.setSelected(defaultName) ;
    uiDopDownSelector.setOnChange("SelectRepo");    
    repoName_ = defaultName ;
  }

  public List<SelectItemOption<String>> getRepoItem(){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    for(Object obj : rservice.getConfig().getRepositoryConfigurations()) { 
      RepositoryEntry repo  = (RepositoryEntry)obj ;
    options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
    }
    return options ;
  }

  public boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }

  public void reloadValue(){
    UIDropDownItemSelector uiDopDownSelector = getChild(UIDropDownItemSelector.class) ;
    uiDopDownSelector.setOptions(getRepoItem()) ;
  }
  public static class SelectRepoActionListener extends EventListener<UIRepositoryControl>{
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiDDSelect = uiControl.getChildById("UIDropDownRepositoryControl") ;
      uiControl.repoName_ = uiDDSelect.getSelectedValue() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ;
    }

  }

  public static class EditRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIDropDownItemSelector uiSelect = uiControl.getChild(UIDropDownItemSelector.class) ;      
      String repoName = uiSelect.getSelectedValue() ;
      System.out.println("repoName==="+ repoName);
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryForm uiForm = uiPopupAction.activate(UIRepositoryForm.class, 600) ;
      uiForm.isAddnew_ = false ;
      RepositoryService rservice = uiControl.getApplicationComponent(RepositoryService.class) ;
      ManageableRepository repo = rservice.getRepository(uiControl.repoName_) ;
      uiForm.refresh(repo) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class RemoveRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {      
    }
  }

  public static class AddRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {      
    }
  }


}
