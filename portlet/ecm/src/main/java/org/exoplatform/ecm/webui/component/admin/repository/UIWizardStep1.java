/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 8, 2007  
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIFormInputSetWithAction.gtmpl")
public class UIWizardStep1 extends UIFormInputSetWithAction {
  final static public String FIELD_NAME = "name" ;  
  final static public String FIELD_NODETYPE = "autoInitializedRootNt" ;
  final static public String FIELD_PERMISSION = "permission" ;
  final static public String FIELD_TIMEOUT = "setLockTimeOut" ;
  final static public String FIELD_ISDEFAULT = "isDefault" ;
  private Map<String, String> permissions_ = new HashMap<String, String>() ;

  public UIWizardStep1(String name) throws Exception {
    super(name);
    addChild(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(ECMNameValidator.class)) ;
    addChild(new UIFormSelectBox(FIELD_NODETYPE, FIELD_NODETYPE, getNodeType())) ;
    UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(FIELD_ISDEFAULT, FIELD_ISDEFAULT, null) ;
    addChild(checkbox) ;
    addUIFormInput(new UIFormInputInfo(FIELD_PERMISSION, FIELD_PERMISSION, null)) ;
    String[] actionInfor = {"EditPermission", "RemovePermission"} ;
    setActionInfo(FIELD_PERMISSION, actionInfor) ;
    setFieldActions(FIELD_PERMISSION, new String[]{"AddPermission"}) ;
    showActionInfo(true) ;
    addChild(new UIFormStringInput(FIELD_TIMEOUT, FIELD_TIMEOUT, null).addValidator(EmptyFieldValidator.class).
        addValidator(NumberFormatValidator.class)) ; 
  }
  private List<SelectItemOption<String>>  getNodeType() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.NT_UNSTRUCTURED, Utils.NT_UNSTRUCTURED)) ;
    options.add(new SelectItemOption<String>(Utils.NT_FOLDER, Utils.NT_FOLDER)) ;
    return options ;
  }

  protected void setFieldName(String name) {
    getUIStringInput(FIELD_NAME).setValue(name) ;
  }
  protected String getFieldName() {return getUIStringInput(FIELD_NAME).getValue();}

  protected String getFieldNodeType() {return getUIFormSelectBox(FIELD_NODETYPE).getValue();}
  protected void setFieldNodeTypeSelected(String selectedValue) {
    getUIFormSelectBox(UIWizardStep1.FIELD_NODETYPE).setValue(selectedValue) ;
  }
  protected void setFieldNodeType(List<SelectItemOption<String>> options) {
    getUIFormSelectBox(UIWizardStep1.FIELD_NODETYPE).setOptions(options) ;
  }
  protected boolean getFieldDefault(){ return getUIFormCheckBoxInput(FIELD_ISDEFAULT).isChecked() ;}
  protected void setFieldDefault(boolean isDefault) {
    getUIFormCheckBoxInput(FIELD_ISDEFAULT).setChecked(isDefault) ;
  }

  protected void setFieldLockTime(String lockTime) {
    getUIStringInput(UIWizardStep1.FIELD_TIMEOUT).setValue(lockTime) ;
  }
  protected String getFieldLockTime() {return getUIStringInput(UIWizardStep1.FIELD_TIMEOUT).getValue() ;}

  protected void resetFields() {
    reset() ;
    setFieldDefault(false) ;
    setFieldNodeType(getNodeType()) ;
  } 
  protected void fillFields(String name, String selectedNodeType, boolean isDefaultWS, String permission, String lockTime) {
    setFieldName(name);
    setPermissionMap(permission) ;
    refreshPermissionList() ;
    setFieldDefault(isDefaultWS) ;
    setFieldNodeTypeSelected(selectedNodeType) ;
    setFieldLockTime(lockTime) ;
  }

  protected void lockFields(boolean isLock) {
    boolean isEdiable = !isLock ;
    setIsView(isLock) ;
    getUIStringInput(FIELD_NAME).setEditable(isEdiable) ;
    getUIFormSelectBox(FIELD_NODETYPE).setEnable(isEdiable) ;
    getUIFormCheckBoxInput(FIELD_ISDEFAULT).setEnable(isEdiable) ;
    getUIStringInput(FIELD_TIMEOUT).setEditable(isEdiable) ;
    showActionInfo(isEdiable) ;
  }
  public void setPermissionMap(String permission) {
    if (permission != null) {
      List<String> userList = new ArrayList<String>() ;
      for(String perm : permission.split(";")) {
        String userName = perm.substring(0,perm.lastIndexOf(" ")) ;
        if(!userList.contains(userName)) userList.add(userName) ;      
      }
      for(String user : userList) {
        StringBuilder sb = new StringBuilder() ;
        for(String perm : permission.split(";")) {
          if(perm.contains(user)) {
            if(sb.length() > 1) sb.append(";") ;
            sb.append(perm) ;
          }
        }
        permissions_.put(user, sb.toString()) ;
      }
    }
  }
  public void refreshPermissionList() {
    StringBuilder labels = new StringBuilder() ;
    for(String perm : permissions_.keySet()){
      if(labels.length() > 0) labels.append(",") ;
      labels.append(perm) ;
    }
    setInfoField(FIELD_PERMISSION, labels.toString()) ;
  }
  protected void addPermissions(String user, String permissions) {
    permissions_.put(user, permissions) ;
  }
  protected void removePermission(String perm) {
    permissions_.remove(perm) ;
  }  
  protected boolean isPermissionEmpty() {return permissions_.isEmpty();}
  protected Map<String, String> getPermissions() {return permissions_ ;}
}
