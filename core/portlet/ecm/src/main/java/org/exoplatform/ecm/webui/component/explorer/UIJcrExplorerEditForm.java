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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
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
 * 3 févr. 09  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.SelectTypeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.SelectDriveActionListener.class)
    }
)
public class UIJcrExplorerEditForm extends UIForm {
  private boolean flagSelectRender = false;
  
  public UIJcrExplorerEditForm() throws Exception {
    UIFormSelectBox repository = new UIFormSelectBox(UIJCRExplorerPortlet.REPOSITORY, UIJCRExplorerPortlet.REPOSITORY, getRepoOption());
    String repositoryValue = getPreference().getValue(UIJCRExplorerPortlet.REPOSITORY, "");
    repository.setValue(repositoryValue);
    repository.setEnable(false);
    addChild(repository);
    
    UIFormCheckBoxInput<Boolean> checkBoxCategory = new UIFormCheckBoxInput<Boolean>(UIJCRExplorerPortlet.CATEGORY_MANDATORY, null, null);
    checkBoxCategory.setChecked(Boolean.parseBoolean(getPreference().getValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, "")));
    checkBoxCategory.setEnable(false);
    addChild(checkBoxCategory);
    
    List<SelectItemOption<String>> listType = new ArrayList<SelectItemOption<String>>();
    String usecase = getPreference().getValue(UIJCRExplorerPortlet.USECASE, "");
    listType.add(new SelectItemOption<String>("Selection", "selection"));
    listType.add(new SelectItemOption<String>("Jailed", "jailed"));
    listType.add(new SelectItemOption<String>("Personal", "personal"));
    listType.add(new SelectItemOption<String>("Social", "social"));
    UIFormSelectBox typeSelectBox = new UIFormSelectBox(UIJCRExplorerPortlet.USECASE, UIJCRExplorerPortlet.USECASE, listType);
    typeSelectBox.setValue(usecase);
    typeSelectBox.setEnable(false);
    typeSelectBox.setOnChange("SelectType");
    addChild(typeSelectBox);
    
    UIFormInputSetWithAction driveNameInput = new UIFormInputSetWithAction("DriveNameInput");
    UIFormStringInput stringInputDrive = new UIFormStringInput(UIJCRExplorerPortlet.DRIVE_NAME, UIJCRExplorerPortlet.DRIVE_NAME, null);
    stringInputDrive.setValue(getPreference().getValue(UIJCRExplorerPortlet.DRIVE_NAME, ""));
    stringInputDrive.setEnable(false);
    driveNameInput.addUIFormInput(stringInputDrive);
    driveNameInput.setActionInfo(UIJCRExplorerPortlet.DRIVE_NAME, new String[] {"SelectDrive"});
    addUIComponentInput(driveNameInput);
    
