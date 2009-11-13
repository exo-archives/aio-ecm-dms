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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 5:31:04 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIDriveForm.SaveActionListener.class),
      @EventConfig(listeners = UIDriveForm.RefreshActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddPermissionActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddPathActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.AddIconActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDriveForm.ChangeActionListener.class, phase = Phase.DECODE)
    }
)
public class UIDriveForm extends UIFormTabPane implements UISelectable {

  private boolean isAddNew_ = true;  
  final static public String[] ACTIONS = {"Save", "Refresh", "Cancel"};
  final static public String POPUP_DRIVEPERMISSION = "PopupDrivePermission";
  private String membershipString = "";

  public UIDriveForm() throws Exception {
    super("UIDriveForm");

    UIFormInputSet driveInputSet = new UIDriveInputSet("DriveInputSet");
    UIFormSelectBox selectBox = driveInputSet.getChildById(UIDriveInputSet.FIELD_WORKSPACE);
    selectBox.setOnChange("Change");
    addUIFormInput(driveInputSet);
    setSelectedTab(driveInputSet.getId());
    UIFormInputSet viewInputSet = new UIViewsInputSet("ViewsInputSet");
    addUIFormInput(viewInputSet);    
    setActions(ACTIONS);
  }

  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIDriveForm.label." + id);
    } catch (MissingResourceException ex) {
      return id;
    }    
  }

  public void doSelect(String selectField, Object value) {
    UIFormStringInput uiStringInput = getUIStringInput(selectField);
    if (selectField.equals(UIDriveInputSet.FIELD_PERMISSION)){
      String membership = value.toString(); 
      String valuePermissions = uiStringInput.getValue(); 
      if ((membership != null) && (valuePermissions.indexOf(membership) < 0)){
        if (valuePermissions.length() > 0)
          valuePermissions += "," + membership;
        else
          valuePermissions += membership;
      }        
      uiStringInput.setValue(valuePermissions);
    } else {
      uiStringInput.setValue(value.toString());
    }
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
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    DriveData drive = null;
    if(driveName == null) {
      isAddNew_ = true;
    } else {
      isAddNew_ = false;
      setActions(new String[] {"Save", "Cancel"});
      drive = getApplicationComponent(ManageDriveService.class).getDriveByName(driveName, repository);
    }
    getChild(UIDriveInputSet.class).update(drive);
    getChild(UIViewsInputSet.class).update(drive);
  }

  static public class SaveActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      String repository = uiDriveForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
      RepositoryService rservice = uiDriveForm.getApplicationComponent(RepositoryService.class);
      UIDriveInputSet driveInputSet = uiDriveForm.getChild(UIDriveInputSet.class);
      UIApplication uiApp = uiDriveForm.getAncestorOfType(UIApplication.class);
      String name = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_NAME).getValue();
      if(name == null || name.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.name-null", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] arrFilterChar = {"&", "$", "@", "'", ":","]", "[", "*", "%", "!"};
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.fileName-invalid", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
      String workspace = 
        driveInputSet.getUIFormSelectBox(UIDriveInputSet.FIELD_WORKSPACE).getValue();
      String path = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_HOMEPATH).getValue();
      if((path == null)||(path.trim().length() == 0)) path = "/";
      Session session = null;
      try {        
        session = rservice.getRepository(repository).getSystemSession(workspace);
        String userId = Util.getPortalRequestContext().getRemoteUser();
        String pathReal = path.replace("${userId}", userId);
        session.getItem(pathReal);
        session.logout();
      } catch(Exception e) {
        if(session!=null) {
          session.logout();
        }
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.workspace-path-invalid", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      boolean viewReferences = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWPREFERENCESDOC).isChecked();
      boolean viewSideBar = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWSIDEBAR).isChecked();
      boolean showHiddenNode = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.SHOW_HIDDEN_NODE).isChecked();      
      boolean viewNonDocument = 
        driveInputSet.getUIFormCheckBoxInput(UIDriveInputSet.FIELD_VIEWNONDOC).isChecked();
      String allowCreateFolder =  driveInputSet.<UIFormRadioBoxInput>getUIInput(UIDriveInputSet.ALLOW_CREATE_FOLDER).getValue();
      UIViewsInputSet viewsInputSet = uiDriveForm.getChild(UIViewsInputSet.class);
      String views = viewsInputSet.getViewsSelected();      
      String permissions = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_PERMISSION).getValue();
      if(permissions == null || permissions.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-null", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      OrganizationService oservice = uiDriveForm.getApplicationComponent(OrganizationService.class);
      String[] arrPermissions = permissions.split(",");
      List<String> listMemberhip;
      Collection<?> collection = oservice.getMembershipTypeHandler().findMembershipTypes();
      listMemberhip  = new ArrayList<String>(5);
      for(Object obj : collection){
        listMemberhip.add(((MembershipType)obj).getName());
      }
      listMemberhip.add("*");
      for(String itemPermission : arrPermissions) {
        if(itemPermission.length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-path-invalid", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        } 
        if (itemPermission.contains(":")) {
          String[] permission = itemPermission.split(":");
          if((permission[0] == null) || (permission[0].length() == 0)){
            uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-path-invalid", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } else if(!listMemberhip.contains(permission[0])) {
            uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-path-invalid", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          if((permission[1] == null) || (permission[1].length() == 0)) {
            uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-path-invalid", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } else if(oservice.getGroupHandler().findGroupById(permission[1]) == null){
            uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.permission-path-invalid", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return;
          }
        }        
      }
      
      ManageDriveService dservice_ = uiDriveForm.getApplicationComponent(ManageDriveService.class);
      if(uiDriveForm.isAddNew_ && (dservice_.getDriveByName(name, repository) != null)) {
        uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.drive-exists", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String iconPath = driveInputSet.getUIStringInput(UIDriveInputSet.FIELD_WORKSPACEICON).getValue();
      if(iconPath != null && iconPath.trim().length() > 0) {        
        Session jcrSession = null;
        try {
          if(iconPath.indexOf(":/") > -1) {
            String[] paths = iconPath.split(":/");
            jcrSession = rservice.getRepository(repository).getSystemSession(paths[0]);
            jcrSession.getItem("/" + paths[1]);
            jcrSession.logout();
          }
        } catch(Exception e) {
          if(jcrSession != null) {
            jcrSession.logout();
          }
          uiApp.addMessage(new ApplicationMessage("UIDriveForm.msg.icon-not-found", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }  
      } else {
        iconPath = "";
      }
      dservice_.addDrive(name, workspace, permissions, path, views, iconPath, viewReferences, 
          viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder);
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      UIDriveList uiDriveList = uiManager.getChild(UIDriveList.class);
      uiDriveList.updateDriveListGrid(uiDriveList.getUIPageIterator().getCurrentPage());
      uiDriveForm.refresh(null);
      UIDriveManager uiDriveManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      uiDriveManager.removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION);
      uiDriveManager.removeChildById(UIDriveList.ST_ADD);
      uiDriveManager.removeChildById(UIDriveList.ST_EDIT);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager);
    }
  }

  static  public class CancelActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      uiDriveForm.refresh(null);
      UIDriveManager uiDriveManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      uiDriveManager.removeChildById(UIDriveForm.POPUP_DRIVEPERMISSION);
      uiDriveManager.removeChildById(UIDriveList.ST_ADD);
      uiDriveManager.removeChildById(UIDriveList.ST_EDIT);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDriveManager);
    }
  }

  static  public class RefreshActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      event.getSource().refresh(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }

  static public class AddPermissionActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      String membership = uiDriveForm.getUIStringInput(UIDriveInputSet.FIELD_PERMISSION).getValue();
      if ((membership != null) && (uiDriveForm.membershipString.indexOf(membership) < 0)){
        if (uiDriveForm.membershipString.length() > 0)
          uiDriveForm.membershipString += "," + membership;
        else
          uiDriveForm.membershipString += membership;
      }        
      uiDriveForm.getUIStringInput(UIDriveInputSet.FIELD_PERMISSION).setValue(uiDriveForm.membershipString);
      
      uiManager.initPopupPermission(uiDriveForm.membershipString);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }

  static public class AddPathActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      UIDriveInputSet driveInputSet = uiDriveForm.getChild(UIDriveInputSet.class);
      String workspace = 
        driveInputSet.getUIFormSelectBox(UIDriveInputSet.FIELD_WORKSPACE).getValue();
      uiManager.initPopupJCRBrowser(workspace, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }

  static public class AddIconActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      UIDriveManager uiManager = uiDriveForm.getAncestorOfType(UIDriveManager.class);
      UIDriveInputSet driveInputSet = uiDriveForm.getChild(UIDriveInputSet.class);
      String workspace = 
        driveInputSet.getUIFormSelectBox(UIDriveInputSet.FIELD_WORKSPACE).getValue();
      uiManager.initPopupJCRBrowserAssets(workspace);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }

  static public class ChangeActionListener extends EventListener<UIDriveForm> {
    public void execute(Event<UIDriveForm> event) throws Exception {
      UIDriveForm uiDriveForm = event.getSource();
      String driverName = uiDriveForm.getUIStringInput(UIDriveInputSet.FIELD_NAME).getValue();
      String repository = uiDriveForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
      String selectedWorkspace = uiDriveForm.getUIStringInput(UIDriveInputSet.FIELD_WORKSPACE).getValue();
      UIDriveInputSet driveInputSet = uiDriveForm.getChild(UIDriveInputSet.class);
      ManageDriveService manageDriveService = 
        uiDriveForm.getApplicationComponent(ManageDriveService.class);
      RepositoryService repositoryService = 
        uiDriveForm.getApplicationComponent(RepositoryService.class);
      List<WorkspaceEntry> wsEntries = 
        repositoryService.getRepository(repository).getConfiguration().getWorkspaceEntries();
      String wsInitRootNodeType = null;
      for(WorkspaceEntry wsEntry : wsEntries) {
        if(wsEntry.getName().equals(selectedWorkspace)) {
          wsInitRootNodeType = wsEntry.getAutoInitializedRootNt();
        }
      }
      List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>();
      UIFormRadioBoxInput uiInput = driveInputSet.<UIFormRadioBoxInput>getUIInput(UIDriveInputSet.ALLOW_CREATE_FOLDER);
      if(wsInitRootNodeType != null && wsInitRootNodeType.equals(Utils.NT_FOLDER)) {
        folderOptions.add(new SelectItemOption<String>(UIDriveInputSet.FIELD_FOLDER_ONLY, Utils.NT_FOLDER));
      } else {
        folderOptions.add(new SelectItemOption<String>(driveInputSet.folderOnlyLabel_, Utils.NT_FOLDER));
        folderOptions.add(new SelectItemOption<String>(driveInputSet.unstructuredFolderLabel_, Utils.NT_UNSTRUCTURED));
        folderOptions.add(new SelectItemOption<String>(driveInputSet.bothLabel_, UIDriveInputSet.FIELD_BOTH));
      }
      uiInput.setOptions(folderOptions);
      if(!uiDriveForm.isAddNew_) {
        DriveData drive = manageDriveService.getDriveByName(driverName, repository);
        String defaultPath = drive.getHomePath();
        if(!drive.getWorkspace().equals(selectedWorkspace)) defaultPath = "/";
        uiDriveForm.getUIStringInput(UIDriveInputSet.FIELD_HOMEPATH).setValue(defaultPath);
      }
    }
  }
}