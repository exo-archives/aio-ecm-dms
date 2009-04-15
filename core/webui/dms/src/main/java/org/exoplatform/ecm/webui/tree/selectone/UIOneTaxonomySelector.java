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

import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UITreeTaxonomyBuilder;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:12:26 PM 
 */
@ComponentConfigs(
    {
      @ComponentConfig(
          template = "classpath:groovy/ecm/webui/UIContainerWithAction.gtmpl"
      ),
      @ComponentConfig(
          type = UIBreadcumbs.class, id = "BreadcumbOneTaxonomy",
          template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
          events = @EventConfig(listeners = UIOneTaxonomySelector.SelectPathActionListener.class)
      )
    }
)

public class UIOneTaxonomySelector extends UIBaseNodeTreeSelector {
  
  private String[] acceptedNodeTypesInTree = {};  
  private String[] acceptedNodeTypesInPathPanel = {};
  private String[] acceptedMimeTypes = {};
  
  private String[] exceptedNodeTypesInPathPanel = {};

  private String repositoryName = null;
  private String workspaceName = null;
  private String rootTreePath = null;
  private boolean isDisable = false;
  private boolean allowPublish = false;
  
  private boolean alreadyChangePath = false;
  
  public UIOneTaxonomySelector() throws Exception {
    addChild(UIBreadcumbs.class, "BreadcumbOneTaxonomy", "BreadcumbOneTaxonomy");
    addChild(UITreeTaxonomyList.class, null, null);
    addChild(UITreeTaxonomyBuilder.class, null, UITreeTaxonomyBuilder.class.getSimpleName()+hashCode());
    addChild(UISelectPathPanel.class, null, null);
  }
  