    if (usecase.equals(UIJCRExplorerPortlet.JAILED)) {
      driveNameInput.setRendered(true);
      setFlagSelectRender(true);
    } else if(usecase.equals(UIJCRExplorerPortlet.PERSONAL)) {
      driveNameInput.setRendered(false);
      setFlagSelectRender(true);
    } else if (usecase.equals(UIJCRExplorerPortlet.SOCIAL)) {
      getGroupId();
      setFlagSelectRender(true);
      driveNameInput.setRendered(false);
    } else if (usecase.equals(UIJCRExplorerPortlet.SELECTION)) {
      driveNameInput.setRendered(false);
    } else {
      driveNameInput.setRendered(false);
    }
    setActions(new String[] {"Edit"});
  }
  
  public String getGroupId() {
    try {
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      String requestUrl = pcontext.getRequestURI();
      String portalUrl = pcontext.getPortalURI();
      String spaceUrl = requestUrl.replace(portalUrl,"");
      if (spaceUrl.contains("/")) spaceUrl = spaceUrl.split("/")[0];
      Object space = getSpaceByUrl(spaceUrl);
      Class clazzSpace = Class.forName("org.exoplatform.social.space.Space");
      Method[] methods = clazzSpace.getDeclaredMethods();
      for (Method m : methods) {
        if (m.getName().trim().equals("getGroupId")) {
          return (String) m.invoke(space);
        }
      }
    } catch (Exception e) {
      UIFormSelectBox typeSelectBox = getChildById(UIJCRExplorerPortlet.USECASE);
      typeSelectBox.setValue(UIJCRExplorerPortlet.SELECTION);
      UIFormInputSetWithAction driveNameInput = getChildById("DriveNameInput");
      UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
      stringInputDrive.setValue("");
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      uiApp.addMessage(new ApplicationMessage("UIJcrExplorerEditForm.msg.not-have-social", null));
    }
    return null;
  }
  
  public Object getSpaceByUrl(String url) throws Exception {
    try {
      Class clazz = Class.forName("org.exoplatform.social.space.SpaceService");
      Object obj = PortalContainer.getInstance().getComponentInstanceOfType(clazz);
      Method[] methods = clazz.getDeclaredMethods();
      for (Method m : methods) {
        if (m.getName().trim().equals("getSpaceByUrl")) {
          return m.invoke(obj, url);
        }
      }
    } catch(Exception e) {
      return null;
    }
    return null;
  }
  
  public boolean isFlagSelectRender() {
    return flagSelectRender;
  }

  public void setFlagSelectRender(boolean flagSelectRender) {
    this.flagSelectRender = flagSelectRender;
  }
  
  public void setEditable(boolean isEditable) {
    UIFormSelectBox repository = getChild(UIFormSelectBox.class);
    repository.setEnable(isEditable);
    UIFormCheckBoxInput<Boolean> checkBoxCategory = getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
    checkBoxCategory.setEnable(isEditable);
    UIFormSelectBox typeSelectBox = getChildById(UIJCRExplorerPortlet.USECASE);
    typeSelectBox.setEnable(isEditable);
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

  private String getPublicUserDrive(List<DriveData> personalDrives) {
    for(DriveData userDrive : personalDrives) {
      if(userDrive.getName().equalsIgnoreCase("public")) {
        return userDrive.getName();
      }
    }
    return null;
  }
  
  public static class EditActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      UIFormInputSetWithAction driveNameInput = uiForm.getChildById("DriveNameInput");
      driveNameInput.setActionInfo(UIJCRExplorerPortlet.DRIVE_NAME, new String[] {"SelectDrive"});
      uiForm.setEditable(true);
      uiForm.setActions(new String[] {"Save", "Cancel"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }  
  
  public static class CancelActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      PortletPreferences pref = uiForm.getPreference();
      UIFormSelectBox repository = uiForm.getChildById(UIJCRExplorerPortlet.REPOSITORY);
      repository.setValue(pref.getValue(UIJCRExplorerPortlet.REPOSITORY, ""));
      UIFormCheckBoxInput<Boolean> checkBoxCategory = uiForm.getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
      checkBoxCategory.setChecked(Boolean.parseBoolean(pref.getValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, "")));
      UIFormSelectBox typeSelectBox = uiForm.getChildById(UIJCRExplorerPortlet.USECASE);
      typeSelectBox.setValue(pref.getValue(UIJCRExplorerPortlet.USECASE, ""));
      UIFormInputSetWithAction driveNameInput = uiForm.getChildById("DriveNameInput");
      UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
      stringInputDrive.setValue(pref.getValue(UIJCRExplorerPortlet.DRIVE_NAME, ""));
      if (pref.getValue(UIJCRExplorerPortlet.USECASE, "").equals(UIJCRExplorerPortlet.JAILED)) {
        driveNameInput.setRendered(true);
      } else {
        driveNameInput.setRendered(false);
      }
      uiForm.setEditable(false);
      uiForm.setActions(new String[] {"Edit"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  public static class SelectTypeActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      UIJCRExplorerPortlet uiJExplorerPortlet = uiForm.getAncestorOfType(UIJCRExplorerPortlet.class);
      UIJcrExplorerContainer uiContainer = uiJExplorerPortlet.getChild(UIJcrExplorerContainer.class);
      uiForm.setEditable(true);
      UIFormSelectBox typeSelectBox = uiForm.getChildById(UIJCRExplorerPortlet.USECASE);
      UIFormInputSetWithAction driveNameInput = uiForm.getChildById("DriveNameInput");
      
      if (typeSelectBox.getValue().equals(UIJCRExplorerPortlet.JAILED)) {
        driveNameInput.setRendered(true);
      } else if (typeSelectBox.getValue().equals(UIJCRExplorerPortlet.SOCIAL)) {
        String groupId = uiForm.getGroupId();
        driveNameInput.setRendered(false);
        if (groupId == null || groupId.equals("")) {
          UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
          stringInputDrive.setValue("");
        } else {
          groupId = groupId.replaceAll("/",".");
          UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
          stringInputDrive.setValue(groupId);
        }
      } else if(typeSelectBox.getValue().equals(UIJCRExplorerPortlet.PERSONAL)) {
        driveNameInput.setRendered(false);
        List<DriveData> personalDrives = 
          uiContainer.personalDrives(uiContainer.getDrives(uiForm.getPreference())); 
        String personalPublicDrive = uiForm.getPublicUserDrive(personalDrives);
        UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
        stringInputDrive.setValue(personalPublicDrive);
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIJcrExplorerEditForm.msg.personal-usecase", null));
      } else {
        driveNameInput.setRendered(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  public static class SaveActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      PortletPreferences pref = uiForm.getPreference();
      UIFormSelectBox repository = uiForm.getChildById(UIJCRExplorerPortlet.REPOSITORY);
      UIFormCheckBoxInput<Boolean> checkBoxCategory = uiForm.getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
      UIFormSelectBox typeSelectBox = uiForm.getChildById(UIJCRExplorerPortlet.USECASE);
      UIFormInputSetWithAction driveNameInput = uiForm.getChildById("DriveNameInput");
      UIFormStringInput stringInputDrive = driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME);
      String driveName = stringInputDrive.getValue();
      String useCase = typeSelectBox.getValue();
      if (useCase.equals(UIJCRExplorerPortlet.SELECTION)) {
        driveName = "";
      } else {
        uiForm.setFlagSelectRender(true);
      }
      pref.setValue(UIJCRExplorerPortlet.REPOSITORY, repository.getValue());
      pref.setValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, String.valueOf(checkBoxCategory.isChecked()));
      pref.setValue(UIJCRExplorerPortlet.USECASE, useCase);
      pref.setValue(UIJCRExplorerPortlet.DRIVE_NAME, driveName);
      pref.store();
      uiForm.setEditable(false);
      uiForm.setActions(new String[] {"Edit"});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  public static class SelectDriveActionListener extends EventListener<UIJcrExplorerEditForm>{
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      UIJcrExplorerEditContainer editContainer = uiForm.getParent();
      UIPopupWindow popupWindow = editContainer.initPopup("PopUpSelectDrive");
      
      UIDriveSelector driveSelector = editContainer.createUIComponent(UIDriveSelector.class, null, null);
      driveSelector.updateGrid();
      popupWindow.setUIComponent(driveSelector);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }
}
