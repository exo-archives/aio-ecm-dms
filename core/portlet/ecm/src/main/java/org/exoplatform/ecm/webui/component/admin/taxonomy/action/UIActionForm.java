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
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManagerTrees;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeContainer;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeCreateChild;
import org.exoplatform.ecm.webui.component.admin.taxonomy.tree.info.UIPermissionTreeForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.tree.info.UIPermissionTreeInfo;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.TaxonomyTreeData;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyNodeAlreadyExistsException;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;

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
      @EventConfig(listeners = UIDialogForm.OnchangeActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIActionForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)

public class UIActionForm extends UIDialogForm implements UISelectable {
  
  private String              parentPath_ = "";
  
  private String              nodeTypeName_ = null;

  private boolean             isAddNew_;

  private String              scriptPath_   = null;

  private String              rootPath_     = null;
  
  public static final String POPUP_COMPONENT = "PopupComponent";

  public static final String EXO_ACTIONS   = "exo:actions".intern();
  
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
    if (parentNode != null) parentPath_ = parentNode.getPath();
  }
  
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true;
    getUIStringInput(selectField).setValue(value.toString());
    UITaxonomyManagerTrees uiTaxoManageTree = getAncestorOfType(UITaxonomyManagerTrees.class);
    uiTaxoManageTree.removeChildById(POPUP_COMPONENT);
  }
  
  public String getCurrentPath() throws Exception { 
    return parentPath_;
  }
  
  public void setCurrentPath(String path) {
    parentPath_ = path;
  }
  
  public String getWorkspace() {
    return getTaxoTreeData().getTaxoTreeWorkspace();
  }
  
  public String getRepositoryName() {
    return getTaxoTreeData().getRepository();
  }
  
  public TaxonomyTreeData getTaxoTreeData() {
    return getAncestorOfType(UITaxonomyTreeContainer.class).getTaxonomyTreeData();
  }
  
  private String getTaxonomyTreeHomePath() throws Exception{
    TaxonomyTreeData taxoTreeData = getTaxoTreeData();
    String workspace = taxoTreeData.getTaxoTreeWorkspace();
    String homePath = taxoTreeData.getTaxoTreeHomePath();
    String systemWorkspace = getAncestorOfType(UITaxonomyManagerTrees.class)
    .getSystemWorkspaceName(getRepositoryName());
    if (workspace.equals(systemWorkspace) && homePath.length() == 0) {
      homePath = getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
      taxoTreeData.setTaxoTreeHomePath(homePath);
    }
    return (workspace + ":" + homePath);
  }
  
  private String getJcrPath(String path) {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    return nodeHierarchyCreator.getJcrPath(path);
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return new JCRResourceResolver(getTaxoTreeData().getRepository(), getWorkspace(), "exo:templateFile");
  }

  public String getTemplate() {
    return getDialogPath();
  }

  public String getDialogPath() {
    repositoryName = getTaxoTreeData().getRepository();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    String dialogPath = null;
    if (nodeTypeName_ != null) {
      try {
        dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName_, userName, repositoryName);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return dialogPath;    
  }
  
  
  public void renderField(String name) throws Exception {
    TaxonomyTreeData taxoTreeData = getTaxoTreeData();
    UIComponent uiInput = findComponentById(name);
    String homPath = getTaxonomyTreeHomePath();
    if (homPath.endsWith("/"))
      homPath = homPath.substring(0, homPath.length() - 1);
    if ("homePath".equals(name)) {
      ((UIFormInput<String>) uiInput).setValue(homPath + "/" + taxoTreeData.getTaxoTreeName());
    }
    super.renderField(name);
  }
  
/*  private void setPath(String scriptPath) {
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = getWorkspace() + ":" + scriptPath ;
    }
    scriptPath_ = scriptPath ; 
  }*/
  
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
    UITaxonomyManagerTrees uiTaxonomyManagerTrees = getAncestorOfType(UITaxonomyManagerTrees.class);
    event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
  }
  
  public static class SaveActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiActionForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = uiActionForm.getAncestorOfType(UITaxonomyTreeContainer.class);
      UIPermissionTreeInfo uiPermissionInfo = uiTaxonomyTreeContainer.findFirstComponentOfType(UIPermissionTreeInfo.class);
      TaxonomyTreeData taxoTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiActionForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      TaxonomyService taxonomyService = uiTaxonomyTreeContainer.getApplicationComponent(TaxonomyService.class);
      String repository = uiActionForm.getTaxoTreeData().getRepository();
      String systemWorkspace = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class)
          .getSystemWorkspaceName(repository);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      String name = taxoTreeData.getTaxoTreeName();
      String workspace = taxoTreeData.getTaxoTreeWorkspace();
      String homePath = taxoTreeData.getTaxoTreeHomePath();
      homePath = homePath != null ? homePath : "";
      boolean isEdit = taxoTreeData.isEdit();
      if (homePath.length() == 0) {
        if (systemWorkspace.equals(workspace)) {
          homePath = uiActionForm.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
        } else {
          uiApp.addMessage(new ApplicationMessage("uiTaxonomyTreeForm.msg.homePath-emty", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }        
      
      boolean regAction = true;
      try {
        if (!isEdit) {
          uiTaxonomyTreeContainer.addTaxonomyTree(name, workspace, homePath, uiPermissionInfo.getPermBeans());
        } else {
          regAction = uiTaxonomyTreeContainer.updateTaxonomyTree(name, workspace, homePath);
        }
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.path-invalid", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (TaxonomyAlreadyExistsException e) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.taxonomy-existed", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (TaxonomyNodeAlreadyExistsException e) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.taxonomy-existed", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      Node currentNode = taxonomyService.getTaxonomyTree(repository, name, true);
      if (regAction) {
        ActionServiceContainer actionServiceContainer = uiActionForm.getApplicationComponent(ActionServiceContainer.class);
        Map<String, JcrInputProperty> sortedInputs = DialogFormUtil.prepareMap(uiActionForm
            .getChildren(), uiActionForm.getInputProperties());
        Session session = currentNode.getSession();
        if (uiActionForm.getCurrentPath().length() == 0) {
          uiActionForm.setCurrentPath(currentNode.getPath());
        }
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
          Node actionNode = actionServiceContainer.getAction(currentNode, actionName);
          uiActionForm.setIsOnchange(false);
          uiActionForm.setNodePath(actionNode.getPath());
          session.save();
          uiActionForm.setCurrentPath(currentNode.getPath());
          //uiActionForm.createNewAction(currentNode, uiActionForm.nodeTypeName_, true);
          //uiActionForm.reset();
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
          e.printStackTrace();
          uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null));
          return;
        }
      }
      
      UIPermissionTreeInfo uiPermInfo = uiTaxonomyTreeContainer.findFirstComponentOfType(UIPermissionTreeInfo.class);
      UIPermissionTreeForm uiPermForm = uiTaxonomyTreeContainer.findFirstComponentOfType(UIPermissionTreeForm.class);
      uiPermInfo.setCurrentNode(currentNode);
      uiPermForm.setCurrentNode(currentNode);
      uiPermInfo.updateGrid();
      //UITaxonomyTreeCreateChild uiTaxonomyCreateChild = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeCreateChild.class);
      //if (uiTaxonomyCreateChild == null) uiTaxonomyTreeContainer.addChild(UITaxonomyTreeCreateChild.class, null, null);
      uiTaxonomyTreeContainer.viewStep(4);
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeCreateChild.class);
      if (uiTaxonomyTreeCreateChild == null) 
        uiTaxonomyTreeCreateChild = uiTaxonomyTreeContainer.addChild(UITaxonomyTreeCreateChild.class, null, null);
      uiTaxonomyTreeCreateChild.setWorkspace(workspace);
      uiTaxonomyTreeCreateChild.setTaxonomyTreeNode(currentNode);
      //uiTaxonomyTreeCreateChild.setSelectedPath(currentNode.getPath());
      //String path = currentNode.getParent().getPath();
      //uiTaxonomyCreateChild.update(path);
      uiTaxonomyManagerTrees.update();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class BackActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource().getAncestorOfType(UITaxonomyTreeContainer.class);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiTaxonomyTreeContainer.viewStep(2);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ShowComponentActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource();
      UITaxonomyTreeContainer uiTaxonomyTree = uiForm.getAncestorOfType(UITaxonomyTreeContainer.class);
      UITaxonomyManagerTrees uiManager = uiForm.getAncestorOfType(UITaxonomyManagerTrees.class);
      uiForm.isShowingComponent = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      Map fieldPropertiesMap = uiForm.componentSelectors.get(fieldName);
      String classPath = (String) fieldPropertiesMap.get("selectorClass");
      String rootPath = (String) fieldPropertiesMap.get("rootPath");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz = Class.forName(classPath, true, cl);
      UIComponent uiComp = uiManager.createUIComponent(clazz, null, null);
      if (uiComp instanceof UIOneNodePathSelector) {
        String repositoryName = uiForm.getTaxoTreeData().getRepository();
        SessionProvider provider = SessionProviderFactory.createSessionProvider();
        String wsFieldName = (String) fieldPropertiesMap.get("workspaceField");
        String wsName = "";
        if (wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String) uiForm.<UIFormInputBase> getUIInput(wsFieldName).getValue();
          ((UIOneNodePathSelector) uiComp).setIsDisable(wsName, true);
        }
        String selectorParams = (String) fieldPropertiesMap.get("selectorParams");
        String[] filterType = new String[] {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED};
        ((UIOneNodePathSelector) uiComp).setAcceptedNodeTypesInPathPanel(filterType);
        ((UIOneNodePathSelector) uiComp).setAcceptedNodeTypesInTree(filterType);
        if (selectorParams != null) {
          String[] arrParams = selectorParams.split(",");
          if (arrParams.length == 4) {
            wsName = arrParams[1];
            rootPath = arrParams[2];
            ((UIOneNodePathSelector) uiComp).setIsDisable(wsName, true);
            if (arrParams[3].indexOf(";") > -1) {
              ((UIOneNodePathSelector) uiComp).setAcceptedMimeTypes(arrParams[3].split(";"));
            } else {
              ((UIOneNodePathSelector) uiComp).setAcceptedMimeTypes(new String[] { arrParams[3] });
            }
          }
        }
        if (rootPath == null)
          rootPath = "/";
        ((UIOneNodePathSelector) uiComp).setRootNodeLocation(repositoryName, wsName, rootPath);
        ((UIOneNodePathSelector) uiComp).setShowRootPathSelect(true);
        ((UIOneNodePathSelector) uiComp).init(provider);
      }
      uiManager.initPopupComponent(uiComp, UIActionForm.POPUP_COMPONENT);
      String param = "returnField=" + fieldName;
      ((ComponentSelector) uiComp).setSourceComponent(uiTaxonomyTree, new String[] { param });
      if (uiForm.isAddNew_) {
        UIContainer uiParent = uiManager.getParent();
        uiParent.setRenderedChild(uiManager.getId());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }
  
  static public class RemoveReferenceActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource() ;
      uiForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getUIStringInput(fieldName).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
}
