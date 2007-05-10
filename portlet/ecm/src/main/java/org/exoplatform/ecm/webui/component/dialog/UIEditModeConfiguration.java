/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 25, 2007 9:10:53 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIEditModeConfiguration.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIEditModeConfiguration.SelectPathActionListener.class)
    }
)
public class UIEditModeConfiguration extends UIForm implements UISelector {

  final static public String FIELD_SELECT = "selectTemplate" ;
  final static public String FIELD_SAVEDPATH = "savedPath" ;
  final static public String ACTION_INPUT = "actionInput" ;
  final static public String WORKSPACE_NAME = "workspaceName" ;
  
  public UIEditModeConfiguration() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, options)) ;
    UIFormSelectBox uiWorkspaceList = 
      new UIFormSelectBox(UIEditModeConfiguration.WORKSPACE_NAME, UIEditModeConfiguration.WORKSPACE_NAME, options) ; 
    addUIFormInput(uiWorkspaceList) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction(ACTION_INPUT) ;
    uiInputAct.addUIFormInput(new UIFormStringInput(FIELD_SAVEDPATH, FIELD_SAVEDPATH, null)) ;
    uiInputAct.setActionInfo(FIELD_SAVEDPATH, new String[] {"SelectPath"}) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"Save"}) ;
  }
  
  public void initEditMode() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = context.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    UIFormSelectBox uiSelectBox = getUIFormSelectBox(FIELD_SELECT) ;
    boolean hasDefaultDoc = false ;
    boolean isDefaultWs = false ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates() ;
    String prefType = preferences.getValue("type", "") ;
    for(int i = 0; i < templates.size(); i ++) {
      String nodeTypeName = templates.get(i).toString() ;
      if(nodeTypeName.equals(prefType)) hasDefaultDoc = true ;
      options.add(new SelectItemOption<String>(nodeTypeName)) ;
    }
    uiSelectBox.setOptions(options) ;
    if(hasDefaultDoc) {
      uiSelectBox.setValue(prefType);
    } else if(options.size() > 0) {
      uiSelectBox.setValue(options.get(0).getValue());
    }
    ManageableRepository repository = getApplicationComponent(RepositoryService.class).getRepository();
    String[] wsNames = repository.getWorkspaceNames();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
    String prefWs = preferences.getValue(Utils.WORKSPACE_NAME, "") ;
    for(String wsName : wsNames) {
      if(wsName.equals(prefWs)) isDefaultWs = true ;
      workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
    }
    UIFormSelectBox uiWorkspaceList = getUIFormSelectBox(WORKSPACE_NAME) ; 
    uiWorkspaceList.setOptions(workspace) ;
    if(isDefaultWs) {
      uiWorkspaceList.setValue(prefWs);
    } else if(options.size() > 0) {
      uiWorkspaceList.setValue(workspace.get(0).getValue());
    }
    getUIStringInput(FIELD_SAVEDPATH).setValue(preferences.getValue("path", "")) ;
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    UIDialogPortlet uiDialog = getParent() ;
    UIPopupWindow uiPopup = uiDialog.getChild(UIPopupWindow.class) ;
    uiPopup.setRendered(false) ;
    uiPopup.setShow(false) ;
  }
  
  static public class SelectPathActionListener extends EventListener<UIEditModeConfiguration> {
    public void execute(Event<UIEditModeConfiguration> event) throws Exception {
      UIEditModeConfiguration uiTypeForm = event.getSource() ;
      UIDialogPortlet uiDialog = uiTypeForm.getParent() ;
      uiDialog.initPopupJCRBrowser(uiTypeForm.getUIFormSelectBox(WORKSPACE_NAME).getValue()) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIEditModeConfiguration> {
    public void execute(Event<UIEditModeConfiguration> event) throws Exception {
      UIEditModeConfiguration uiSelectForm = event.getSource() ;
      UIApplication uiApp = uiSelectForm.getAncestorOfType(UIApplication.class) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletRequest request = context.getRequest() ; 
      PortletPreferences preferences = request.getPreferences() ;
      String fileType = uiSelectForm.getUIFormSelectBox(FIELD_SELECT).getValue() ;
      String location = uiSelectForm.getUIStringInput(FIELD_SAVEDPATH).getValue() ;
      String wsName = uiSelectForm.getUIFormSelectBox(WORKSPACE_NAME).getValue() ;
      preferences.setValue("workspace", wsName) ;
      preferences.setValue("path", location) ;
      preferences.setValue("type", fileType) ;
      preferences.store() ;
      uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.save-successfully", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      uiSelectForm.reset() ;
    }
  }
}
