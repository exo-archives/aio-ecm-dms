/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 5:31:04 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIMetadataForm.SaveActionListener.class),
      @EventConfig(listeners = UIMetadataForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMetadataForm.AddPermissionActionListener.class, phase = Phase.DECODE)
    }
)

public class UIMetadataForm extends UIForm implements UISelector{

  final static public String METADATA_PATH = "metadataPath" ;
  final static public String MAPPING = "mapping" ;
  final static public String METADATA_MAPPING = "metadataMapping" ;
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String DIALOG_TEMPLATE = "dialogTemplate" ;
  final static public String VIEW_TEMPLATE = "viewTemplate" ;
  final static public String MIXIN_TYPES = "mixinTypes" ;
  final static public String VIEW_PERMISSION = "viewPermission" ;
  
  private boolean isAddNew_ = true ;
  private String metadataName_ ;

  public UIMetadataForm() throws Exception {
    addUIFormInput(new UIFormTextAreaInput(DIALOG_TEMPLATE, DIALOG_TEMPLATE, null)) ;
    addUIFormInput(new UIFormTextAreaInput(VIEW_TEMPLATE, VIEW_TEMPLATE, null)) ;
    addUIFormInput(new UIFormSelectBox(MIXIN_TYPES,MIXIN_TYPES, null)) ;
    UIFormInputSetWithAction permissionInput = new UIFormInputSetWithAction("permission") ;
    permissionInput.addUIFormInput(new UIFormStringInput(VIEW_PERMISSION, VIEW_PERMISSION, null).addValidator(EmptyFieldValidator.class)) ;
    permissionInput.setActionInfo(VIEW_PERMISSION, new String[] {"AddPermission"}) ;
    addUIComponentInput(permissionInput) ;
    setActions(new String[] {"Save", "Cancel"}) ;
  }

  public void update(List metadataList){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(int i = 0; i < metadataList.size(); i ++ ) {
      String ntName = metadataList.get(i).toString() ;
      options.add(new SelectItemOption<String>(ntName, ntName)) ;
    }
    isAddNew_ = true ;
    getUIFormTextAreaInput(DIALOG_TEMPLATE).setValue("") ;
    getUIFormTextAreaInput(VIEW_TEMPLATE).setValue("") ;
    getUIFormSelectBox(MIXIN_TYPES).setOptions(options) ;
    getUIFormSelectBox(MIXIN_TYPES).setEditable(true) ;
  } 
  
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    getUIStringInput(VIEW_PERMISSION).setValue(value) ;
    UIMetadataManager uiManager = getAncestorOfType(UIMetadataManager.class) ;
    uiManager.removeChildById(UIMetadataManager.PERMISSION_POPUP) ;
  }
  
  public void update(String metadata)throws Exception{
    metadataName_ = metadata ;
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(metadata, metadata)) ;
    getUIFormSelectBox(MIXIN_TYPES).setOptions(options) ;
    getUIFormSelectBox(MIXIN_TYPES).setValue(metadata) ;
    String dialogTemplate = metadataService.getMetadataTemplate(metadata, true) ;
    String viewTemplate = metadataService.getMetadataTemplate(metadata, false) ;
    String role = metadataService.getMetadataRoles(metadata, true) ;
    isAddNew_ = false ;
    getUIFormSelectBox(MIXIN_TYPES).setDisabled(true) ;
    getUIStringInput(VIEW_PERMISSION).setValue(role) ;
    getUIFormTextAreaInput(DIALOG_TEMPLATE).setValue(dialogTemplate) ;
    getUIFormTextAreaInput(VIEW_TEMPLATE).setValue(viewTemplate) ;
  }
  
  static public class SaveActionListener extends EventListener<UIMetadataForm> {
    public void execute(Event<UIMetadataForm> event) throws Exception {
      UIMetadataForm uiForm = event.getSource();      
      UIMetadataManager uiMetaManager = uiForm.getAncestorOfType(UIMetadataManager.class) ;
      MetadataService metadataService = uiForm.getApplicationComponent(MetadataService.class) ;
      String ntName ;
      if(uiForm.isAddNew_) ntName = uiForm.getUIFormSelectBox(MIXIN_TYPES).getValue() ;
      else ntName = uiForm.metadataName_ ;
      String roles = uiForm.getUIStringInput(VIEW_PERMISSION).getValue() ;
      String dialogTemplate = uiForm.getUIFormTextAreaInput(DIALOG_TEMPLATE).getValue() ;
      if(dialogTemplate == null) dialogTemplate = "" ;
      String viewTemplate = uiForm.getUIFormTextAreaInput(VIEW_TEMPLATE).getValue() ;
      if(viewTemplate == null) viewTemplate = "" ;
      metadataService.addMetadata(ntName, true, roles, dialogTemplate, uiForm.isAddNew_) ;
      metadataService.addMetadata(ntName, false, roles, viewTemplate, uiForm.isAddNew_) ;
      uiMetaManager.getChild(UIMetadataList.class).updateGrid() ;
      uiForm.reset() ;
      uiMetaManager.getChild(UIMetadataList.class).updateGrid() ;
      uiMetaManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMetaManager) ;
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UIMetadataForm> {
    public void execute(Event<UIMetadataForm> event) throws Exception {
      UIMetadataForm uiView = event.getSource() ;
      UIMetadataManager uiManager = uiView.getAncestorOfType(UIMetadataManager.class) ;
      uiManager.initPopupPermission() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIMetadataForm> {
    public void execute(Event<UIMetadataForm> event) throws Exception {
      UIMetadataForm uiView = event.getSource() ;
      UIMetadataManager uiMetaManager = uiView.getAncestorOfType(UIMetadataManager.class) ;
      uiMetaManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMetaManager) ;
    }
  }
}
