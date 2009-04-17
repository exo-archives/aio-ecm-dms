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

import javax.jcr.RepositoryException;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.taxonomy.action.UIActionForm;
import org.exoplatform.ecm.webui.selector.UIPermissionSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung hunghvit@gmail.com
 * Apr 3, 2009
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UITaxonomyManagerTrees extends UIContainer {
  
  public UITaxonomyManagerTrees() throws Exception {
    addChild(UITaxonomyTreeList.class, null, null);
  }

  public void initPopupTreeContainer(String id) throws Exception {
    UITaxonomyTreeContainer uiTaxonomyTreeContainer;
    UIPopupWindow uiPopup = getChildById(id);
    if (uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, id);
      uiPopup.setWindowSize(650, 450);
      uiTaxonomyTreeContainer = createUIComponent(UITaxonomyTreeContainer.class, null, null);
    } else {
      uiTaxonomyTreeContainer = uiPopup.findFirstComponentOfType(UITaxonomyTreeContainer.class);
      uiPopup.setRendered(true);
    }
    uiPopup.setUIComponent(uiTaxonomyTreeContainer);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
  }

  public void update() throws Exception {
    UITaxonomyTreeList uiTaxonomyTreeList = getChild(UITaxonomyTreeList.class);
    uiTaxonomyTreeList.updateTaxonomyTreeListGrid(uiTaxonomyTreeList.getUIPageIterator().getCurrentPage());
  }
  
  public void initPopupPermission(String membership) throws Exception {
    removePopup();
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null,
        UITaxonomyTreeContainer.POPUP_PERMISSION);
    uiPopup.setWindowSize(560, 300);
    UIPermissionSelector uiTaxonomyTreePermission = createUIComponent(UIPermissionSelector.class,
        null, null);
    uiTaxonomyTreePermission.setSelectedMembership(true);
    if (membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/");
      uiTaxonomyTreePermission.setCurrentPermission("/" + arrMember[1]);
    }
    uiPopup.setUIComponent(uiTaxonomyTreePermission);
    uiPopup.setShow(true);
  }
  
  public String getSystemWorkspaceName(String repository) throws RepositoryException, RepositoryConfigurationException {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    return manageableRepository.getConfiguration().getSystemWorkspaceName();
  }
  
  public void initPopupJCRBrowser(String workspace, boolean isDisable) throws Exception {
    removePopup();
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, UITaxonomyTreeContainer.POPUP_TAXONOMYHOMEPATH);
    uiPopup.setWindowSize(610, 300);
    String[] filterType = {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED};
    UIOneNodePathSelector uiOneNodePathSelector = createUIComponent(UIOneNodePathSelector.class, null, null);
    uiOneNodePathSelector.setIsDisable(workspace, isDisable);
    uiOneNodePathSelector.setShowRootPathSelect(true);
    uiOneNodePathSelector.setAcceptedNodeTypesInTree(filterType);
    uiOneNodePathSelector.setAcceptedNodeTypesInPathPanel(filterType);
    uiOneNodePathSelector.setRootNodeLocation(repository, workspace, "/");
    if (SessionProviderFactory.isAnonim()) {
      uiOneNodePathSelector.init(SessionProviderFactory.createAnonimProvider());
    } else if (workspace.equals(getSystemWorkspaceName(repository))) {
      uiOneNodePathSelector.init(SessionProviderFactory.createSystemProvider());
    } else {
      uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider());
    }
    uiPopup.setUIComponent(uiOneNodePathSelector);
    UITaxonomyTreeContainer uiTaxonomyTreeContainer = findFirstComponentOfType(UITaxonomyTreeContainer.class);
    uiOneNodePathSelector.setSourceComponent(uiTaxonomyTreeContainer,
        new String[] { UITaxonomyTreeMainForm.FIELD_HOMEPATH });
    uiPopup.setShow(true);
  }
  
  public void initPopupComponent(UIComponent uiComp, String id) throws Exception {
    removePopup();
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, id);
    uiPopup.setUIComponent(uiComp);
    uiPopup.setWindowSize(640, 300);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
  }
  
  private void removePopup() {
    removeChildById(UIActionForm.POPUP_COMPONENT);
    removeChildById(UITaxonomyTreeContainer.POPUP_PERMISSION);
    removeChildById(UITaxonomyTreeContainer.POPUP_TAXONOMYHOMEPATH);
  }
}
