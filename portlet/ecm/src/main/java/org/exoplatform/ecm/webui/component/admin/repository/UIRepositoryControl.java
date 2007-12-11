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
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIRepositoryControl.RemoveRepositoryActionListener.class),
        @EventConfig(listeners = UIRepositoryControl.AddRepositoryActionListener.class)
    }
)

public class UIRepositoryControl extends UIContainer {

  public UIRepositoryControl() throws Exception{
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences pref = pcontext.getRequest().getPreferences() ;
    String repository = pref.getValue(Utils.REPOSITORY, "") ;
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    UIRepositorySelectForm uiSelectForm = createUIComponent(UIRepositorySelectForm.class, null, null) ;
    try{
      rservice.getRepository(repository) ;
      uiSelectForm.setOptionValue(getRepoItem(true, rservice)) ;
      uiSelectForm.setSelectedValue(repository) ;      
    }catch(Exception e) {
      uiSelectForm.setOptionValue(getRepoItem(false, rservice)) ;
      uiSelectForm.setSelectedValue("") ;
    }
    uiSelectForm.setActionEvent() ;
    addChild(uiSelectForm) ;
  }

  protected List<SelectItemOption<String>> getRepoItem(boolean isExists, RepositoryService rservice ){
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    if(!isExists){
      options.add(new SelectItemOption<String>("", "")) ;
    }
    for(Object obj : rservice.getConfig().getRepositoryConfigurations()) { 
      RepositoryEntry repo  = (RepositoryEntry)obj ;
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
    }
    return options ;
  }
  protected String getSelectedRepo() {
    return getChild(UIRepositorySelectForm.class).getSelectedValue() ;
  }
  protected boolean isDefaultRepo(String repoName) {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    return rservice.getConfig().getDefaultRepositoryName().equals(repoName);
  }
  protected void reloadValue(boolean isExists, RepositoryService rservice){
    getChild(UIRepositorySelectForm.class).setOptionValue(getRepoItem(isExists, rservice)) ;
    if(isExists) {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      PortletPreferences pref = pcontext.getRequest().getPreferences() ;
      String repository = pref.getValue(Utils.REPOSITORY, "") ;
      getChild(UIRepositorySelectForm.class).setSelectedValue(repository) ;
    }else {
      getChild(UIRepositorySelectForm.class).setSelectedValue("") ;
    }
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
      UIECMAdminPortlet ecmPortlet = uiControl.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIPopupAction uiPopupAction = ecmPortlet.getChild(UIPopupAction.class) ;
      UIRepositoryList uiList = uiPopupAction.activate(UIRepositoryList.class, 600) ;
      uiList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
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