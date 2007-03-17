/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Session;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormTabPane;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 5:31:04 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIDriveForm.SaveActionListener.class),
      @EventConfig(listeners = UIDriveForm.RefreshActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddPermissionActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddPathActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddIconActionListener.class, phase = Phase.DECODE)
    }
)
public class UIDriveForm extends UIFormTabPane implements UISelector {

  private boolean isAddNew_ = true ;
  
  final static public String[] ACTIONS = {"Cancel","Refresh","Save"} ;
  final static public String POPUP_DRIVEPERMISSION = "PopupDrivePermission" ;
  
  public UIDriveForm() throws Exception {
    super("UIDriveForm", false) ;
    
    UIFormInputSet driveInputSet = new UIDriveInputSet("DriveInputSet") ;
    addUIFormInput(driveInputSet) ;

    UIFormInputSet viewInputSet = new UIViewsInputSet("ViewsInputSet") ;
    viewInputSet.setRendered(false) ;
    addUIFormInput(viewInputSet) ;
    
    setActions(ACTIONS) ;
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIDriveForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    UIDriveManager uiContainer = getAncestorOfType(UIDriveManager.class) ;
    for(UIComponent uiChild : uiContainer.getChildren()) {
      if(uiChild.getId().equals(POPUP_DRIVEPERMISSION) || uiChild.getId().equals("JCRBrowser")
          || uiChild.getId().equals("JCRBrowserAssets")) {
        UIPopupWindow uiPopup = uiContainer.getChildById(uiChild.getId()) ;
        uiPopup.setRendered(false) ;
        uiPopup.setShow(false) ;
      }
    }
  }
  
  public void refresh(String driveName) throws Exception {
    DriveData drive = null ;
    if(driveName == null) {
      isAddNew_ = true ;
    } else {
      isAddNew_ = false ;
      drive = (DriveData)getApplicationComponent(ManageDriveService.class).getDriveByName(driveName) ;
    }
    getChild(UIDriveInputSet.class).update(drive) ;
    getChild(UIViewsInputSet.class).update(drive) ;
  }
  
  static public class SaveActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource() ;
      UIDriveInputSet driveInputSet = uiDriveForm.getChild(UIDriveInputSet.class) ;
      String name = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_NAME).getValue() ;
      String workspace = 
        driveInputSet.getUIFormSelectBox(UIDriveInputSet.FIELD_WORKSPACE).getValue() ;
      String path = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_HOMEPATH).getValue() ;
      UIApplication uiApp = uiDriveForm.getAncestorOfType(UIApplication.class) ;
      try {
        RepositoryService rservice = uiDriveForm.getApplicationComponent(RepositoryService.class) ;
        Session session = rservice.getRepository().getSystemSession(workspace) ;
        session.getItem(path) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.workspace-path-invalid", null, 
                                                ApplicationMessage.ERROR)) ;
        return ;
      }      
      String icon = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_WORKSPACEICON).getValue() ;
      if (icon == null) icon = "" ;
      boolean viewReferences = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWPREFERENCESDOC).isChecked() ;
      boolean viewExplorer = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWEXPLORER).isChecked() ;
      boolean viewNonDocument = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWNONDOC).isChecked() ;
      boolean viewClipboard = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWCLIPBOARD).isChecked() ;

      UIViewsInputSet viewsInputSet = uiDriveForm.getChild(UIViewsInputSet.class) ;
      String views = viewsInputSet.getViewsSelected() ;
      
      String permissions = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_PERMISSION).getValue() ;

      ManageDriveService dservice_ = uiDriveForm.getApplicationComponent(ManageDriveService.class) ;
      if(uiDriveForm.isAddNew_ && (dservice_.getDriveByName(name) != null)) {
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.drive-exists", null, 
                                                ApplicationMessage.ERROR)) ;
        return ;
      }
      dservice_.addDrive(name, workspace, permissions, path, views, icon, viewReferences, 
                         viewNonDocument, viewExplorer, viewClipboard ) ;
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      UIDriveList uiDriveList = uiManager.getChild(UIDriveList.class) ;
      uiDriveList.updateDriveListGrid() ;
      uiDriveForm.refresh(null) ;
      UIDriveManager uiDriveManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      uiDriveManager.removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION) ;
      uiDriveManager.removeChildById("DriveManagerPopup") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource() ;
      uiDriveForm.refresh(null) ;
      UIDriveManager uiDriveManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      uiDriveManager.removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION) ;
      uiDriveManager.removeChildById("DriveManagerPopup") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager) ;
    }
  }
  
  static  public class RefreshActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      event.getSource().refresh(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource() ;
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      uiManager.initPopupPermission() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class AddPathActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource() ;
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      uiManager.initPopupJCRBrowser() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class AddIconActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource() ;
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class) ;
      uiManager.initPopupJCRBrowserAssets() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}