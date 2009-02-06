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
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * 3 févr. 09  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.ChangeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.CancelActionListener.class)
    }
)
public class UIJcrExplorerEditForm extends UIForm {
  public UIJcrExplorerEditForm() throws Exception {
    UIFormSelectBox repository = new UIFormSelectBox(UIJCRExplorerPortlet.REPOSITORY, UIJCRExplorerPortlet.REPOSITORY, getRepoOption());
    String repositoryValue = getPreference().getValue(UIJCRExplorerPortlet.REPOSITORY, "");
    repository.setDefaultValue(repositoryValue);
    addChild(repository);
    
    UIFormCheckBoxInput<Boolean> checkBoxCategory = new UIFormCheckBoxInput<Boolean>(UIJCRExplorerPortlet.CATEGORY_MANDATORY, null, null);
    checkBoxCategory.setChecked(Boolean.parseBoolean(getPreference().getValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, "")));
    addChild(checkBoxCategory);
    
    UIFormCheckBoxInput<Boolean> checkBoxDirectlyDrive = new UIFormCheckBoxInput<Boolean>(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE, null, null);
    checkBoxDirectlyDrive.setOnChange("Change");
    checkBoxDirectlyDrive.setChecked(Boolean.parseBoolean(getPreference().getValue(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE, "")));
    
    addChild(checkBoxDirectlyDrive);
    
    UIFormStringInput stringInputDrive = new UIFormStringInput(UIJCRExplorerPortlet.DRIVE_NAME, UIJCRExplorerPortlet.DRIVE_NAME, null);
    stringInputDrive.setValue(getPreference().getValue(UIJCRExplorerPortlet.DRIVE_NAME, ""));
    addUIFormInput(stringInputDrive);
    setActions(new String[] {"Edit"});
  }
  
  public void setEditable(boolean isEditable) {
    UIFormSelectBox repository = getChild(UIFormSelectBox.class);
    repository.setEnable(isEditable);
    UIFormCheckBoxInput<Boolean> checkBoxCategory = getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
    checkBoxCategory.setEnable(isEditable);
    UIFormCheckBoxInput<Boolean> checkBoxDirectlyDrive = getChildById(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE);
    checkBoxDirectlyDrive.setEnable(isEditable);
    UIFormStringInput stringInputDrive = getChildById(UIJCRExplorerPortlet.DRIVE_NAME);
    if (isEditable && Boolean.parseBoolean(getPreference().getValue(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE, ""))) {
      stringInputDrive.setEnable(true);
    } else {
      stringInputDrive.setEnable(false);
    }
  } 
  
  private PortletPreferences getPreference() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    return pcontext.getRequest().getPreferences();
  }
  
  private List<SelectItemOption<String>> getRepoOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    for (RepositoryEntry repo : repositoryService.getConfig().getRepositoryConfigurations()) {
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName()));
    }
    return options;
  }
  
  public static class ChangeActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      boolean value = uiForm.getUIFormCheckBoxInput(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE).isChecked();
      UIFormStringInput stringInputDrive = uiForm.getChildById(UIJCRExplorerPortlet.DRIVE_NAME);
      stringInputDrive.setEnable(value);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }  
  
  public static class EditActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      uiForm.setEditable(true);
      uiForm.setActions(new String[] {"Save", "Cancel"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }  
  
  public static class CancelActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      uiForm.setEditable(false);
      uiForm.setActions(new String[] {"Edit"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  public static class SaveActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      PortletPreferences pref = uiForm.getPreference();
      UIFormSelectBox repository = uiForm.getChildById(UIJCRExplorerPortlet.REPOSITORY);
      UIFormCheckBoxInput<Boolean> checkBoxCategory = uiForm.getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
      UIFormCheckBoxInput<Boolean> checkBoxDirectlyDrive = uiForm.getChildById(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE);
      UIFormStringInput stringInputDrive = uiForm.getChildById(UIJCRExplorerPortlet.DRIVE_NAME);
      
      pref.setValue(UIJCRExplorerPortlet.REPOSITORY, repository.getValue());
      pref.setValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, String.valueOf(checkBoxCategory.isChecked()));
      pref.setValue(UIJCRExplorerPortlet.ISDIRECTLY_DRIVE, String.valueOf(checkBoxDirectlyDrive.isChecked()));
      pref.setValue(UIJCRExplorerPortlet.DRIVE_NAME, stringInputDrive.getValue());
      pref.store();
      uiForm.setEditable(false);
      uiForm.setActions(new String[] {"Edit"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

}
