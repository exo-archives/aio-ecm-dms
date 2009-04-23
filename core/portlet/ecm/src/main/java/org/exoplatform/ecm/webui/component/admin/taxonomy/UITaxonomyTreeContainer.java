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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.exoplatform.ecm.permission.PermissionBean;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.tree.info.UIPermissionTreeInfo;
import org.exoplatform.ecm.webui.component.admin.taxonomy.tree.info.UIPermissionTreeManager;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.TaxonomyTreeData;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyNodeAlreadyExistsException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
      @EventConfig(listeners = UITaxonomyTreeContainer.RefreshActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.CloseActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep1ActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep2ActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep3ActionListener.class),
      @EventConfig(listeners = UITaxonomyTreeContainer.ViewStep4ActionListener.class)
    }
)

public class UITaxonomyTreeContainer extends UIContainer implements UISelectable {

  private int                wizardMaxStep_         = 4;

  private int                selectedStep_          = 1;

  private int                currentStep_           = 0;

  private TaxonomyTreeData   taxonomyTreeData;

  public static final String POPUP_PERMISSION       = "PopupTaxonomyTreePermission";

  public static final String POPUP_TAXONOMYHOMEPATH = "PopupTaxonomyJCRBrowser";

  private String[]           actions_               = { "Close" };
  
  public UITaxonomyTreeContainer() throws Exception {
    addChild(UITaxonomyTreeMainForm.class, null, "TaxonomyTreeMainForm");
    addChild(UIPermissionTreeManager.class, null, "TaxonomyPermissionTree").setRendered(false);
    addChild(UIActionTaxonomyManager.class, null, null).setRendered(false);
  }
  
  public String[] getActions() {return actions_;}
  
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
    selectedStep_ = step;
    currentStep_ = step - 1;    
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
    if (taxonomyTreeData == null) {
      taxonomyTreeData = new TaxonomyTreeData();
    }
    taxonomyTreeData.setRepository(getRepository());
    String taxoTreeName = taxonomyTreeData.getTaxoTreeName();
    UIActionTaxonomyManager uiActionTaxonomyManager = getChild(UIActionTaxonomyManager.class);
    
