/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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
    template = "app:/groovy/webui/component/explorer/UIFormWithoutAction.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIEditModeDocumentType.ChangeTypeActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIEditModeDocumentType.SelectPathActionListener.class)
    }
)
public class UIEditModeDocumentType extends UIForm implements UISelector {

  final static public String FIELD_SELECT = "selectTemplate" ;
  final static public String FIELD_SAVEDPATH = "savedPath" ;
  final static public String ACTION_INPUT = "actionInput" ;
  final static public String WORKSPACE_NAME = "workspaceName" ;
  
  public UIEditModeDocumentType() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox templateSelect = new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, options) ;
    templateSelect.setOnChange("ChangeType") ;
    addUIFormInput(templateSelect) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction(ACTION_INPUT) ;
    uiInputAct.addUIFormInput(new UIFormStringInput(FIELD_SAVEDPATH, FIELD_SAVEDPATH, null)) ;
    uiInputAct.setActionInfo(FIELD_SAVEDPATH, new String[] {"SelectPath"}) ;
    addUIComponentInput(uiInputAct) ;
  }
  
  public String getSelectValue() {
    return getUIFormSelectBox(FIELD_SELECT).getValue();
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    UIEditModeController uiController = getParent() ;
    UIEditModeDocumentForm uiDocumentForm = uiController.getChild(UIEditModeDocumentForm.class) ;
    uiDocumentForm.setSavedPath(value) ;
    UIPopupWindow uiPopup = uiController.getChild(UIPopupWindow.class) ;
    uiPopup.setRendered(false) ;
    uiPopup.setShow(false) ;
  }
  
  static public class SelectPathActionListener extends EventListener<UIEditModeDocumentType> {
    public void execute(Event<UIEditModeDocumentType> event) throws Exception {
      UIEditModeDocumentType uiTypeForm = event.getSource() ;
      UIEditModeController uiController = uiTypeForm.getParent() ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletRequest request = context.getRequest() ; 
      PortletPreferences preferences = request.getPreferences() ;
      String wsName = preferences.getValue(Utils.WORKSPACE_NAME, "") ;
      if(uiTypeForm.getChildById(WORKSPACE_NAME) != null) {
        wsName = ((UIFormSelectBox)uiTypeForm.getChildById(WORKSPACE_NAME)).getValue() ; ;
      }
      uiController.initPopupJCRBrowser(wsName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiController) ;
    }
  }
  
  static public class ChangeTypeActionListener extends EventListener<UIEditModeDocumentType> {
    public void execute(Event<UIEditModeDocumentType> event) throws Exception {
      UIEditModeDocumentType uiSelectForm = event.getSource() ;
      UIEditModeController uiEditModeController = uiSelectForm.getParent() ;
      UIEditModeDocumentForm uiEditModeDocumentForm = uiEditModeController.getChild(UIEditModeDocumentForm.class) ;
      uiEditModeDocumentForm.getChildren().clear() ;
      uiEditModeDocumentForm.resetProperties() ;
      String type = uiSelectForm.getUIFormSelectBox(FIELD_SELECT).getValue() ;
      uiEditModeDocumentForm.setTemplateNode(type) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEditModeController) ;
    }
  }
}
