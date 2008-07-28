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
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
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
  
  private List<String> acceptedNodeTypesInTree = new ArrayList<String>();  
  private List<String> acceptedNodeTypesInPathPanel = new ArrayList<String>();
  
  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  private String repositoryName = null;
  private String workspaceName = null ;
  private String rootTreePath = null;
  
  public UIOneNodePathSelector() throws Exception {
    //addChild(UIDriveListForm.class, null, UIDriveListForm.class.getSimpleName()+hashCode()) ;
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getSimpleName()+hashCode()) ;
    addChild(UISelectPathPanel.class,null,null);
  }
  
  public void init() throws Exception {
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    Session session = provider.getSession(workspaceName,manageableRepository);
    Node rootNode = (Node)session.getItem(rootTreePath);
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypesInPathPanel);
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);    
    builder.setAcceptedNodeTypes(acceptedNodeTypesInTree);    
    builder.setRootTreeNode(rootNode);    
  }
  
  public void setRootNodeLocation(String repository, String workspace, String rootPath) throws Exception {
    this.repositoryName = repository;
    this.workspaceName = workspace;
    this.rootTreePath = rootPath;    
  }
  
  public List<String> getAcceptedNodeTypesInTree() {
    return acceptedNodeTypesInTree;
  }

  public void setAcceptedNodeTypesInTree(List<String> acceptedNodeTypesInTree) {
    this.acceptedNodeTypesInTree = acceptedNodeTypesInTree;
  }

  public List<String> getAcceptedNodeTypesInPathPanel() {
    return acceptedNodeTypesInPathPanel;
  }

  public void setAcceptedNodeTypesInPathPanel(List<String> acceptedNodeTypesInPathPanel) {
    this.acceptedNodeTypesInPathPanel = acceptedNodeTypesInPathPanel;
  }  

  public String getReturnFieldName() { return returnFieldName; }

  public void setReturnFieldName(String returnFieldName) {
    this.returnFieldName = returnFieldName;
  }

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
  
  public UIComponent getReturnComponent() { return uiComponent ; }  
  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }  

  public void onChange(final Node currentNode, final WebuiRequestContext requestContext) throws Exception {
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setParentNode(currentNode);
  } 
  
}
