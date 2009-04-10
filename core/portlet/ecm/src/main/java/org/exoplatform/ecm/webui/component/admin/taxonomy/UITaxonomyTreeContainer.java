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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.drives.UIDriveInputSet;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTypeForm;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 3, 2009  
 */

@ComponentConfig(
    template =  "app:/groovy/webui/component/admin/taxonomy/UITaxonomyTreeWizard.gtmpl",
    events = {
      @EventConfig(listeners = UITaxonomyTreeContainer.SaveActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.RefreshActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.CloseActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep1ActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep2ActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep3ActionListener.class)
    }
)

public class UITaxonomyTreeContainer extends UIContainer implements UISelectable {

  private boolean isAddNew_ = true;
  
  private int                wizardMaxStep_   = 3;

  private int                selectedStep_    = 1;

  private int                currentStep_     = 0;
  
  
  public static final String POPUP_PERMISSION = "PopupTaxonomyTreePermission";

  private String[]           actions_         = { "Close"};
  
  public UITaxonomyTreeContainer() throws Exception {
    UITaxonomyTreeMainForm uiTaxonomyTreeMain = addChild(UITaxonomyTreeMainForm.class, null, "TaxonomyTreeMain");
    UIFormSelectBox selectBox = uiTaxonomyTreeMain.findComponentById(UITaxonomyTreeInputSet.FIELD_WORKSPACE);
    selectBox.setOnChange("Change");
    addChild(UIActionTaxonomyManager.class, null, null).setRendered(false);
    if (!isAddNew_) {
      addChild(UITaxonomyTreeCreateChild.class, null, null).setRendered(false);
    }
  }
  
  public String[] getActions() {return actions_ ;}
  
  public void setCurrentSep(int step) {
    currentStep_ = step;
  }

  public int getCurrentStep() {
    return currentStep_;
  }

  public void setSelectedStep(int step) {
    selectedStep_ = step;
  }

  public int getSelectedStep() {
    return selectedStep_;
  }

  public int getMaxStep() {
    return wizardMaxStep_;
  }
  
  public int getNumberSteps() {
    return wizardMaxStep_;
  }
  
  public void viewStep(int step) {   
    selectedStep_ = step ;
    currentStep_ = step - 1 ;    
    List<UIComponent> children = getChildren(); 
    for(int i=0; i<children.size(); i++){
      if(i == getCurrentStep()) {
        children.get(i).setRendered(true);
      } else {
        children.get(i).setRendered(false);
      }
    }
  }
  
  public void refresh() throws Exception {
    UIActionTaxonomyManager uiActionTaxonomyManager = getChild(UIActionTaxonomyManager.class);
    uiActionTaxonomyManager.getChild(UIActionTypeForm.class).update();
    if (isAddNew_) {
      findFirstComponentOfType(UITaxonomyTreeMainForm.class).update(null);
    }
  }
  
  private UIFormStringInput getFormInputById(String id) {
    return (UIFormStringInput)findComponentById(id);
  }
  
  private String getRepository(){
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  private Session getSession(String workspace) throws RepositoryException, RepositoryConfigurationException  {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class);
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    return rservice.getRepository(repository).getSystemSession(workspace);
  }
  
  public void doSelect(String selectField, Object value) throws Exception {
    getFormInputById(selectField).setValue(value.toString());
    UITaxonomyManagerTrees uiContainer = getAncestorOfType(UITaxonomyManagerTrees.class);
    for(UIComponent uiChild : uiContainer.getChildren()) {
      if(uiChild.getId().equals(UITaxonomyTreeContainer.POPUP_PERMISSION) || uiChild.getId().equals("JCRBrowser")) {
        UIPopupWindow uiPopup = uiContainer.getChildById(uiChild.getId());
        uiPopup.setRendered(false);
        uiPopup.setShow(false);
      }
    }
  }
  
  public void addTaxonomyTree(String name, String workspace, String homePath, String permission)
      throws RepositoryException, RepositoryConfigurationException, TaxonomyAlreadyExistsException {
    Session session = getSession(workspace);
    Node homeNode = (Node)session.getItem(homePath);
    Node taxonomyTreeNode = homeNode.addNode(name, Utils.EXO_TAXANOMY);
    String [] permissionType= {PermissionType.READ};
    if (taxonomyTreeNode.canAddMixin("exo:privilegeable")) {
      taxonomyTreeNode.addMixin("exo:privilegeable");
      ((ExtendedNode)taxonomyTreeNode).setPermission(permission, permissionType);
    }
    homeNode.save();
    session.save();
    session.logout();
    TaxonomyService taxonomyService_ = getApplicationComponent(TaxonomyService.class);
    taxonomyService_.addTaxonomyTree(taxonomyTreeNode);
  }
  
