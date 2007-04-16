/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTabPane;
import org.exoplatform.webui.component.UIFormTextAreaInput;
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
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 03, 2006
 * 9:43:23 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UITemplateForm.SaveActionListener.class),
      @EventConfig(listeners = UITemplateForm.RefreshActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UITemplateForm.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UITemplateForm.AddPermissionActionListener.class, phase = Phase.DECODE)
    }
)
public class UITemplateForm extends UIFormTabPane implements UISelector {  
  final static public String FIELD_NAME = "name" ;
  final static public String FIELD_LABEL = "label" ;
  final static public String FIELD_ISTEMPLATE = "isDocumentTemplate" ;
  final static public String FIELD_DIALOG = "dialog" ;
  final static public String FIELD_VIEW = "view" ;
  final static public String FIELD_TAB_TEMPLATE = "template" ;
  final static public String FIELD_TAB_DIALOG = "defaultDialog" ;
  final static public String FIELD_TAB_VIEW = "defaultView" ;
  final static public String FIELD_PERMISSION = "permission" ;

  public UITemplateForm() throws Exception {
    super("UITemplateForm", false) ;
    UIFormInputSetWithAction templateTab = new UIFormInputSetWithAction(FIELD_TAB_TEMPLATE) ;
    templateTab.setActions(new String[]{"Save", "Refresh", "Cancel"}, null) ;
    templateTab.addUIFormInput(new UIFormSelectBox(FIELD_NAME, FIELD_NAME, getOption())) ;
    templateTab.addUIFormInput(new UIFormStringInput(FIELD_LABEL, FIELD_LABEL, null).
                               addValidator(EmptyFieldValidator.class)) ;
    
    templateTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ISTEMPLATE, FIELD_ISTEMPLATE, null));                               
    templateTab.addUIFormInput(new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION, null).
                               addValidator(EmptyFieldValidator.class)) ;
    templateTab.setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"}) ;
    addUIComponentInput(templateTab) ;
    
    UIFormInputSet defaultDialogTab = new UIFormInputSet(FIELD_TAB_DIALOG) ;
    defaultDialogTab.addUIFormInput(new UIFormTextAreaInput(FIELD_DIALOG, FIELD_DIALOG, null).
                                    addValidator(EmptyFieldValidator.class)) ;
    defaultDialogTab.setRendered(false) ;
    addUIFormInput(defaultDialogTab) ;
    UIFormInputSet defaultViewTab = new UIFormInputSet(FIELD_TAB_VIEW) ;
    defaultViewTab.addUIFormInput(new UIFormTextAreaInput(FIELD_VIEW, FIELD_VIEW, null).
                                  addValidator(EmptyFieldValidator.class)) ;
    defaultViewTab.setRendered(false) ;
    addUIFormInput(defaultViewTab) ;
    setActions(new String[]{}) ;
  }

  public void refresh()throws Exception {
    getUIStringInput(FIELD_LABEL).setValue("") ;
    getUIFormCheckBoxInput(FIELD_ISTEMPLATE).setChecked(false) ;
    getUIFormTextAreaInput(FIELD_DIALOG).setValue("") ;
    getUIFormTextAreaInput(FIELD_VIEW).setValue("") ;
    getUIStringInput(FIELD_PERMISSION).setValue("") ;
    getUIFormSelectBox(FIELD_NAME).setOptions(getOption()) ; 
  }

  private List<SelectItemOption<String>> getOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ; 
    CmsConfigurationService configService = getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(configService.getWorkspace()) ;
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager() ; 
    NodeTypeIterator iter = nodeTypeManager.getAllNodeTypes() ;
    while (iter.hasNext()) {
      NodeType nodeType = iter.nextNodeType();
      String nodeTypeName = nodeType.getName();
      options.add(new SelectItemOption<String>(nodeTypeName,nodeTypeName)) ;
    }
    return options ;
  }

  private boolean isExistedItem(String nodeType) throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    templateService.getTemplatesHome().getNodes() ;
    NodeIterator iter = templateService.getTemplatesHome().getNodes() ;
    while (iter.hasNext()) {
      if(iter.nextNode().getName().equals(nodeType)) return true ;
    }    
    return false ;
  }
  
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    UIFormInputSetWithAction uiFormAction = getChildById(FIELD_TAB_TEMPLATE) ;
    uiFormAction.getUIStringInput(FIELD_PERMISSION).setValue(value) ;
    UITemplatesManager uiManager = getAncestorOfType(UITemplatesManager.class) ;
    uiManager.removeChildById("AddNewTemplatePermission") ;
  }

  static public class SaveActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      String name = uiForm.getUIFormSelectBox(FIELD_NAME).getValue() ;
      if(uiForm.isExistedItem(name)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        Object[] args = {name} ;
        app.addMessage(new ApplicationMessage("UITemplateForm.msg.item-exist", args)) ; 
        return ;
      }
      String label = uiForm.getUIStringInput(FIELD_LABEL).getValue() ; 
      String dialog = uiForm.getUIFormTextAreaInput(FIELD_DIALOG).getValue() ;
      String view = uiForm.getUIFormTextAreaInput(FIELD_VIEW).getValue() ;
      boolean isDocumentTemplate = uiForm.getUIFormCheckBoxInput(FIELD_ISTEMPLATE).isChecked() ;
      String[] roles = {uiForm.getUIStringInput(FIELD_PERMISSION).getValue()} ; 
      if(dialog == null) dialog = "" ;
      if(view == null) view = "" ;
      TemplateService templateService = uiForm.getApplicationComponent(TemplateService.class) ;
      templateService.addTemplate(true, name, label, isDocumentTemplate, 
          TemplateService.DEFAULT_DIALOG, roles, dialog) ;
      templateService.addTemplate(false, name, label, isDocumentTemplate,
          TemplateService.DEFAULT_VIEW, roles, view) ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      uiManager.refresh() ;
      uiForm.refresh() ;
      uiManager.removeChildById("TemplatePopup") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiTemplateForm = event.getSource() ;
      UITemplatesManager uiManager = uiTemplateForm.getAncestorOfType(UITemplatesManager.class) ;
      uiTemplateForm.reset() ;
      uiManager.removeChildById("TemplatePopup") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static  public class RefreshActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiFormTabPane = event.getSource() ;
      uiFormTabPane.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFormTabPane.getParent()) ;
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiTemplateForm = event.getSource() ;
      UITemplatesManager uiManager = uiTemplateForm.getAncestorOfType(UITemplatesManager.class) ;
      uiManager.initPopupPermission("AddNew") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}