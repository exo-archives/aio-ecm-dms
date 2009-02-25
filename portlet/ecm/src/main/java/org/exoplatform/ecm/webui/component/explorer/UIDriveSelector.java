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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
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
    ObjectPageList objPageList = new ObjectPageList(getDrives("repository"), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public List<String> getDrives(String repoName) throws Exception {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class) ;
    ManageableRepository repository = rservice.getRepository(repoName) ;  
    List<DriveData> driveList = new ArrayList<DriveData>() ;
    Session session = null ;
    List<DriveData> drives = driveService.getAllDrives(repoName) ;
    if(drives != null && drives.size() > 0) {
      for(DriveData drive : drives) {
        if(drive.getIcon() != null && drive.getIcon().length() > 0) {
          try {
            String[] iconPath = drive.getIcon().split(":/") ;   
            session = repository.getSystemSession(iconPath[0]) ;
            Node node = (Node) session.getItem("/" + iconPath[1]) ;
            Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
            InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
            InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, "image") ;
            dresource.setDownloadName(node.getName()) ;
            drive.setIcon(dservice.getDownloadLink(dservice.addDownloadResource(dresource))) ;
            session.logout() ;
          } catch(PathNotFoundException pnf) {
            drive.setIcon("") ;
          }
        }
        if(isExistWorspace(repository, drive)) driveList.add(drive) ;
      }
    }
    List<String> driveListName = new ArrayList<String>();
    for (DriveData driveData : driveList) {
      driveListName.add(driveData.getName());
    }
    Collections.sort(driveListName) ;
    return driveListName ; 
  }
  
  private boolean isExistWorspace(ManageableRepository repository, DriveData drive) {
    for (String ws:  repository.getWorkspaceNames()) {
      if (ws.equals(drive.getWorkspace())) return true;
    }
    return false;
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
