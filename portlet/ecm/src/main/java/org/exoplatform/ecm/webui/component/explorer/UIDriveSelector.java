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
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * 10 févr. 09  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UIDriveSelector.gtmpl",
    events = {
      @EventConfig(listeners = UIDriveSelector.AddDriveActionListener.class),
      @EventConfig(listeners = UIDriveSelector.CancelActionListener.class)
    }
)
public class UIDriveSelector extends UIContainer {
  private UIPageIterator uiPageIterator_;

  public UIDriveSelector() throws Exception {
    uiPageIterator_ = addChild(UIPageIterator.class, null, "DriveSelectorList");
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  public List getListDrive() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getDrives(), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public List<String> getDrives() throws Exception {
    List<DriveData> driveList = new ArrayList<DriveData>();
    driveList = getDrives("repository");
    List<DriveData> listDriveAll = new ArrayList<DriveData>();
    List<String> listDriveNameAll = new ArrayList<String>();
    List<DriveData> generalDrives = generalDrives(driveList);
    List<DriveData> groupDrives = groupDrives(driveList);
    List<DriveData> personalDrives = personalDrives(driveList);
    listDriveAll.addAll(generalDrives);
    listDriveAll.addAll(groupDrives);
    listDriveAll.addAll(personalDrives);
    for (DriveData driveData : listDriveAll) {
      listDriveNameAll.add(driveData.getName());
    }
    return listDriveNameAll;
  }
  
  @SuppressWarnings("unused")
  public void doSelect(String selectField, Object value) throws Exception {
    System.out.println("\n\n===do select");
//    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
//    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class) ;
//    try {
//      Node currentNode = uiJCRExplorer.getCurrentNode();
//      if(currentNode.isLocked()) {
//        String lockToken = LockUtil.getLockToken(currentNode);
//        if(lockToken != null) uiJCRExplorer.getSession().addLockToken(lockToken);
//      }
//      categoriesService.addCategory(currentNode, value.toString(), uiJCRExplorer.getRepositoryName()) ;
//      uiJCRExplorer.getCurrentNode().save() ;
//      uiJCRExplorer.getSession().save() ;
//      updateGrid() ;
//      setRenderSibbling(UICategoriesAddedList.class) ;
//    } catch(Exception e) {
//      e.printStackTrace() ;
//    }
  }
  
  public List<DriveData> getDrives(String repoName) throws Exception {    
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = Utils.getMemberships();    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId != null) {
      // We will improve ManageDrive service to allow getAllDriveByUser
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        boolean flag = false;
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("${userId}")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (flag)
            continue;
          for (String rolse : userRoles) {
            if (driveData.hasPermission(allPermission, rolse)) {
              temp.add(driveData);
              break;
            }
          }
        }
      }
    } else {
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            break;
          }
        }
      }
    }
    
    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList);
    return driveList; 
  }
  
  public List<DriveData> generalDrives(List<DriveData> driveList) throws Exception {
    List<DriveData> generalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    for(DriveData drive : driveList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrives.add(drive);
      }
    }
    return generalDrives;
  }
  
  public List<DriveData> groupDrives(List<DriveData> driveList) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    List<DriveData> groupDrives = new ArrayList<DriveData>();
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    List<String> groups = Utils.getGroups();
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrives.add(drive);
            break;
          }
        }
      } 
    }
    Collections.sort(groupDrives);
    return groupDrives;
  }
  
  public List<DriveData> personalDrives(List<DriveData> driveList) {
    List<DriveData> personalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        personalDrives.add(drive);
      }
    }
    Collections.sort(personalDrives);
    return personalDrives;
  }

  static public class CancelActionListener extends EventListener<UIDriveSelector> {
    public void execute(Event<UIDriveSelector> event) throws Exception { 
      UIDriveSelector driveSelector = event.getSource();
      UIComponent uiComponent = driveSelector.getParent();
      if (uiComponent != null) {
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(((UIPopupWindow)uiComponent).getParent());
          return;
        } 
      } 
    }
  }
  
  static public class AddDriveActionListener extends EventListener<UIDriveSelector> {
    public void execute(Event<UIDriveSelector> event) throws Exception { 
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDriveSelector driveSelector = event.getSource();
      UIJcrExplorerEditContainer editContainer = driveSelector.getAncestorOfType(UIJcrExplorerEditContainer.class);
      UIJcrExplorerEditForm form = editContainer.getChild(UIJcrExplorerEditForm.class);
      UIFormInputSetWithAction driveNameInput = form.getChildById("DriveNameInput");
      driveNameInput.getUIStringInput(UIJCRExplorerPortlet.DRIVE_NAME).setValue(driveName); 
      UIComponent uiComponent = driveSelector.getParent();
      if (uiComponent != null) {
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(((UIPopupWindow)uiComponent).getParent());
          return;
        } 
      } 
    }
  }
}
