/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIFormInputSetWithAction.gtmpl")
public class UIDriveInputSet extends UIFormInputSetWithAction {
  final static public String FIELD_NAME = "name" ;
  final static public String FIELD_WORKSPACE = "workspace" ;
  final static public String FIELD_HOMEPATH = "homePath" ;
  final static public String FIELD_WORKSPACEICON = "icon" ;
  final static public String FIELD_PERMISSION = "permissions" ;
  
  final static public String FIELD_VIEWPREFERENCESDOC = "viewPreferences" ;
  final static public String FIELD_VIEWNONDOC = "viewNonDocument" ;
  final static public String FIELD_VIEWSIDEBAR = "viewSideBar" ;
  final static public String FIELD_FOLDER_ONLY = "Folder" ;
  final static public String FIELD_UNSTRUCTURED_ONLY = "Unstructured folder" ;
  final static public String FIELD_BOTH_FOLDER_UNSTRUCTURED = "Both" ;
  final static public String ALLOW_CREATE_FOLDER = "allowCreateFolder" ;

  public UIDriveInputSet(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null) ;

    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).
                       addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, null)) ;    
    addUIFormInput(new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_WORKSPACEICON, FIELD_WORKSPACEICON, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PERMISSION , FIELD_PERMISSION , null).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWPREFERENCESDOC, FIELD_VIEWPREFERENCESDOC, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWNONDOC, FIELD_VIEWNONDOC, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWSIDEBAR, FIELD_VIEWSIDEBAR, null)) ;
    List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>() ;
    folderOptions.add(new SelectItemOption<String>(FIELD_FOLDER_ONLY, Utils.NT_FOLDER)) ;
    folderOptions.add(new SelectItemOption<String>(FIELD_UNSTRUCTURED_ONLY, Utils.NT_UNSTRUCTURED)) ;
    folderOptions.add(new SelectItemOption<String>(FIELD_BOTH_FOLDER_UNSTRUCTURED, "both")) ;
    addUIFormInput(new UIFormRadioBoxInput(ALLOW_CREATE_FOLDER, ALLOW_CREATE_FOLDER, folderOptions).
                   setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;
    setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"}) ;
    setActionInfo(FIELD_HOMEPATH, new String[] {"AddPath"}) ;
    setActionInfo(FIELD_WORKSPACEICON, new String[] {"AddIcon"}) ;
  }

  public void update(DriveData drive) throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    String[] wsNames = getApplicationComponent(RepositoryService.class)
                      .getRepository(repository).getWorkspaceNames();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
    for(String wsName : wsNames) {
      workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
    }
    getUIFormSelectBox(FIELD_WORKSPACE).setOptions(workspace) ;
    if(drive != null) {
      invokeGetBindingField(drive) ;
      getUIStringInput(FIELD_NAME).setEditable(false) ;
      return ;
    }
    getUIStringInput(FIELD_NAME).setEditable(true) ;
    reset() ;
    getUIFormCheckBoxInput(FIELD_VIEWPREFERENCESDOC).setChecked(false) ;
    getUIFormCheckBoxInput(FIELD_VIEWNONDOC).setChecked(false) ;
    getUIFormCheckBoxInput(FIELD_VIEWSIDEBAR).setChecked(false) ;
  }
}