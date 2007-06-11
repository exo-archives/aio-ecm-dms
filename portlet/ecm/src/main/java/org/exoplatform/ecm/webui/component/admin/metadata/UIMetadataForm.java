/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 5:31:04 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIMetadataForm.SaveActionListener.class),
      @EventConfig(listeners = UIMetadataForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMetadataForm.AddPermissionActionListener.class, phase = Phase.DECODE)
    }
)

public class UIMetadataForm extends UIFormTabPane implements UISelector {

  final static public String METADATA_PATH = "metadataPath" ;
  final static public String MAPPING = "mapping" ;
  final static public String METADATA_MAPPING = "metadataMapping" ;
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String DIALOG_TEMPLATE = "dialogTemplate" ;
  final static public String VIEW_TEMPLATE = "viewTemplate" ;
  final static public String METADATA_NAME = "metadataName" ;
  final static public String VIEW_PERMISSION = "viewPermission" ;
  final static public String METADATA_TAB = "metadataTypeTab" ;
  final static public String DIALOG_TAB = "dialogTab" ;
  final static public String VIEW_TAB = "viewTab" ;
  
  private boolean isAddNew_ = false ;
  private String metadataName_ ;

  public UIMetadataForm() throws Exception {
    super("UIMetadataForm", false) ;
    UIFormInputSetWithAction uiMetadataType = new UIFormInputSetWithAction(METADATA_TAB) ;
    uiMetadataType.addUIFormInput(new UIFormStringInput(METADATA_NAME,METADATA_NAME, null)) ;
    uiMetadataType.addUIFormInput(new UIFormStringInput(VIEW_PERMISSION, VIEW_PERMISSION, null).
                                  addValidator(EmptyFieldValidator.class).setEditable(false)) ;
    uiMetadataType.setActionInfo(VIEW_PERMISSION, new String[] {"AddPermission"}) ;
    addUIComponentInput(uiMetadataType) ;
    UIFormInputSet uiDialogTab = new UIFormInputSet(DIALOG_TAB) ;
    uiDialogTab.addUIFormInput(new UIFormTextAreaInput(DIALOG_TEMPLATE, DIALOG_TEMPLATE, null)) ;
    uiDialogTab.setRendered(false) ;
    addUIComponentInput(uiDialogTab) ;
    UIFormInputSet uiViewTab = new UIFormInputSet(VIEW_TAB) ;
    uiViewTab.addUIFormInput(new UIFormTextAreaInput(VIEW_TEMPLATE, VIEW_TEMPLATE, null)) ;
    uiViewTab.setRendered(false) ;
    addUIComponentInput(uiViewTab) ;
    setActions(new String[] {"Save", "Cancel"}) ;
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
    getUIStringInput(METADATA_NAME).setValue(metadata) ;
    String dialogTemplate = metadataService.getMetadataTemplate(metadata, true) ;
    String viewTemplate = metadataService.getMetadataTemplate(metadata, false) ;
    String role = metadataService.getMetadataRoles(metadata, true) ;
    getUIStringInput(METADATA_NAME).setEditable(false) ;
    getUIStringInput(VIEW_PERMISSION).setValue(role) ;
    getUIFormTextAreaInput(DIALOG_TEMPLATE).setValue(dialogTemplate) ;
    getUIFormTextAreaInput(VIEW_TEMPLATE).setValue(viewTemplate) ;
  }
  
  static public class SaveActionListener extends EventListener<UIMetadataForm> {
    public void execute(Event<UIMetadataForm> event) throws Exception {
      UIMetadataForm uiForm = event.getSource();      
      UIMetadataManager uiMetaManager = uiForm.getAncestorOfType(UIMetadataManager.class) ;
      MetadataService metadataService = uiForm.getApplicationComponent(MetadataService.class) ;
      String roles = uiForm.getUIStringInput(VIEW_PERMISSION).getValue() ;
      String dialogTemplate = uiForm.getUIFormTextAreaInput(DIALOG_TEMPLATE).getValue() ;
      if(dialogTemplate == null) dialogTemplate = "" ;
      String viewTemplate = uiForm.getUIFormTextAreaInput(VIEW_TEMPLATE).getValue() ;
      if(viewTemplate == null) viewTemplate = "" ;
      if(uiMetaManager.metadatasDeleted.contains(uiForm.metadataName_)) {
        uiForm.isAddNew_ = true ;
        uiMetaManager.metadatasDeleted.remove(uiForm.metadataName_) ;
      } else {
        uiForm.isAddNew_ = false ;
      }
      metadataService.addMetadata(uiForm.metadataName_, true, roles, dialogTemplate, uiForm.isAddNew_) ;
      metadataService.addMetadata(uiForm.metadataName_, false, roles, viewTemplate, uiForm.isAddNew_) ;
      uiForm.reset() ;
      uiMetaManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMetaManager) ;
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UIMetadataForm> {
    public void execute(Event<UIMetadataForm> event) throws Exception {
      UIMetadataForm uiView = event.getSource() ;
      UIMetadataManager uiManager = uiView.getAncestorOfType(UIMetadataManager.class) ;
      String membership = uiView.getUIStringInput(VIEW_PERMISSION).getValue() ;
      uiManager.initPopupPermission(membership) ;
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
