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
import org.exoplatform.ecm.webui.component.admin.taxonomy.tree.info.UIPermissionTreeManager;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.TaxonomyTreeData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
      @EventConfig(listeners = UITaxonomyTreeMainForm.ChangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaxonomyTreeMainForm.ResetActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaxonomyTreeMainForm.AddPathActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeMainForm.ViewPermissionActionListener.class)
    }
)

public class UITaxonomyTreeMainForm extends UIForm {
  
  public static final String FIELD_NAME       = "TaxoTreeName";

  public static final String FIELD_WORKSPACE  = "TaxoTreeWorkspace";

  public static final String FIELD_HOMEPATH   = "TaxoTreeHomePath";

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
    setActions(new String[] {"ViewPermission", "Reset"});
  }
  
  public void update(TaxonomyTreeData taxonomyTree) throws Exception {
    String repository = getRepository();
    String[] wsNames = getApplicationComponent(RepositoryService.class)
                      .getRepository(repository).getWorkspaceNames();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>();
    String systemWorkspace = getAncestorOfType(UITaxonomyManagerTrees.class).getSystemWorkspaceName(repository);
    String dmsSystemWorkspace = getAncestorOfType(UITaxonomyManagerTrees.class).getDmsSystemWorkspaceName(repository);
    
    for(String wsName : wsNames) {
      if (!systemWorkspace.equals(wsName))
        workspace.add(new SelectItemOption<String>(wsName, wsName));
    }
    if (taxonomyTree == null) taxonomyTree = new TaxonomyTreeData();
    UIFormSelectBox uiSelectBox = getUIFormSelectBox(FIELD_WORKSPACE).setOptions(workspace);
    if (taxonomyTree.getTaxoTreeWorkspace() == null) {
      taxonomyTree.setTaxoTreeWorkspace(dmsSystemWorkspace);
    }
    uiSelectBox.setValue(taxonomyTree.getTaxoTreeWorkspace());
    UIFormInputBase<String> inputName = findComponentById(UITaxonomyTreeMainForm.FIELD_NAME);
    UIFormInputBase<String> inputHomePath = findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
    String treeName = taxonomyTree.getTaxoTreeName();
    inputName.setValue(treeName);
    inputHomePath.setValue(taxonomyTree.getTaxoTreeHomePath());
    if(treeName != null && treeName.length() > 0) {
      getUIStringInput(FIELD_NAME).setEditable(false);
    }
  }

  private String getRepository(){
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  int checkForm() throws Exception {
    UIFormStringInput input = getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
    if (input == null || input.getValue() == null || input.getValue().length() == 0) {
      return 1;
    }
    UIFormSelectBox selectBox = getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
    UIFormInputBase inputHomePath = findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
    String homePath = "";
    if (inputHomePath != null && inputHomePath.getValue() != null) {
      homePath = inputHomePath.getValue().toString();
    }
    String dmsSysWorkspace = getAncestorOfType(UITaxonomyManagerTrees.class).getDmsSystemWorkspaceName(getRepository());
    String workspace = selectBox.getValue();
    if (homePath.length() == 0) {
      if (!dmsSysWorkspace.equals(workspace)) {
        return 2;
      }
    }
    return 0;
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
  
  public static class ChangeActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = uiTaxonomyTreeMainForm.getParent();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeMainForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeMainForm.getUIStringInput(FIELD_HOMEPATH).setValue("");
      TaxonomyTreeData taxonomyTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      UIFormSelectBox selectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      taxonomyTreeData.setTaxoTreeHomePath("");
      taxonomyTreeData.setTaxoTreeWorkspace(selectBox.getValue());
      
      String dmsSysWorkspace = uiTaxonomyManagerTrees.getDmsSystemWorkspaceName(uiTaxonomyTreeMainForm.getRepository());
      UIFormInputSetWithAction uiActionHomePath = uiTaxonomyTreeMainForm.getChildById("TaxonomyTreeHomePath");
      uiActionHomePath.removeChildById(FIELD_HOMEPATH);
      if (!selectBox.getValue().equals(dmsSysWorkspace)) {
        uiActionHomePath.addUIFormInput(new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null).
            addValidator(MandatoryValidator.class).setEditable(false));
      } else {
        uiActionHomePath.addUIFormInput(new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null).
            setEditable(false));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewPermissionActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = uiTaxonomyTreeMainForm.getParent();
      TaxonomyTreeData taxonomyTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      int validateCode = uiTaxonomyTreeMainForm.checkForm();
      if (validateCode == 1) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.name-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if (validateCode == 2) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.homepath-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      UIFormInputBase inputName = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_NAME);
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", 
          "'", "#", ";", "}", "{", "/", "|", "\""};
      String name = inputName.getValue().toString().trim();
      
      if (!Utils.isNameValid(name,arrFilterChar)) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.name-not-allowed", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      TaxonomyService taxonomyService = uiTaxonomyTreeMainForm.getApplicationComponent(TaxonomyService.class);
      if (taxonomyService.hasTaxonomyTree(uiTaxonomyTreeMainForm.getRepository(), name) && !taxonomyTreeData.isEdit()) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.taxonomytree-existed", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      UIFormSelectBox selectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      UIFormInputBase inputHomePath = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
      String homePath = "";
      if (inputHomePath != null && inputHomePath.getValue() != null) homePath =inputHomePath.getValue().toString();
      NodeHierarchyCreator nodeHierarchyCreator = uiTaxonomyTreeMainForm.getApplicationComponent(NodeHierarchyCreator.class);
      String treeDefinitionPath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_DEFINITION_PATH);
      if ((homePath.length() > 0) && (treeDefinitionPath != null) 
          && (treeDefinitionPath.length() > 0) && (homePath.equals(treeDefinitionPath))) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.no-right-target-path", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPermissionTreeManager uiPermissionManage = uiTaxonomyTreeContainer.getChild(UIPermissionTreeManager.class);
      taxonomyTreeData.setTaxoTreeName(name);
      taxonomyTreeData.setTaxoTreeHomePath(homePath.trim());
      taxonomyTreeData.setTaxoTreeWorkspace(selectBox.getValue());
      uiTaxonomyTreeContainer.viewStep(2);
      uiPermissionManage.update();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ResetActionListener extends EventListener<UITaxonomyTreeMainForm> {
    public void execute(Event<UITaxonomyTreeMainForm> event) throws Exception {
      UITaxonomyTreeMainForm uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeForm.getUIStringInput(FIELD_NAME).setValue("");
      uiTaxonomyTreeForm.getUIStringInput(FIELD_HOMEPATH).setValue("");
      uiTaxonomyTreeForm.update(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
}
