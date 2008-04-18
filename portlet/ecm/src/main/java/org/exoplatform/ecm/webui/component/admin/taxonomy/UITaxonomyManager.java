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
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 10, 2008 4:28:44 PM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UITaxonomyManager extends UIContainer {
  
  static private String TAXONIMIES_ALIAS = "exoTaxonomiesPath" ;

  public UITaxonomyManager() throws Exception {
    addChild(UITaxonomyTree.class, null, null) ;
    addChild(UITaxonomyWorkingArea.class, null, null) ;
  }
  
  public void update() throws Exception {
    UITaxonomyTree uiTree = getChild(UITaxonomyTree.class) ;
    uiTree.update() ;
  }
  
  public void update(String parentPath) throws Exception {
    UITaxonomyTree uiTree = getChild(UITaxonomyTree.class) ;
    uiTree.setNodeSelect(parentPath) ;
    UITaxonomyWorkingArea uiWorkingArea = getChild(UITaxonomyWorkingArea.class) ;
    uiWorkingArea.setSelectedPath(parentPath) ;
    uiWorkingArea.update() ;    
  }
  
  public Node getRootNode() throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class) ;
    return (Node)getSession().getItem(nodeHierarchyCreator.getJcrPath(TAXONIMIES_ALIAS)) ;
  }
  
  public Node getNodeByPath(String path) throws Exception {
    return (Node) getSession().getItem(path) ;
  }
  
  public String getRepository() throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences pref = pcontext.getRequest().getPreferences() ;
    String repository = pref.getValue(Utils.REPOSITORY, "") ;
    return repository ;
  }
  
  public Session getSession() throws Exception {
    String repositoryName = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    String workspace = getRepository(repositoryName).getConfiguration().getSystemWorkspaceName() ;
    return SessionsUtils.getSystemProvider().getSession(workspace, getRepository(repositoryName)) ;
  }
  
  public ManageableRepository getRepository(String repositoryName) throws Exception{
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    return repositoryService.getRepository(repositoryName) ;
  }
  
  public void initPopup(String path) throws Exception {
    removeChildById("TaxonomyPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "TaxonomyPopup") ;
    uiPopup.setWindowSize(600,250) ;
    UITaxonomyForm uiTaxoForm = createUIComponent(UITaxonomyForm.class, null, null) ;
    uiTaxoForm.setParent(path) ;
    uiPopup.setUIComponent(uiTaxoForm) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}
