/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 23, 2006
 * 11:39:49 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
      @EventConfig(listeners = UIDriveList.DeleteActionListener.class, confirm = "UIDriveList.msg.confirm-delete"),
      @EventConfig(listeners = UIDriveList.EditInfoActionListener.class),
      @EventConfig(listeners = UIDriveList.AddDriveActionListener.class)
    }
)
public class UIDriveList extends UIGrid {
  
  final static public String[] ACTIONS = {"AddDrive"} ;
  final  static public String ST_ADD = "AddDriveManagerPopup" ;
  final  static public String ST_EDIT = "EditDriveManagerPopup" ;
  private static String[] DRIVE_BEAN_FIELD = {"icon", "name", "workspace", "homePath", "permissions", "views"} ;
  private static String[] DRIVE_ACTION = {"EditInfo", "Delete"} ;
  public UIDriveList() throws Exception {
    configure("name", DRIVE_BEAN_FIELD, DRIVE_ACTION) ;
    updateDriveListGrid() ;
  }
  
  public String[] getActions() { return ACTIONS ; }
  
  public void updateDriveListGrid() throws Exception {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class) ;
    ManageableRepository repository = rservice.getRepository() ;
    Session digitalSession = repository.getSystemSession("digital-assets") ;
    
    List drives = driveService.getAllDrives() ;
    for(int i = 0; i < drives.size(); i++) {
      DriveData drive = (DriveData)drives.get(i) ;
      if(drive.getIcon() != null && drive.getIcon().length() > 0) {
        Node node = (Node) digitalSession.getItem(drive.getIcon()) ;
        Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
        InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, "image") ;
        dresource.setDownloadName(node.getName()) ;
        drive.setIcon("<img src=\"" + dservice.getDownloadLink(dservice.addDownloadResource(dresource)) + "\" width=\"16\" height=\"16\" />") ;
      }
    }
    ObjectPageList objPageList = new ObjectPageList(drives, 10) ;
    getUIPageIterator().setPageList(objPageList) ;    
  }
  
  static  public class AddDriveActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      UIDriveManager uiDriveManager = event.getSource().getParent() ;
      uiDriveManager.removeChildById(UIDriveList.ST_EDIT);
      uiDriveManager.initPopup(UIDriveList.ST_ADD) ;
      UIDriveForm uiForm = uiDriveManager.findFirstComponentOfType(UIDriveForm.class) ;
      uiForm.refresh(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIDriveList uiDriveList = event.getSource();
      ManageDriveService driveService = uiDriveList.getApplicationComponent(ManageDriveService.class) ;
      driveService.removeDrive(name) ;
      uiDriveList.updateDriveListGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveList.getParent()) ;
    }
  }

  static  public class EditInfoActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      UIDriveManager uiDriveManager = event.getSource().getParent() ;
      uiDriveManager.removeChildById(UIDriveList.ST_ADD);
      uiDriveManager.initPopup(UIDriveList.ST_EDIT) ;
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiDriveManager.findFirstComponentOfType(UIDriveForm.class).refresh(driveName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
}
