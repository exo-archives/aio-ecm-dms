/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTypeForm;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.services.cms.taxonomy.TaxonomyTreeData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 9, 2009  
 */

@ComponentConfig( 
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITaxonomyTreeMainForm.AddPathActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeMainForm.AddPermissionActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaxonomyTreeMainForm.ChangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaxonomyTreeMainForm.ResetActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaxonomyTreeMainForm.SaveActionListener.class)
    }
)

public class UITaxonomyTreeMainForm extends UIForm {
  
  public static final String FIELD_NAME       = "TaxoTreeName";

  public static final String FIELD_WORKSPACE  = "TaxoTreeWorkspace";

  public static final String FIELD_HOMEPATH   = "TaxoTreeHomePath";

  public static final String FIELD_PERMISSION = "TaxoTreepermissions";
  
  public UITaxonomyTreeMainForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)
        .addValidator(MandatoryValidator.class));
    UIFormSelectBox uiSelectWorkspace = new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, null);
    addChild(uiSelectWorkspace);
    uiSelectWorkspace.setOnChange("Change");
    UIFormInputSetWithAction uiActionHomePath = new UIFormInputSetWithAction("TaxonomyTreeHomePath");
    uiActionHomePath.addUIFormInput(new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null)
        .setEditable(false));
    uiActionHomePath.setActionInfo(FIELD_HOMEPATH, new String[] { "AddPath" });
    addUIComponentInput(uiActionHomePath);
    UIFormInputSetWithAction uiActionPermission = new UIFormInputSetWithAction(
        "TaxonomyTreePermission");
    uiActionPermission.addUIFormInput(new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION,
        null).setEditable(false));
    uiActionPermission.addValidator(MandatoryValidator.class);
    uiActionPermission.setActionInfo(FIELD_PERMISSION, new String[] { "AddPermission" });
    addUIComponentInput(uiActionPermission);
    setActions(new String[] {"Save", "Reset"});
  }
  
  protected void update(TaxonomyTreeData taxonomyTree) throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    String[] wsNames = getApplicationComponent(RepositoryService.class)
                      .getRepository(repository).getWorkspaceNames();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>();
    for(String wsName : wsNames) {
      workspace.add(new SelectItemOption<String>(wsName,  wsName));
    }
    getUIFormSelectBox(FIELD_WORKSPACE).setOptions(workspace);
    if(taxonomyTree != null) {
      getUIStringInput(FIELD_NAME).setEditable(false);
      return;
    }
  }

  private String getRepository(){
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  int checkForm() throws Exception {
    UIFormSelectBox selectBox = getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
    UIFormInputBase inputHomePath = findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
    UIFormInputBase inputPermission = findComponentById(UITaxonomyTreeMainForm.FIELD_PERMISSION);
    if (inputHomePath == null)  return 1;
    if (inputPermission == null)  return 2;
    String systemWorkspace = getAncestorOfType(UITaxonomyManagerTrees.class)
        .getSystemWorkspaceName(getRepository());
    String workspace = selectBox.getValue();
    String homePath = inputHomePath.getValue() != null ? inputHomePath.getValue().toString() : "";
    String permission = inputPermission.getValue() != null ? inputPermission.getValue().toString() : "";
    if (homePath.length() == 0) {
      if (!systemWorkspace.equals(workspace)) {
        return 1;
      }
    }
    if (permission.length() == 0) {
        return 2;
    }
    return 0;
  }
  public void doSelect(String selectField, Object value) throws Exception {
      getUIStringInput(selectField).setValue(value.toString());
      /*UITaxonomyManagerTrees uiContainer = getAncestorOfType(UITaxonomyManagerTrees.class);
      for(UIComponent uiChild : uiContainer.getChildren()) {
        if(uiChild.getId().equals(UITaxonomyTreeContainer.POPUP_PERMISSION) || uiChild.getId().equals("JCRBrowser")) {
          UIPopupWindow uiPopup = uiContainer.getChildById(uiChild.getId());
          uiPopup.setRendered(false);
          uiPopup.setShow(false);
        }
      }*/
  }
  
  public static class AddPathActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      String workspace = 
        uiTaxonomyTreeForm.getUIFormSelectBox(UITaxonomyTreeMainForm.FIELD_WORKSPACE).getValue();
      uiTaxonomyManagerTrees.initPopupJCRBrowser(workspace, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class AddPermissionActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      String membership = uiTaxonomyTreeForm.getUIStringInput(UITaxonomyTreeMainForm.FIELD_PERMISSION).getValue();
      uiTaxonomyManagerTrees.initPopupPermission(membership);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ChangeActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeForm.getUIStringInput(FIELD_HOMEPATH).setValue("");
      uiTaxonomyTreeForm.getUIStringInput(FIELD_PERMISSION).setValue("");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class SaveActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = uiTaxonomyTreeMainForm.getParent();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      int validateCode = uiTaxonomyTreeMainForm.checkForm();
      if (validateCode == 1) {
        uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.homePath-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if (validateCode == 2) {
        uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.permission-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIActionTaxonomyManager uiActionTaxonomyManager = uiTaxonomyTreeContainer.getChild(UIActionTaxonomyManager.class);
      UIActionForm uiActionForm = uiActionTaxonomyManager.getChild(UIActionForm.class);
      UIFormSelectBox selectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      TaxonomyTreeData taxonomyTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      UIFormInputBase inputName = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_NAME);
      UIFormInputBase inputHomePath = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
      UIFormInputBase inputPermission = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_PERMISSION);
      taxonomyTreeData.setTaxoTreeName(inputName.getValue().toString());
      taxonomyTreeData.setTaxoTreeHomePath(inputHomePath.getValue().toString());
      taxonomyTreeData.setTaxoTreePermissions(inputPermission.getValue().toString());
      taxonomyTreeData.setTaxoTreeWorkspace(selectBox.getValue().toString());
      uiActionForm.setWorkspace(selectBox.getValue().toString());
      uiActionTaxonomyManager.setDefaultConfig();
      UIActionTypeForm uiActionTypeForm = uiActionTaxonomyManager.getChild(UIActionTypeForm.class);
      uiTaxonomyTreeContainer.viewStep(2);
      uiActionForm.createNewAction(null, uiActionTypeForm.defaultActionType_, true);
      uiActionTaxonomyManager.setRenderSibbling(UIActionTaxonomyManager.class);
      uiTaxonomyTreeContainer.setRenderedChild(UIActionTaxonomyManager.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ResetActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeForm.getUIStringInput(FIELD_NAME).setValue("");
      uiTaxonomyTreeForm.getUIStringInput(FIELD_HOMEPATH).setValue("");
      uiTaxonomyTreeForm.getUIStringInput(FIELD_PERMISSION).setValue("");
      uiTaxonomyTreeForm.update(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
}