  public void init(SessionProvider sessionProvider) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    TemplateService templateService  = getApplicationComponent(TemplateService.class);
    List<String> templates = templateService.getDocumentTemplates(repositoryName);
    try {
      //TODO: Should review this method to make sure we have no problem with permission when use system session      
      Node rootNode;
      if (rootTreePath.trim().equals("/")) {
        rootNode = sessionProvider.getSession(workspaceName, manageableRepository).getRootNode();
      } else {
        NodeFinder nodeFinder = getApplicationComponent(NodeFinder.class);
        rootNode = (Node) nodeFinder.getItem(repositoryName, workspaceName, rootTreePath);
      }
      
      UITreeTaxonomyList uiTreeTaxonomyList = getChild(UITreeTaxonomyList.class);
      uiTreeTaxonomyList.setWorkspaceList(repositoryName);
      uiTreeTaxonomyList.setTaxonomyTreeList(repositoryName);
      uiTreeTaxonomyList.setIsDisable(workspaceName, isDisable);
      UITreeTaxonomyBuilder builder = getChild(UITreeTaxonomyBuilder.class);
      builder.setAllowPublish(allowPublish, publicationService, templates);
      builder.setAcceptedNodeTypes(acceptedNodeTypesInTree);
      builder.setRootTreeNode(rootNode);
      UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
      selectPathPanel.setAllowPublish(allowPublish, publicationService, templates);
      selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypesInPathPanel);
      selectPathPanel.setAcceptedMimeTypes(acceptedMimeTypes);
      selectPathPanel.setExceptedNodeTypes(exceptedNodeTypesInPathPanel);
      selectPathPanel.updateGrid();
    } finally {
      sessionProvider.close();
    }        
  }
  
  public boolean isAllowPublish() {
    return allowPublish;
  }

  public void setAllowPublish(boolean allowPublish) {
    this.allowPublish = allowPublish;
  }
  
  public void setRootNodeLocation(String repository, String workspace, String rootPath) throws Exception {
    this.repositoryName = repository;
    this.workspaceName = workspace;
    this.rootTreePath = rootPath;    
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    setWorkspaceName(wsName);
    this.isDisable = isDisable;
  }
  
  public boolean isDisable() { return isDisable; }
  
  public void setIsShowSystem(boolean isShowSystem) {
    getChild(UITreeTaxonomyList.class).setIsShowSystem(isShowSystem);
  }
  
  public void setShowRootPathSelect(boolean isRendered) {
    UITreeTaxonomyList uiTreeTaxonomyList = getChild(UITreeTaxonomyList.class);
    uiTreeTaxonomyList.setShowRootPathSelect(isRendered);
  }
  
  public String[] getAcceptedNodeTypesInTree() {
    return acceptedNodeTypesInTree;
  }

  public void setAcceptedNodeTypesInTree(String[] acceptedNodeTypesInTree) {
    this.acceptedNodeTypesInTree = acceptedNodeTypesInTree;
  }

  public String[] getAcceptedNodeTypesInPathPanel() {
    return acceptedNodeTypesInPathPanel;
  }

  public void setAcceptedNodeTypesInPathPanel(String[] acceptedNodeTypesInPathPanel) {
    this.acceptedNodeTypesInPathPanel = acceptedNodeTypesInPathPanel;
  }
  
  public String[] getExceptedNodeTypesInPathPanel() {
    return exceptedNodeTypesInPathPanel;
  }

  public void setExceptedNodeTypesInPathPanel(String[] exceptedNodeTypesInPathPanel) {
    this.exceptedNodeTypesInPathPanel = exceptedNodeTypesInPathPanel;
  }  
  
  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }
  
  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; } 

  public String getRepositoryName() { return repositoryName; }
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public String getWorkspaceName() { return workspaceName; }
  
  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }

  public String getRootTreePath() { return rootTreePath; }
  
  public void setRootTreePath(String rootTreePath) { this.rootTreePath = rootTreePath; 
  }      
  
  public void onChange(final Node currentNode, Object context) throws Exception {
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setParentNode(currentNode);
    selectPathPanel.updateGrid();
    UIBreadcumbs uiBreadcumbs = getChild(UIBreadcumbs.class);
    String pathName = currentNode.getName();
    //Node rootNode = (Node)currentNode.getSession().getItem(rootTreePath);    
    NodeFinder nodeFinder = getApplicationComponent(NodeFinder.class);
    Node rootNode = (Node) nodeFinder.getItem(repositoryName, workspaceName, rootTreePath);
    
    if (currentNode.equals(rootNode)) {
      pathName = "";
    }
    UIBreadcumbs.LocalPath localPath = new UIBreadcumbs.LocalPath(pathName, pathName);
    List<LocalPath> listLocalPath = uiBreadcumbs.getPath();
    StringBuilder buffer = new StringBuilder(1024);
    for(LocalPath iterLocalPath: listLocalPath) {
      buffer.append("/").append(iterLocalPath.getId());
    }
    if (!alreadyChangePath) {
      String path = buffer.toString();
      if (path.startsWith("//")) path = path.substring(1);
      if (!path.startsWith(rootTreePath)) path = rootTreePath + path;
      if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
      if (path.length() == 0) path = "/";
      Node currentBreadcumbsNode = getNodeByVirtualPath(path);
      if (currentNode.equals(rootNode)
          ||((!currentBreadcumbsNode.equals(rootNode) && currentBreadcumbsNode.getParent().equals(currentNode)))){
        if (listLocalPath != null && listLocalPath.size() > 0) {  
          listLocalPath.remove(listLocalPath.size() - 1);
        }
      } else {
          listLocalPath.add(localPath);
      }
    }
    alreadyChangePath = false;
    uiBreadcumbs.setPath(listLocalPath);
  }
  
  private Node getNodeByVirtualPath(String pathLinkNode) throws Exception{
    NodeFinder nodeFinder_ = getApplicationComponent(NodeFinder.class);
    Item item = nodeFinder_.getItem(repositoryName, workspaceName, pathLinkNode);
    return (Node)item;
  }
  
  private void changeNode(String stringPath, Object context) throws Exception {
    UITreeTaxonomyBuilder builder = getChild(UITreeTaxonomyBuilder.class);
    builder.changeNode(stringPath, context);
  }
  
  public void changeGroup(String groupId, Object context) throws Exception {
    String stringPath = rootTreePath;
    if (!rootTreePath.equals("/")) {
      stringPath += "/";    
    }
    UIBreadcumbs uiBreadcumb = getChild(UIBreadcumbs.class);
    if (groupId == null) groupId = "";
    List<LocalPath> listLocalPath = uiBreadcumb.getPath();
    if (listLocalPath == null || listLocalPath.size() == 0) return;
    List<String> listLocalPathString = new ArrayList<String>();
    for (LocalPath localPath : listLocalPath) {
      listLocalPathString.add(localPath.getId().trim());
    }
    if (listLocalPathString.contains(groupId)) {
      int index = listLocalPathString.indexOf(groupId);
      alreadyChangePath = false;
      if (index == listLocalPathString.size() - 1) return;
      for (int i = listLocalPathString.size() - 1; i > index; i--) {
        listLocalPathString.remove(i);
        listLocalPath.remove(i);
      }
      alreadyChangePath = true;
      uiBreadcumb.setPath(listLocalPath);
      for (int i = 0; i < listLocalPathString.size(); i++) {
        String pathName = listLocalPathString.get(i);
        if (pathName != null || !pathName.equals("")) {
          stringPath += pathName.trim();
          if (i < listLocalPathString.size() - 1) stringPath += "/";
        }
      }
      changeNode(stringPath, context);
    }
  }
  
  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      UIOneTaxonomySelector uiOneNodePathSelector = uiBreadcumbs.getParent();
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);    
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiOneNodePathSelector.changeGroup(selectGroupId, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiOneNodePathSelector);
    }
  }
}
