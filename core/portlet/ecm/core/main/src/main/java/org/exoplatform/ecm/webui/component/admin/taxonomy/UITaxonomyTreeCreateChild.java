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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 5, 2009  
 */

@ComponentConfigs(
    {
      @ComponentConfig(lifecycle = UIContainerLifecycle.class),
      @ComponentConfig(
          type = UIBreadcumbs.class, id = "BreadcumbTaxonomyTreeECMAdmin",
          template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
          events = @EventConfig(listeners = UITaxonomyTreeCreateChild.SelectPathActionListener.class)
      )
    }
)

public class UITaxonomyTreeCreateChild extends UIContainer {
  
  private String workspace;
  
  public static final String PERMISSION_ID_POPUP = "TaxonomyTreeViewPermissionPopup";
  
  private String selectedPath_ = null ;
  
  private Node taxonomyTreeNode = null;

  public UITaxonomyTreeCreateChild() throws Exception {
    addChild(UIBreadcumbs.class, "BreadcumbTaxonomyTreeECMAdmin", "BreadcumbTaxonomyTreeECMAdmin");
    addChild(UITaxonomyTreeBrowser.class, null, null);
    UITaxonomyTreeWorkingArea uiTaxonomyTreeWorkingArea = addChild(UITaxonomyTreeWorkingArea.class, null, null);
    uiTaxonomyTreeWorkingArea.setAcceptedNodeTypes(new String[] {Utils.EXO_TAXANOMY});
  }
  
  public void update() throws Exception {
    UITaxonomyTreeBrowser uiTree = getChild(UITaxonomyTreeBrowser.class);
    uiTree.update();
    UITaxonomyTreeWorkingArea uiTaxonomyTreeWorkingArea = getChild(UITaxonomyTreeWorkingArea.class);
    uiTaxonomyTreeWorkingArea.update(); 
  }
  
  public void update(String path) throws Exception {
    UITaxonomyTreeBrowser uiTree = getChild(UITaxonomyTreeBrowser.class);
    uiTree.update();
    uiTree.setNodeSelect(path);
    UITaxonomyTreeWorkingArea uiTaxonomyTreeWorkingArea = getChild(UITaxonomyTreeWorkingArea.class);
    uiTaxonomyTreeWorkingArea.setSelectedPath(path);
    uiTaxonomyTreeWorkingArea.update(); 
    setSelectedPath(path);
  }
  
  public Node getRootNode() throws Exception {
    return getTaxonomyTreeNode().getParent();
  }
  
  public Node getTaxonomyTreeNode() {
    return taxonomyTreeNode;
  }
  
  public void setSelectedPath(String selectedPath) {
    selectedPath_ = selectedPath;
  }

  public String getSelectedPath() {
    return selectedPath_;
  }
  
  public Node getNodeByPath(String path) throws Exception {
    return (Node) getSession().getItem(path) ;
  }
  
  String getRepository() throws Exception {
    return getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
  }
  
  Session getSession() throws Exception {
    String repositoryName = getRepository();
    return SessionProviderFactory.createSystemProvider().getSession(workspace,
        getRepository(repositoryName));
  }
  
  public ManageableRepository getRepository(String repositoryName) throws Exception{
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    return repositoryService.getRepository(repositoryName);
  }
  
  public void initPopup(String path) throws Exception {
    removeChildById("TaxonomyPopupCreateChild");
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "TaxonomyPopupCreateChild");
    uiPopup.setWindowSize(600, 250);
    UITaxonomyTreeCreateChildForm uiTaxoForm = createUIComponent(UITaxonomyTreeCreateChildForm.class, null, null);
    uiTaxoForm.setParent(path);
    uiPopup.setUIComponent(uiTaxoForm);
    uiPopup.setRendered(true);
    uiPopup.setShow(true);
  }

  public UIPopupContainer initPopupPermission(String id) throws Exception {
    removeChildById(id) ;
    return addChild(UIPopupContainer.class, null, id) ;
  }
  
  public void onChange(Node currentNode) throws Exception {
    UIBreadcumbs uiBreadcumbs = getChild(UIBreadcumbs.class);
    List<LocalPath> listLocalPath = new ArrayList<LocalPath>();
    String path = currentNode.getPath().trim();
    String taxonomyPath = getTaxonomyTreeNode().getPath();
    if (path.startsWith(taxonomyPath)) {
      String subTaxonomy = path.substring(taxonomyPath.length(), path.length());
      String[] arrayPath = subTaxonomy.split("/");
      if (arrayPath.length > 0) {
        for (int i = 0; i < arrayPath.length; i++) {
          if (!arrayPath[i].trim().equals("")) {
            UIBreadcumbs.LocalPath localPath1 = new UIBreadcumbs.LocalPath(arrayPath[i].trim(),
                arrayPath[i].trim());
            listLocalPath.add(localPath1);
          }
        }
      }
    } 
    uiBreadcumbs.setPath(listLocalPath);
  }
  
  public void changeGroup(String groupId, Object context) throws Exception {    
    String stringPath = getTaxonomyTreeNode().getPath() + "/";    
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
      UITaxonomyTreeBrowser uiTaxonomyTree = getChild(UITaxonomyTreeBrowser.class);
      uiTaxonomyTree.setNodeSelect(stringPath);
    }
  }
  
  public String getWorkspace() {
    return workspace;
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  public void setTaxonomyTreeNode(Node taxonomyTreeNode) {
    this.taxonomyTreeNode = taxonomyTreeNode;
  }
  
  public static class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiBreadcumbs.getParent();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiTaxonomyTreeCreateChild.changeGroup(selectGroupId, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyTreeCreateChild);
    }
  }

}
