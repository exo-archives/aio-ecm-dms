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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
          type = UIBreadcumbs.class, id = "BreadcumbCategoriesOne",
          template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
          events = @EventConfig(listeners = UIOneNodePathSelector.SelectPathActionListener.class)
      )
    }
)

public class UIOneNodePathSelector extends UIBaseNodeTreeSelector {
  
  private String[] acceptedNodeTypesInTree = {};  
  private String[] acceptedNodeTypesInPathPanel = {};
  private String[] acceptedMimeTypes = {};
  
  private String repositoryName = null;
  private String workspaceName = null;
  private String rootTreePath = null;
  private boolean isDisable = false;
  private String pathTaxonomy = "";
  
  private static String TAXONOMIES_ALIAS = "exoTaxonomiesPath";
  
  public UIOneNodePathSelector() throws Exception {
    addChild(UIBreadcumbs.class, "BreadcumbCategoriesOne", "BreadcumbCategoriesOne");
    addChild(UIWorkspaceList.class, null, null);
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getSimpleName()+hashCode());
    addChild(UISelectPathPanel.class, null, null);
  }
  
  public void init(SessionProvider sessionProvider) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    try {
//TODO: Should review this method to make sure we have no problem with permission when use system session      
      Node rootNode;
      if (rootTreePath.trim().equals("/")) {
        rootNode = manageableRepository.getSystemSession(workspaceName).getRootNode();
        pathTaxonomy = ((Node)manageableRepository.getSystemSession(workspaceName).
              getItem(nodeHierarchyCreator.getJcrPath(TAXONOMIES_ALIAS))).getPath() + "/";
      } else {
        Session session = sessionProvider.getSession(workspaceName, manageableRepository);
        rootNode = (Node)session.getItem(rootTreePath);
        pathTaxonomy = ((Node)session.getItem(nodeHierarchyCreator.getJcrPath(TAXONOMIES_ALIAS))).getPath() + "/";
      }
      
      UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class);
      uiWorkspaceList.setWorkspaceList(repositoryName);
      uiWorkspaceList.setIsDisable(workspaceName, isDisable);
      UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);    
      builder.setAcceptedNodeTypes(acceptedNodeTypesInTree);    
      builder.setRootTreeNode(rootNode);
      
      UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
      selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypesInPathPanel);
      selectPathPanel.setAcceptedMimeTypes(acceptedMimeTypes);
      selectPathPanel.updateGrid();
      
      
    } finally {
      sessionProvider.close();
    }        
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
    getChild(UIWorkspaceList.class).setIsShowSystem(isShowSystem);
  }
  
  public void setShowRootPathSelect(boolean isRendered) {
    UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class);
    uiWorkspaceList.setShowRootPathSelect(isRendered);
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
    List<LocalPath> listLocalPath = new ArrayList<LocalPath>();
    String path = currentNode.getPath().trim();
    String[] arrayPath = path.split("/");
    if (arrayPath.length > 0) {
      for (int i = 0; i < arrayPath.length; i++) {
        if (!arrayPath[i].trim().equals("") && !arrayPath[i].trim().equals("jcr:system") &&
            !arrayPath[i].trim().equals("exo:ecm") && !arrayPath[i].trim().equals("exo:taxonomies")) {
          UIBreadcumbs.LocalPath localPath1 = new UIBreadcumbs.LocalPath(arrayPath[i].trim(), arrayPath[i].trim());
          listLocalPath.add(localPath1);
        }
      }
    }
    uiBreadcumbs.setPath(listLocalPath);
  }
  
  private void changeNode(String stringPath, Object context) throws Exception {
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);
    builder.changeNode(stringPath, context);
  }
  
  public void changeGroup(String groupId, Object context) throws Exception {    
    String stringPath = pathTaxonomy;    
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
      if (index == listLocalPathString.size() - 1) return;
      for (int i = listLocalPathString.size() - 1; i > index; i--) {
        listLocalPathString.remove(i);
        listLocalPath.remove(i);
      }
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
      UIOneNodePathSelector uiOneNodePathSelector = uiBreadcumbs.getParent();
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);    
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiOneNodePathSelector.changeGroup(selectGroupId, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiOneNodePathSelector);
    }
  }
}
