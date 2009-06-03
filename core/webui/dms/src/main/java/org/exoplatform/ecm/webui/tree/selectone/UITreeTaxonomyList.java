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
package org.exoplatform.ecm.webui.tree.selectone;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UITreeTaxonomyBuilder;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 21, 2007 2:32:49 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/ecm/webui/form/UIFormWithoutAction.gtmpl",
    events = { 
      @EventConfig(listeners = UITreeTaxonomyList.ChangeTaxonomyTreeActionListener.class),
      @EventConfig(listeners = UITreeTaxonomyList.AddRootNodeActionListener.class)
    }
)
public class UITreeTaxonomyList extends UIForm {

  static private String ROOT_NODE_INFO = "rootNodeInfo";
  static private String ROOT_NODE_PATH = "rootNodePath";
  public static String TAXONOMY_TREE = "taxonomyTree";
  
  private boolean isShowSystem_ = true;

  public UITreeTaxonomyList() throws Exception {    
    List<SelectItemOption<String>> taxonomyTreeList = new ArrayList<SelectItemOption<String>>();
    UIFormSelectBox uiTaxonomyTreeList = new UIFormSelectBox(TAXONOMY_TREE, TAXONOMY_TREE, taxonomyTreeList);
    uiTaxonomyTreeList.setOnChange("ChangeTaxonomyTree");
    addUIFormInput(uiTaxonomyTreeList);
    
    UIFormInputSetWithAction rootNodeInfo = new UIFormInputSetWithAction(ROOT_NODE_INFO);
    rootNodeInfo.addUIFormInput(new UIFormInputInfo(ROOT_NODE_PATH, ROOT_NODE_PATH, null));
    String[] actionInfor = {"AddRootNode"};
    rootNodeInfo.setActionInfo(ROOT_NODE_PATH, actionInfor);
    rootNodeInfo.showActionInfo(true);
    rootNodeInfo.setRendered(false);
    addUIComponentInput(rootNodeInfo);
  }
  
  public void setIsShowSystem(boolean isShowSystem) { isShowSystem_ = isShowSystem; }
  
  public boolean isShowSystemWorkspace() { return isShowSystem_; }
  
  public void setShowRootPathSelect(boolean isRender) { 
    UIFormInputSetWithAction uiInputAction = getChildById(ROOT_NODE_INFO); 
    uiInputAction.setRendered(isRender); 
  }
  
  
  public void setTaxonomyTreeList(String repository) throws Exception {
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    List<Node> listNode = taxonomyService.getAllTaxonomyTrees(repository);
    List<SelectItemOption<String>> taxonomyTree = new ArrayList<SelectItemOption<String>>();
    for(Node itemNode : listNode) {
      taxonomyTree.add(new SelectItemOption<String>(itemNode.getName(), itemNode.getName()));
    }
    UIFormSelectBox uiTreeTaxonomyList = getUIFormSelectBox(TAXONOMY_TREE);
    uiTreeTaxonomyList.setOptions(taxonomyTree);
  }
  
  private Node getRootNode(String repositoryName, String workspaceName, String pathNode) throws 
    RepositoryException, RepositoryConfigurationException {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      SessionProvider sessionProvider =  SessionProviderFactory.createSessionProvider();
      return (Node) sessionProvider.getSession(workspaceName, manageableRepository).getItem(pathNode); 
  }
  
  static public class ChangeTaxonomyTreeActionListener extends EventListener<UITreeTaxonomyList> {
    public void execute(Event<UITreeTaxonomyList> event) throws Exception {
      UITreeTaxonomyList uiTreeTaxonomyList = event.getSource();
      UIOneTaxonomySelector uiOneTaxonomySelector = uiTreeTaxonomyList.getParent();
      String taxoTreeName = uiTreeTaxonomyList.getUIFormSelectBox(TAXONOMY_TREE).getValue();  
      Node taxoTreeNode = uiOneTaxonomySelector.getTaxoTreeNode(taxoTreeName);
      String workspaceName = taxoTreeNode.getSession().getWorkspace().getName();
      String pathTaxonomy = taxoTreeNode.getPath();
      UIApplication uiApp = uiTreeTaxonomyList.getAncestorOfType(UIApplication.class);
      if (taxoTreeNode.hasNodes()) {
        uiOneTaxonomySelector.setWorkspaceName(workspaceName);
        uiOneTaxonomySelector.setRootTaxonomyName(taxoTreeNode.getName());
        uiOneTaxonomySelector.setRootTreePath(taxoTreeNode.getPath());
        UIBreadcumbs uiBreadcumbs = uiOneTaxonomySelector.getChildById("BreadcumbOneTaxonomy");
        uiBreadcumbs.getPath().clear();
        UITreeTaxonomyBuilder uiTreeJCRExplorer = uiOneTaxonomySelector.getChild(UITreeTaxonomyBuilder.class);
        try {
          uiTreeJCRExplorer.setRootTreeNode(uiTreeTaxonomyList.getRootNode(uiOneTaxonomySelector
              .getRepositoryName(), workspaceName, pathTaxonomy));
          uiTreeJCRExplorer.buildTree();
        } catch (AccessDeniedException ade) {        
          uiApp.addMessage(new ApplicationMessage("UIWorkspaceList.msg.AccessDeniedException", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch(Exception e) {
          e.printStackTrace();
          return;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UITreeTaxonomyList.msg.NoChild", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiOneTaxonomySelector);
    }
  }
  
  static public class AddRootNodeActionListener extends EventListener<UITreeTaxonomyList> {
    public void execute(Event<UITreeTaxonomyList> event) throws Exception {
      UITreeTaxonomyList uiTreeTaxonomyList = event.getSource();
      UIOneTaxonomySelector uiJBrowser = uiTreeTaxonomyList.getParent();
      String returnField = uiJBrowser.getReturnFieldName();
      String workspaceName = uiJBrowser.getWorkspaceName();
      String repositoryName = uiJBrowser.getRepositoryName();
      RepositoryService repositoryService = uiTreeTaxonomyList.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
      String value = session.getRootNode().getPath();
      if(!uiJBrowser.isDisable()) value = uiJBrowser.getWorkspaceName() + ":" + value;
      ((UISelectable)uiJBrowser.getSourceComponent()).doSelect(returnField, value);
    }
  }
}
