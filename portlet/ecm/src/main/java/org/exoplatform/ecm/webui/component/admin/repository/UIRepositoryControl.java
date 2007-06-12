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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.model.SelectItemOption;
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

  public UIRepositoryControl() throws Exception{
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    String defaultName = rservice.getDefaultRepository().getConfiguration().getName() ;
    UIRepositorySelectForm uiSelectForm = createUIComponent(UIRepositorySelectForm.class, null, null) ;
    uiSelectForm.setOptionValue(getRepoItem()) ;
    uiSelectForm.setSelectedValue(defaultName) ;
    uiSelectForm.setActionEvent() ;
    addChild(uiSelectForm) ;
    uiSelectForm.setSelectedValue(defaultName) ;
  }

  protected List<SelectItemOption<String>> getRepoItem(){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    for(Object obj : rservice.getConfig().getRepositoryConfigurations()) { 
      RepositoryEntry repo  = (RepositoryEntry)obj ;
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
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

  public static class EditRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      String repoName = uiControl.getChild(UIRepositorySelectForm.class).getSelectedValue() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryFormContainer uiForm = uiPopupAction.activate(UIRepositoryFormContainer.class, 600) ;
      RepositoryService rservice = uiControl.getApplicationComponent(RepositoryService.class) ;
      ManageableRepository repo = rservice.getRepository(repoName) ;
      uiForm.refresh(false, repo.getConfiguration()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  public static class RemoveRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {     
      UIRepositoryControl uiControl = event.getSource() ;
      String repoName = uiControl.getChild(UIRepositorySelectForm.class).getSelectedValue() ;
      RepositoryService rservice = uiControl.getApplicationComponent(RepositoryService.class) ;
      UIApplication uiApp = uiControl.getAncestorOfType(UIApplication.class) ;
      if(rservice.canRemoveRepository(repoName)) {
        try {
          rservice.removeRepository(repoName) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
      } else {
        Object[] args = new Object[]{repoName}  ;        
        uiApp.addMessage(new ApplicationMessage("UIRepositoryControl.msg.cannot-delete", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ; 
      }
      uiControl.reloadValue() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ;
    }
  }

  public static class AddRepositoryActionListener extends EventListener<UIRepositoryControl> {
    public void execute(Event<UIRepositoryControl> event) throws Exception {
      UIRepositoryControl uiControl = event.getSource() ;
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryFormContainer uiForm = uiPopupAction.activate(UIRepositoryFormContainer.class, 600) ;
      RepositoryService rService = uiControl.getApplicationComponent(RepositoryService.class) ;
      uiForm.refresh(true ,rService.getDefaultRepository().getConfiguration()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }


}
