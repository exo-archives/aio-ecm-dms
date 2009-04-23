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
  static private String TAXONOMY_TREE = "taxonomyTree";
  
  private List<String> wsList_;
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
  
  public void setWorkspaceList(String repository) throws Exception {
    wsList_ = new ArrayList<String>();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String[] wsNames = repositoryService.getRepository(repository).getWorkspaceNames();
    String systemWsName = 
      repositoryService.getRepository(repository).getConfiguration().getSystemWorkspaceName();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>();
    for(String wsName : wsNames) {
      if(!isShowSystem_) {
        if(!wsName.equals(systemWsName)) {
          workspace.add(new SelectItemOption<String>(wsName,  wsName));
          wsList_.add(wsName);
        }
      } else {
        workspace.add(new SelectItemOption<String>(wsName,  wsName));
        wsList_.add(wsName);
      }
    }
    UIFormSelectBox uiTreeTaxonomyList = getUIFormSelectBox(TAXONOMY_TREE);
    uiTreeTaxonomyList.setOptions(workspace);
    UIOneTaxonomySelector uiBrowser = getParent();
    if(uiBrowser.getWorkspaceName() != null) {
      if(wsList_.contains(uiBrowser.getWorkspaceName())) {
        uiTreeTaxonomyList.setValue(uiBrowser.getWorkspaceName()); 
      }
    }
  }
  
  public void setTaxonomyTreeList(String repository) throws Exception {
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    List<Node> listNode = new ArrayList<Node>();
    listNode = taxonomyService.getAllTaxonomyTrees(repository);
    List<SelectItemOption<String>> taxonomyTree = new ArrayList<SelectItemOption<String>>();
    for(Node itemNode : listNode) {
      String value = itemNode.getSession().getWorkspace().getName() + ":" + itemNode.getPath(); 
      taxonomyTree.add(new SelectItemOption<String>(itemNode.getName(), value));
    }
    UIFormSelectBox uiTreeTaxonomyList = getUIFormSelectBox(TAXONOMY_TREE);
    uiTreeTaxonomyList.setOptions(taxonomyTree);
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    if(wsList_.contains(wsName)) getUIFormSelectBox(TAXONOMY_TREE).setValue(wsName); 
    getUIFormSelectBox(TAXONOMY_TREE).setDisabled(isDisable);
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
      UIOneTaxonomySelector uiJBrowser = uiTreeTaxonomyList.getParent();
      String valueTaxonomy = uiTreeTaxonomyList.getUIFormSelectBox(TAXONOMY_TREE).getValue();      
      String workspaceName = uiJBrowser.getWorkspaceName();
      String pathTaxonomy = valueTaxonomy;
      if (valueTaxonomy.indexOf(":/") > -1) {
        String[] arrayValueTaxonomy = valueTaxonomy.split(":/");
        workspaceName = arrayValueTaxonomy[0];
        if (arrayValueTaxonomy[1].startsWith("/")) 
          pathTaxonomy = arrayValueTaxonomy[1];
        else
          pathTaxonomy = "/" + arrayValueTaxonomy[1];
      }
      
      UITreeTaxonomyBuilder uiTreeJCRExplorer = uiJBrowser.getChild(UITreeTaxonomyBuilder.class);
      UIApplication uiApp = uiTreeTaxonomyList.getAncestorOfType(UIApplication.class);
      try {
        uiTreeJCRExplorer.setRootTreeNode(uiTreeTaxonomyList.getRootNode(uiJBrowser.getRepositoryName(), 
            workspaceName, pathTaxonomy));
      } catch (AccessDeniedException ade) {        
        uiTreeTaxonomyList.getUIFormSelectBox(TAXONOMY_TREE).setValue("collaboration");
        uiApp.addMessage(new ApplicationMessage("UIWorkspaceList.msg.AccessDeniedException", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        e.printStackTrace();
        return;
      }
      uiTreeJCRExplorer.buildTree();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJBrowser);
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
