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
import java.util.Set;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */
@ComponentConfig(template = "classpath:groovy/ecm/webui/form/UIFormInputSetWithAction.gtmpl")
public class UIDriveInputSet extends UIFormInputSetWithAction {
  final static public String FIELD_NAME = "name";
  final static public String FIELD_WORKSPACE = "workspace";
  final static public String FIELD_HOMEPATH = "homePath";
  final static public String FIELD_WORKSPACEICON = "icon";
  final static public String FIELD_PERMISSION = "permissions";
  
  final static public String FIELD_VIEWPREFERENCESDOC = "viewPreferences";
  final static public String FIELD_VIEWNONDOC = "viewNonDocument";
  final static public String FIELD_VIEWSIDEBAR = "viewSideBar";
  final static public String FIELD_FOLDER_ONLY = "Folder";
  final static public String FIELD_BOTH = "Both";
  final static public String FIELD_UNSTRUCTURED_ONLY = "Unstructured folder";
  final static public String FIELD_ALLOW_CREATE_FOLDERS = "allowCreateFolders";
  final static public String SHOW_HIDDEN_NODE = "showHiddenNode";
  
  public String bothLabel_;
  public String folderOnlyLabel_;
  public String unstructuredFolderLabel_;

  public UIDriveInputSet(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null);

    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).
                       addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, null));  
    UIFormStringInput homePathField = new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null);
    homePathField.setValue("/");
    homePathField.setEditable(false);
    addUIFormInput(homePathField);
    addUIFormInput(new UIFormStringInput(FIELD_WORKSPACEICON, FIELD_WORKSPACEICON, null).setEditable(false));
    UIFormStringInput permissonSelectField = new UIFormStringInput(FIELD_PERMISSION , FIELD_PERMISSION , null);
    permissonSelectField.addValidator(MandatoryValidator.class);
    permissonSelectField.setEditable(true);
    addUIFormInput(permissonSelectField);
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWPREFERENCESDOC, FIELD_VIEWPREFERENCESDOC, null));
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWNONDOC, FIELD_VIEWNONDOC, null));
    addUIFormInput(new UIFormCheckBoxInput<String>(FIELD_VIEWSIDEBAR, FIELD_VIEWSIDEBAR, null));
    addUIFormInput(new UIFormCheckBoxInput<String>(SHOW_HIDDEN_NODE, SHOW_HIDDEN_NODE, null));
    List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>();
    
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    bothLabel_ = res.getString(getId() + ".label.both");
    folderOnlyLabel_ = res.getString(getId() + ".label.folderOnly");
    unstructuredFolderLabel_ = res.getString(getId() + ".label.unstructuredFolder");
    folderOptions.add(new SelectItemOption<String>(folderOnlyLabel_, Utils.NT_FOLDER));
    folderOptions.add(new SelectItemOption<String>(unstructuredFolderLabel_, Utils.NT_UNSTRUCTURED));
    folderOptions.add(new SelectItemOption<String>(bothLabel_, FIELD_BOTH));
    addUIFormInput(new UIFormSelectBox(FIELD_ALLOW_CREATE_FOLDERS, FIELD_ALLOW_CREATE_FOLDERS, null));
    setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"});
    setActionInfo(FIELD_HOMEPATH, new String[] {"AddPath"});
    setActionInfo(FIELD_WORKSPACEICON, new String[] {"AddIcon"});
  }

  public void update(DriveData drive) throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository();
    String[] wsNames = getApplicationComponent(RepositoryService.class)
                      .getRepository(repository).getWorkspaceNames();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    Set<String> setFoldertypes = templateService.getAllowanceFolderType(repository);
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>();
    
    List<SelectItemOption<String>> foldertypeOptions = new ArrayList<SelectItemOption<String>>();
    for(String wsName : wsNames) {
      workspace.add(new SelectItemOption<String>(wsName,  wsName));
    }
    
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    
    for(String foldertype : setFoldertypes) {
      foldertypeOptions.add(new SelectItemOption<String>(res.getString(getId() + ".label." + foldertype.replace(":", "_")),  foldertype));
    }
    getUIFormSelectBox(FIELD_WORKSPACE).setOptions(workspace);
    getUIFormSelectBox(FIELD_ALLOW_CREATE_FOLDERS).setOptions(foldertypeOptions);
    getUIFormSelectBox(FIELD_ALLOW_CREATE_FOLDERS).setMultiple(true);
    if(drive != null) {
      invokeGetBindingField(drive);
      //Set value for multi-value select box
      String foldertypes = drive.getAllowCreateFolders();
      String selectedFolderTypes[];
      if (foldertypes.contains(",")) {
        selectedFolderTypes = foldertypes.split(",");
      } else {
        selectedFolderTypes = new String[] {foldertypes};
      }
      getUIFormSelectBox(FIELD_ALLOW_CREATE_FOLDERS).setSelectedValues(selectedFolderTypes);
      getUIStringInput(FIELD_NAME).setEditable(false);
      return;
    }
    getUIStringInput(FIELD_NAME).setEditable(true);
    reset();
    getUIFormCheckBoxInput(FIELD_VIEWPREFERENCESDOC).setChecked(false);
    getUIFormCheckBoxInput(FIELD_VIEWNONDOC).setChecked(false);
    getUIFormCheckBoxInput(FIELD_VIEWSIDEBAR).setChecked(false);
    getUIFormCheckBoxInput(SHOW_HIDDEN_NODE).setChecked(false);
  }
}