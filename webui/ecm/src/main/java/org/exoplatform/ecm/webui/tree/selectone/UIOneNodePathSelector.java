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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:12:26 PM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIOneNodePathSelector extends UIBaseNodeTreeSelector {
  
  private String[] acceptedNodeTypesInTree = {};  
  private String[] acceptedNodeTypesInPathPanel = {};
  private String[] acceptedMimeTypes = {};
  
  private String repositoryName = null;
  private String workspaceName = null ;
  private String rootTreePath = null;
  private boolean isDisable = false ;
  
  public UIOneNodePathSelector() throws Exception {
    addChild(UIWorkspaceList.class, null, null);
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getSimpleName()+hashCode()) ;
    addChild(UISelectPathPanel.class,null,null);
  }
  
  public void init(SessionProvider sessionProvider) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    try {
      Session session = sessionProvider.getSession(workspaceName, manageableRepository);
      Node rootNode = (Node)session.getItem(rootTreePath);
      UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class);
      uiWorkspaceList.setWorkspaceList(repositoryName);
      uiWorkspaceList.setIsDisable(workspaceName, isDisable) ;
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
    setWorkspaceName(wsName) ;
    this.isDisable = isDisable ;
  }
  
  public boolean isDisable() { return isDisable ; }
  
  public void setIsShowSystem(boolean isShowSystem) {
    getChild(UIWorkspaceList.class).setIsShowSystem(isShowSystem) ;
  }
  
  public void setShowRootPathSelect(boolean isRendered) {
    UIWorkspaceList uiWorkspaceList = getChild(UIWorkspaceList.class) ;
    uiWorkspaceList.setShowRootPathSelect(isRendered) ;
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
  }
  
}
