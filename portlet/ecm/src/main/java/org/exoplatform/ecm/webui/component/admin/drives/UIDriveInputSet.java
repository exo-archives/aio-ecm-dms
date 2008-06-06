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
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
  final static public String ALLOW_CREATE_FOLDER = "allowCreateFolder" ;
  final static public String SHOW_HIDDEN_NODE = "showHiddenNode" ;
  
  public String bothLabel_ ;

  public UIDriveInputSet(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null) ;

    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).
                       addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, null)) ;  
    UIFormStringInput homePathField = new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null) ;
    homePathField.setValue("/") ;
    homePathField.setEditable(false) ;
    addUIFormInput(homePathField) ;
    addUIFormInput(new UIFormStringInput(FIELD_WORKSPACEICON, FIELD_WORKSPACEICON, null).setEditable(false)) ;
    UIFormStringInput permissonSelectField = new UIFormStringInput(FIELD_PERMISSION , FIELD_PERMISSION , null) ;
    permissonSelectField.addValidator(MandatoryValidator.class) ;
    permissonSelectField.setEditable(false) ;
    addUIFormInput(permissonSelectField) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWPREFERENCESDOC, FIELD_VIEWPREFERENCESDOC, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWNONDOC, FIELD_VIEWNONDOC, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWSIDEBAR, FIELD_VIEWSIDEBAR, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<String>(SHOW_HIDDEN_NODE, SHOW_HIDDEN_NODE, null)) ;
    List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>() ;
    folderOptions.add(new SelectItemOption<String>(FIELD_FOLDER_ONLY, Utils.NT_FOLDER)) ;
    RequestContext context = RequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    bothLabel_ = res.getString(getId() + ".label.both") ;
    folderOptions.add(new SelectItemOption<String>(FIELD_UNSTRUCTURED_ONLY, Utils.NT_UNSTRUCTURED)) ;
    folderOptions.add(new SelectItemOption<String>(bothLabel_, bothLabel_)) ;
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
    getUIFormCheckBoxInput(SHOW_HIDDEN_NODE).setChecked(false) ;
  }
}