    if (taxoTreeName != null && taxoTreeName.length() > 0) {
      TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
      ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class);
      Node taxoTreeNode = taxonomyService.getTaxonomyTree(taxonomyTreeData.getRepository(),
          taxoTreeName, true);
      loadData(taxoTreeNode);
      Node actionNode = actionService.getAction(taxoTreeNode,
          taxonomyTreeData.getTaxoTreeActionName());
      UIActionForm uiActionForm = uiActionTaxonomyManager.getChild(UIActionForm.class);
      uiActionForm.createNewAction(taxoTreeNode, actionNode.getPrimaryNodeType().getName(), false);
      uiActionForm.setWorkspace(taxonomyTreeData.getTaxoTreeWorkspace());
      uiActionForm.setNodePath(actionNode.getPath());
    }

    uiActionTaxonomyManager.getChild(UIActionTypeForm.class).update();
    findFirstComponentOfType(UITaxonomyTreeMainForm.class).update(taxonomyTreeData);
    UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = getChild(UITaxonomyTreeCreateChild.class);
    if (uiTaxonomyTreeCreateChild != null) {
      uiTaxonomyTreeCreateChild.setWorkspace(taxonomyTreeData.getTaxoTreeWorkspace());
    }
  }
  
  private void loadData(Node taxoTreeTargetNode) throws RepositoryException{
    String taxoTreeName = taxonomyTreeData.getTaxoTreeName();
    if (taxoTreeName == null || taxoTreeName.length() == 0) return;
    if (taxoTreeTargetNode != null) {
        Session session = taxoTreeTargetNode.getSession();
        taxonomyTreeData.setTaxoTreeWorkspace(session.getWorkspace().getName());
        taxonomyTreeData.setTaxoTreeHomePath(taxoTreeTargetNode.getParent().getPath());
        taxonomyTreeData.setTaxoTreePermissions("");
        NodeIterator nodeIterator = taxoTreeTargetNode.getNode("exo:actions").getNodes();
        if (nodeIterator != null && nodeIterator.getSize() > 0) {
          Node node = null;
          while (nodeIterator.hasNext()) {
            node = nodeIterator.nextNode();
            if (node.isNodeType(TaxonomyTreeData.ACTION_TAXONOMY_TREE)) {
              break;
            }
            node = null;
          }
          if (node != null) {
            taxonomyTreeData.setTaxoTreeActionName(node.getName());
            /*taxonomyTreeData.setTaxoTreeActionTargetPath(node.getProperty("exo:targetPath").getString());
            taxonomyTreeData.setTaxoTreeActionTargetWorkspace(node.getProperty("exo:targetWorkspace").getString());*/
          }
        }
    }
  }
  private UIFormStringInput getFormInputById(String id) {
    return (UIFormStringInput)findComponentById(id);
  }
  
  private String getRepository(){
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  public Session getSession(String workspace) throws RepositoryException, RepositoryConfigurationException  {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class);
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    return rservice.getRepository(repository).getSystemSession(workspace);
  }
  
  public void doSelect(String selectField, Object value) throws Exception {
    getFormInputById(selectField).setValue(value.toString());
    UITaxonomyManagerTrees uiContainer = getAncestorOfType(UITaxonomyManagerTrees.class);
    for (UIComponent uiChild : uiContainer.getChildren()) {
      if (uiChild.getId().equals(UITaxonomyTreeContainer.POPUP_PERMISSION)
          || uiChild.getId().equals(UITaxonomyTreeContainer.POPUP_TAXONOMYHOMEPATH)) {
        UIPopupWindow uiPopup = uiContainer.getChildById(uiChild.getId());
        uiPopup.setRendered(false);
        uiPopup.setShow(false);
      }
    }
  }
  
  public void addTaxonomyTree(String name, String workspace, String homePath, List<PermissionBean> permBeans)
      throws TaxonomyAlreadyExistsException, TaxonomyNodeAlreadyExistsException, AccessControlException, Exception {
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    taxonomyService.addTaxonomyNode(getRepository(), workspace, homePath, name);
    Session session = getSession(workspace);
    Node homeNode = (Node)session.getItem(homePath);
    Node taxonomyTreeNode = homeNode.getNode(name);
    ExtendedNode node = (ExtendedNode) taxonomyTreeNode;
    if (permBeans != null && permBeans.size() > 0) {
      if (PermissionUtil.canChangePermission(node)) {
        if (node.canAddMixin("exo:privilegeable")){
          node.addMixin("exo:privilegeable");
          node.setPermission(Utils.getNodeOwner(node),PermissionType.ALL);
        }
        for(PermissionBean permBean : permBeans) {
          List<String> permsList = new ArrayList<String>();
          if (permBean.isRead()) permsList.add(PermissionType.READ);
          if (permBean.isAddNode()) permsList.add(PermissionType.ADD_NODE);
          if (permBean.isRemove()) permsList.add(PermissionType.REMOVE);
          if (permBean.isSetProperty()) permsList.add(PermissionType.SET_PROPERTY);
          if(PermissionUtil.canChangePermission(node)) {
            if (permsList.size() > 0) {
              node.setPermission(permBean.getUsersOrGroups(), permsList.toArray(new String[permsList.size()]));
              
            }
          }
        }
        node.save();
      }
    }
    homeNode.save();
    session.save();
    session.logout();
    taxonomyService.addTaxonomyTree(taxonomyTreeNode);
  }
  
  public boolean updateTaxonomyTree(String name, String workspace, String homePath)
      throws RepositoryException, AccessControlException, Exception {
    
    String repository = getRepository();
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    Node taxonomyTreeNode = taxonomyService.getTaxonomyTree(repository, name, true);
    Node homeNode = taxonomyTreeNode.getParent();
    String srcWorkspace = taxonomyTreeNode.getSession().getWorkspace().getName();
    Session session = getSession(workspace);
    Workspace objWorkspace = session.getWorkspace();
    if (homeNode.getPath().equals(homePath) && srcWorkspace.equals(workspace)) return false;
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class);
    Node actionNode = taxonomyTreeNode.getNode(UIActionForm.EXO_ACTIONS);
    //remove action
    if(actionNode != null && actionNode.hasNodes()) {
      for (Node actionTmpNode : actionService.getActions(taxonomyTreeNode)) {
        actionService.removeAction(taxonomyTreeNode, actionTmpNode.getName(), repository);
      }
    }
    
    String destPath = homePath + "/" + name;
    if (srcWorkspace.equals(workspace)) {
      objWorkspace.move(taxonomyTreeNode.getPath(), destPath);
    } else {
      objWorkspace.copy(srcWorkspace, taxonomyTreeNode.getPath(), destPath);
      taxonomyTreeNode.remove();
      homeNode.save();
    }
    session.save();
    //Update taxonomy tree
    taxonomyTreeNode = (Node)session.getItem(destPath);
    taxonomyService.updateTaxonomyTree(name, taxonomyTreeNode);
    return true;
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
      uiTaxonomyManagerTrees.update();
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewStep1ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      uiTaxonomyTreeContainer.viewStep(1);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }

  public static class ViewStep2ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeMainForm.class);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      int validateCode = uiTaxonomyTreeMainForm.checkForm();
      if (validateCode == 1) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.name-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if (validateCode == 2) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.homePath-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiTaxonomyTreeContainer.viewStep(2);
      UIPermissionTreeManager uiPermissionManage = uiTaxonomyTreeContainer.getChild(UIPermissionTreeManager.class);
      uiPermissionManage.update();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewStep3ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      UITaxonomyTreeMainForm uiTaxonomyTreeMainForm = uiTaxonomyTreeContainer.getChild(UITaxonomyTreeMainForm.class);
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer.getAncestorOfType(UITaxonomyManagerTrees.class);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      int validateCode = uiTaxonomyTreeMainForm.checkForm();
      if (validateCode == 1) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.name-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if (validateCode == 2) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeMainForm.msg.homePath-emty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      UIPermissionTreeInfo uiPermInfo = uiTaxonomyTreeContainer.findFirstComponentOfType(UIPermissionTreeInfo.class);
      if (uiPermInfo.getPermBeans().size() < 1) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionTreeForm.msg.have-not-any-permission",
            null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      TaxonomyTreeData taxonomyTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      UIActionTaxonomyManager uiActionTaxonomyManager = uiTaxonomyTreeContainer.getChild(UIActionTaxonomyManager.class);
      UIActionForm uiActionForm = uiTaxonomyTreeContainer.findFirstComponentOfType(UIActionForm.class);
      uiActionTaxonomyManager.setDefaultConfig();
      TaxonomyService taxonomyService = uiTaxonomyTreeContainer.getApplicationComponent(TaxonomyService.class);
      ActionServiceContainer actionService = uiTaxonomyTreeContainer.getApplicationComponent(ActionServiceContainer.class);
      Node taxoTreeNode = taxonomyService.getTaxonomyTree(taxonomyTreeData.getRepository(),
          taxonomyTreeData.getTaxoTreeName(), true);
      if (taxoTreeNode != null) {
        uiActionTaxonomyManager.removeChild(UIActionForm.class);
        uiActionForm = uiActionTaxonomyManager.addChild(UIActionForm.class, null, null);
        Node actionNode = actionService.getAction(taxoTreeNode, taxonomyTreeData.getTaxoTreeActionName());
        uiActionForm.setIsOnchange(false);
        uiActionForm.setNodePath(actionNode.getPath());
        uiActionForm.createNewAction(taxoTreeNode, actionNode.getPrimaryNodeType().getName(), false);
      } else {
        uiActionForm.createNewAction(null, TaxonomyTreeData.ACTION_TAXONOMY_TREE, true);
      }
      uiActionForm.setWorkspace(taxonomyTreeData.getTaxoTreeWorkspace());
      uiActionTaxonomyManager.setRenderSibbling(UIActionTaxonomyManager.class);
      uiTaxonomyTreeContainer.viewStep(3);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }
  
  public static class ViewStep4ActionListener extends EventListener<UITaxonomyTreeContainer> {
    public void execute(Event<UITaxonomyTreeContainer> event) throws Exception {
      UITaxonomyTreeContainer uiTaxonomyTreeContainer = event.getSource();
      TaxonomyTreeData taxoTreeData = uiTaxonomyTreeContainer.getTaxonomyTreeData();
      TaxonomyService taxonomyService = uiTaxonomyTreeContainer
          .getApplicationComponent(TaxonomyService.class);
      UIApplication uiApp = uiTaxonomyTreeContainer.getAncestorOfType(UIApplication.class);
      
      if (taxoTreeData.getTaxoTreeName() == null || taxoTreeData.getTaxoTreeName().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeContainer.msg.not-exist-tree", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      
      if (!taxonomyService.hasTaxonomyTree(taxoTreeData.getRepository(), taxoTreeData.getTaxoTreeName())) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeContainer.msg.not-exist-tree", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      Node currentTreeNode = taxonomyService.getTaxonomyTree(taxoTreeData.getRepository(),
          taxoTreeData.getTaxoTreeName(), true);
      if (currentTreeNode == null) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyTreeContainer.msg.not-exist-tree", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      UITaxonomyTreeCreateChild uiTaxonomyCreateChild = uiTaxonomyTreeContainer
          .getChild(UITaxonomyTreeCreateChild.class);
      if (uiTaxonomyCreateChild == null)
        uiTaxonomyTreeContainer.addChild(UITaxonomyTreeCreateChild.class, null, null);
      uiTaxonomyCreateChild.setWorkspace(taxoTreeData.getTaxoTreeWorkspace());
      uiTaxonomyTreeContainer.viewStep(4);
      uiTaxonomyCreateChild.setSelectedPath(currentTreeNode.getPath());
      uiTaxonomyCreateChild.setTaxonomyTreeNode(currentTreeNode);
      uiTaxonomyCreateChild.update();
      UITaxonomyManagerTrees uiTaxonomyManagerTrees = uiTaxonomyTreeContainer
          .getAncestorOfType(UITaxonomyManagerTrees.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyManagerTrees);
    }
  }

  public TaxonomyTreeData getTaxonomyTreeData() {
    return taxonomyTreeData;
  }

  public void setTaxonomyTreeData(TaxonomyTreeData taxonomyTreeData) {
    this.taxonomyTreeData = taxonomyTreeData;
  }
}
