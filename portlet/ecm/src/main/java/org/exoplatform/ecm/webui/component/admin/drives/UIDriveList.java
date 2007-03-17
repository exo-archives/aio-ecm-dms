/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import java.util.List;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.drives.ManageDriveService;
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
      @EventConfig(listeners = UIDriveList.DeleteActionListener.class),
      @EventConfig(listeners = UIDriveList.EditInfoActionListener.class),
      @EventConfig(listeners = UIDriveList.AddDriveActionListener.class)
    }
)
public class UIDriveList extends UIGrid {
  
  final static public String[] ACTIONS = {"AddDrive"} ;
  
  private static String[] DRIVE_BEAN_FIELD = {"name", "workspace", "homePath", "permissions", "views"} ;
  private static String[] DRIVE_ACTION = {"EditInfo", "Delete"} ;
  private ManageDriveService dservice ;
  
  public UIDriveList() throws Exception {
    configure("name", DRIVE_BEAN_FIELD, DRIVE_ACTION) ;
    dservice = getApplicationComponent(ManageDriveService.class) ;
    updateDriveListGrid() ;
  }
  
  public String[] getActions() { return ACTIONS ; }
  
  public void updateDriveListGrid() throws Exception {
    List drives = dservice.getAllDrives() ;
    ObjectPageList objPageList = new ObjectPageList(drives, 10) ;
    getUIPageIterator().setPageList(objPageList) ;    
  }
  
  static  public class AddDriveActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      UIDriveManager uiDriveManager = event.getSource().getParent() ;
      uiDriveManager.initPopup() ;
      UIDriveForm uiForm = uiDriveManager.findFirstComponentOfType(UIDriveForm.class) ;
      uiForm.refresh(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIDriveList uiDriveList = event.getSource();
      uiDriveList.dservice.removeDrive(name) ;
      uiDriveList.updateDriveListGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveList.getParent()) ;
    }
  }

  static  public class EditInfoActionListener extends EventListener<UIDriveList> {
    public void execute(Event<UIDriveList> event) throws Exception {
      UIDriveManager uiDriveManager = event.getSource().getParent() ;
      uiDriveManager.initPopup() ;
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiDriveManager.findFirstComponentOfType(UIDriveForm.class).refresh(driveName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
}
