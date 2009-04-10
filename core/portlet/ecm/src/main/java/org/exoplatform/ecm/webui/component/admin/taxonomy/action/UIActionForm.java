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
package org.exoplatform.ecm.webui.component.admin.taxonomy.action;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManagerTrees;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeContainer;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeList;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeMainForm;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 5, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIActionForm.SaveActionListener.class),
      @EventConfig(listeners = UIActionForm.BackActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIDialogForm.OnchangeActionListener.class, phase=Phase.DECODE)
    }
)
public class UIActionForm extends UIDialogForm implements UISelectable {
  
  private String              nodeTypeName_ = null;

  private boolean             isAddNew_;

  private String              scriptPath_   = null;

  private String              rootPath_     = null;

  private static final String EXO_ACTIONS   = "exo:actions".intern();
  
  public UIActionForm() throws Exception {
    setActions(new String[] { "Save", "Back" });
  }
  
  public void createNewAction(Node parentNode, String actionType, boolean isAddNew) throws Exception {
    reset();
    nodeTypeName_ = actionType;
    isAddNew_ = isAddNew;
    componentSelectors.clear();
    properties.clear();
    getChildren().clear();
  }
  
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true;
    getUIStringInput(selectField).setValue(value.toString());
  }
  
  public String getCurrentPath() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentPath();
  }
  
  public String getRepositoryName() {
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  public String getWorkspace() {
    UITaxonomyTreeContainer uiTaxonomyTreeContainer = getAncestorOfType(UITaxonomyTreeContainer.class);
    UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeMainForm.class);
    UIFormSelectBox uiSelectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
    return uiSelectBox.getValue().toString();
  }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return new JCRResourceResolver(getRepositoryName(), getWorkspace(), "exo:templateFile");
  }

  public String getTemplate() { return getDialogPath() ; }

  public String getDialogPath() {
    repositoryName = getRepositoryName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    String dialogPath = null;
    if (nodeTypeName_ != null) {
      try {
        dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName_, userName, getRepositoryName());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return dialogPath;    
  }
  
  private void setPath(String scriptPath) {
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = getWorkspace() + ":" + scriptPath ;
    }
    scriptPath_ = scriptPath ; 
  }
  
  public String getTenmplateNodeType() {
    return nodeTypeName_;
  }

  public String getPath() {
    return scriptPath_;
  }

  public void setRootPath(String rootPath) {
    rootPath_ = rootPath;
  }
  
  public String getRootPath() {
    return rootPath_;
  }

  public void onchange(Event event) throws Exception {
    UITaxonomyTreeContainer uiTaxonomyTreeForm = getAncestorOfType(UITaxonomyTreeContainer.class);
    uiTaxonomyTreeForm.setRenderedChild(UIActionTaxonomyManager.class);
    event.getRequestContext().addUIComponentToUpdateByAjax(getParent());
    event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyTreeForm);
  }
  
  
  public static class SaveActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiActionForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = uiActionForm.getAncestorOfType(UITaxonomyTreeContainer.class);
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeMainForm.class);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiActionForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      UIFormInputBase inputName = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_NAME);
      UIFormSelectBox selectBox = uiTaxonomyTreeMainForm.getChildById(UITaxonomyTreeMainForm.FIELD_WORKSPACE);
      UIFormInputBase inputHomePath = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_HOMEPATH);
      UIFormInputBase inputPermission = uiTaxonomyTreeMainForm.findComponentById(UITaxonomyTreeMainForm.FIELD_PERMISSION);
      String repository = uiActionForm.getRepositoryName();
      String systemWorkspace = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class)
          .getSystemWorkspaceName(uiActionForm.getRepositoryName());
      
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
      
      ActionServiceContainer actionServiceContainer = uiActionForm.getApplicationComponent(ActionServiceContainer.class);
      Map<String, JcrInputProperty> sortedInputs = DialogFormUtil.prepareMap(uiActionForm
          .getChildren(), uiActionForm.getInputProperties());
      Session  session = uiActionForm.getSesssion();
      Node currentNode = (Node)session.getItem(homePath + "/" + name);
      if (!PermissionUtil.canAddNode(currentNode) || !PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.no-permission-add", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (currentNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(currentNode);
        if (lockToken != null)
          session.addLockToken(lockToken);
      }
      try {
        JcrInputProperty rootProp = sortedInputs.get("/node");
        if (rootProp == null) {
          rootProp = new JcrInputProperty();
          rootProp.setJcrPath("/node");
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
          sortedInputs.put("/node", rootProp);
        } else {
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
        }
        String actionName = (String) (sortedInputs.get("/node/exo:name")).getValue();
        if (currentNode.hasNode(EXO_ACTIONS)) {
          if (currentNode.getNode(EXO_ACTIONS).hasNode(actionName)) {
            Object[] args = { actionName };
            uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.existed-action", args,
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
        if (currentNode.isNew()) {
          String[] args = { currentNode.getPath() };
          uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add-action", args));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        actionServiceContainer.addAction(currentNode, repository, uiActionForm.nodeTypeName_,
            sortedInputs);
        uiActionForm.setIsOnchange(false);
        session.save();
        uiActionForm.createNewAction(currentNode, uiActionForm.nodeTypeName_, true);
        uiActionForm.reset();
      } catch (RepositoryException repo) {
        String key = "UIActionForm.msg.repository-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (NumberFormatException nume) {
        String key = "UIActionForm.msg.numberformat-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null));
        return;
      }
      UIPopupWindow uiPopup = uiTaxonomyTreeContainer.getParent();
      uiTaxonomyManagerTrees.removeChildById(UITaxonomyTreeList.ST_ADD);
      uiTaxonomyManagerTrees.removeChildById(UITaxonomyTreeList.ST_EDIT);
      uiTaxonomyManagerTrees.update();
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class BackActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource().getAncestorOfType(UITaxonomyTreeContainer.class);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeContainer.viewStep(1);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  } 
}
