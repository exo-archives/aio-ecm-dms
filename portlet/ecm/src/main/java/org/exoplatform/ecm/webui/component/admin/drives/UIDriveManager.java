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
package org.exoplatform.ecm.webui.component.admin.drives;

import javax.jcr.RepositoryException;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.selector.UIPermissionSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 11:45:11 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIDriveManager extends UIContainer {
  
  public UIDriveManager() throws Exception {
    addChild(UIDriveList.class, null, null) ;
  }
  public void update()throws Exception  {
    getChild(UIDriveList.class).updateDriveListGrid() ;
  }
  public void initPopup(String id) throws Exception {
    UIDriveForm uiDriveForm ;
    UIPopupWindow uiPopup = getChildById(id) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, id) ;
      uiPopup.setWindowSize(560,420) ;      
      uiDriveForm = createUIComponent(UIDriveForm.class, null, null) ;
    } else {
      uiDriveForm = uiPopup.findFirstComponentOfType(UIDriveForm.class) ;
      uiPopup.setRendered(true) ;
    }
    uiPopup.setUIComponent(uiDriveForm) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPopupPermission(String membership) throws Exception {
    removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, UIDriveForm.POPUP_DRIVEPERMISSION);
    uiPopup.setWindowSize(560, 300);
    UIPermissionSelector uiECMPermission = 
      createUIComponent(UIPermissionSelector.class, null, null) ;
    uiECMPermission.setSelectedMembership(true);
    if(membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/") ;
      uiECMPermission.setCurrentPermission("/" + arrMember[1]) ;
    }
    uiPopup.setUIComponent(uiECMPermission);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiECMPermission.setSourceComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_PERMISSION}) ;
    uiPopup.setShow(true) ;
  }
  
  private String getSystemWorkspaceName(String repository) throws RepositoryException, RepositoryConfigurationException {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    return manageableRepository.getConfiguration().getSystemWorkspaceName();
  }
  
  public void initPopupJCRBrowser(String workspace, boolean isDisable) throws Exception {
    removeChildById("JCRBrowser") ;
    removeChildById("JCRBrowserAssets") ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "JCRBrowser");
    uiPopup.setWindowSize(610, 300);
    UIOneNodePathSelector uiOneNodePathSelector = 
      createUIComponent(UIOneNodePathSelector.class, null, null);
    uiOneNodePathSelector.setIsDisable(workspace, isDisable) ;
    uiOneNodePathSelector.setShowRootPathSelect(true) ;
    uiOneNodePathSelector.setRootNodeLocation(repository, workspace, "/");
    if(SessionProviderFactory.isAnonim()) {
      uiOneNodePathSelector.init(SessionProviderFactory.createAnonimProvider()) ;
    } else if(workspace.equals(getSystemWorkspaceName(repository))){
      uiOneNodePathSelector.init(SessionProviderFactory.createSystemProvider()) ;
    } else {
      uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider()) ;
    }
    uiPopup.setUIComponent(uiOneNodePathSelector);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    uiOneNodePathSelector.setSourceComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_HOMEPATH}) ;
    uiPopup.setShow(true) ;
  }
  
  public void initPopupJCRBrowserAssets(String workspace) throws Exception {
    removeChildById("JCRBrowserAssets") ;
    removeChildById("JCRBrowser") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "JCRBrowserAssets");
    uiPopup.setWindowSize(610, 300);
    UIOneNodePathSelector uiOneNodePathSelector = 
      createUIComponent(UIOneNodePathSelector.class, null, null);
    UIDriveForm uiDriveForm = findFirstComponentOfType(UIDriveForm.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    uiOneNodePathSelector.setAcceptedNodeTypesInPathPanel(new String[] {Utils.NT_FILE}) ;
    uiOneNodePathSelector.setAcceptedNodeTypesInTree(new String[] {Utils.NT_UNSTRUCTURED, Utils.NT_FOLDER});
    uiOneNodePathSelector.setAcceptedMimeTypes(new String[] {"image/jpeg", "image/gif", "image/png"}) ;
    uiOneNodePathSelector.setRootNodeLocation(repository, workspace, "/");
    if(SessionProviderFactory.isAnonim()) {
      uiOneNodePathSelector.init(SessionProviderFactory.createAnonimProvider()) ;
    } else if(workspace.equals(getSystemWorkspaceName(repository))){
      uiOneNodePathSelector.init(SessionProviderFactory.createSystemProvider()) ;
    } else {
      uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider()) ;
    }
    uiOneNodePathSelector.setSourceComponent(uiDriveForm, new String[] {UIDriveInputSet.FIELD_WORKSPACEICON}) ;
    uiPopup.setUIComponent(uiOneNodePathSelector);
    uiPopup.setShow(true) ;
  }
}