  public static class SaveActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      UITaxonomyTreeMainForm  uiTaxonomyTreeMain = uiTaxonomyTreeContainer.getChildById("TaxonomyTreeMain");
      UIFormInputBase inputName = uiTaxonomyTreeMain.getChildById(UITaxonomyTreeMainForm.FIELD_NAME);
      UIFormSelectBox selectBox = uiTaxonomyTreeMain.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      UIFormInputBase inputHomePath = uiTaxonomyTreeMain.getChildById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
      UIFormInputBase inputPermission = uiTaxonomyTreeMain.getChildById(UITaxonomyTreeMainForm.FIELD_PERMISSION);
      String systemWorkspace = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class)
          .getSystemWorkspaceName(uiTaxonomyTreeContainer.getRepository());
      
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      String name = inputName.getValue().toString();
      String workspace = selectBox.getValue();
      String homePath = inputHomePath.getValue() != null ? inputHomePath.getValue().toString() : "";
      String permission = inputPermission.getValue().toString();
      if (homePath == null || homePath.length() == 0) {
        if (systemWorkspace.equals(workspace)) {
          NodeHierarchyCreator nodeHierarchyCreator = uiTaxonomyTreeContainer
              .getApplicationComponent(NodeHierarchyCreator.class);
          homePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
        } else {
          uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.homePath-emty", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }        
      
      try {
        uiTaxonomyTreeContainer.addTaxonomyTree(name, workspace, homePath, permission);
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.path-invalid", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (TaxonomyAlreadyExistsException e) {
        uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.taxonomy-existed", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupWindow uiPopup = uiTaxonomyTreeContainer.getParent();
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer
          .getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyManagerTrees.update();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class RefreshActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      event.getSource().refresh();
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
  
  public static class CloseActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      UIPopupWindow uiPopup = uiTaxonomyTreeContainer.getParent();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyManagerTrees.removeChildById(UITaxonomyTreeList.ST_ADD);
      uiTaxonomyManagerTrees.removeChildById(UITaxonomyTreeList.ST_EDIT);
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class AddPathActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      UITaxonomyTreeInputSet taxonomyTreeInputSet = uiTaxonomyTreeForm.getChild(UITaxonomyTreeInputSet.class);
      String workspace = 
        taxonomyTreeInputSet.getUIFormSelectBox(UITaxonomyTreeInputSet.FIELD_WORKSPACE).getValue();
      uiTaxonomyManagerTrees.initPopupJCRBrowser(workspace, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class AddPermissionActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      String membership = uiTaxonomyTreeForm.getFormInputById(UITaxonomyTreeInputSet.FIELD_PERMISSION).getValue();
      uiTaxonomyManagerTrees.initPopupPermission(membership);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ChangeActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeForm = event.getSource();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      String treeName = uiTaxonomyTreeForm.getFormInputById(UIDriveInputSet.FIELD_NAME).getValue();
      String repository = uiTaxonomyTreeForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
      String selectedWorkspace = uiTaxonomyTreeForm.getFormInputById(UITaxonomyTreeInputSet.FIELD_WORKSPACE).getValue();
      UITaxonomyTreeInputSet uiTreeInputSet = uiTaxonomyTreeForm.getChild(UITaxonomyTreeInputSet.class);
      RepositoryService repositoryService = 
        uiTaxonomyTreeForm.getApplicationComponent(RepositoryService.class);
      List<WorkspaceEntry> wsEntries = 
        repositoryService.getRepository(repository).getConfiguration().getWorkspaceEntries();
      String wsInitRootNodeType = null;
      List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>();
      if(!uiTaxonomyTreeForm.isAddNew_) {
      }
    }
  }
  
  public static class ViewStep1ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      //uiTaxonomyTreeContainer.setRenderedChild(UITaxonomyTreeMainForm.class);
      uiTaxonomyTreeContainer.viewStep(1);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewStep2ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeMainForm.class);
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
      UIFormSelectBox selectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      UIActionForm uiActionForm = uiTaxonomyTreeContainer.findFirstComponentOfType(UIActionForm.class);
      uiActionForm.setWorkspace(selectBox.getValue().toString());
      uiTaxonomyTreeContainer.viewStep(2);
      UIActionTaxonomyManager uiActionTaxonomyManager = uiTaxonomyTreeContainer.getChild(UIActionTaxonomyManager.class);
      uiActionTaxonomyManager.setDefaultConfig();
      UIActionTypeForm uiActionTypeForm = uiActionTaxonomyManager.getChild(UIActionTypeForm.class);
      uiActionForm.createNewAction(null, uiActionTypeForm.defaultActionType_, true);
      uiActionTaxonomyManager.setRenderSibbling(UIActionTaxonomyManager.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewStep3ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      uiTaxonomyTreeContainer.viewStep(3);
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeCreateChild.class);
      UIFormSelectBox uiSelectBox = uiTaxonomyTreeContainer.findComponentById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      uiTaxonomyTreeCreateChild.setWorkspace(uiSelectBox.getValue().toString());
      System.out.println("\n\n uri = " + uri);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
}
