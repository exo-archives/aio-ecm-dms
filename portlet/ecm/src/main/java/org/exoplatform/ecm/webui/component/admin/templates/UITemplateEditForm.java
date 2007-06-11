/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import javax.jcr.Node;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 4, 2006 9:50:06 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITemplateEditForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateEditForm.CancelActionListener.class)
    }
)

public class UITemplateEditForm extends UIForm {

  final static public String FIELD_NAME = "name" ;
  final static public String FIELD_LABEL = "label" ;
  final static public String FIELD_ISTEMPLATE = "isDocumentTemplate" ; 

  private static String nodeType_ ;

  public UITemplateEditForm() {
    addChild(new UIFormStringInput(FIELD_NAME, null)) ;
    addChild(new UIFormStringInput(FIELD_LABEL, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISTEMPLATE, null, null)) ;
  }

  private boolean isDocumentTemplate(String nodeType)throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    return templateService.getDocumentTemplates().contains(nodeType) ;
  }

  public void update(String nodeType) throws Exception {
    TemplateService tempService = getApplicationComponent(TemplateService.class) ;
    Node node = tempService.getTemplatesHome().getNode(nodeType) ;
    String label = node.getProperty(TemplateService.TEMPLATE_LABEL).getString() ;
    getUIFormCheckBoxInput(FIELD_ISTEMPLATE).setChecked(isDocumentTemplate(nodeType)) ;
    getUIStringInput(FIELD_NAME).setValue(nodeType) ;
    getUIStringInput(FIELD_LABEL).setValue(label) ;
    getUIFormCheckBoxInput(FIELD_ISTEMPLATE).setEnable(false) ;
    getUIStringInput(FIELD_NAME).setEditable(false) ;
    nodeType_ = nodeType ;
  }

  static public class SaveActionListener extends EventListener<UITemplateEditForm> {
    public void execute(Event<UITemplateEditForm> event) throws Exception {
      UITemplateEditForm uiForm = event.getSource() ;
      TemplateService tempService = uiForm.getApplicationComponent(TemplateService.class) ;
      Node node = tempService.getTemplatesHome().getNode(nodeType_) ;
      node.setProperty(TemplateService.TEMPLATE_LABEL,uiForm.getUIStringInput(FIELD_LABEL).getValue()) ;
      node.save() ;
      uiForm.reset() ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      UIPopupWindow uiPopupWindow = uiManager.getChildById(UITemplatesManager.EDIT_TEMPLATE) ;
      uiPopupWindow.setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UITemplateEditForm> {
    public void execute(Event<UITemplateEditForm> event) throws Exception {
      UITemplatesManager uiManager = event.getSource().getAncestorOfType(UITemplatesManager.class) ;
      UIPopupWindow uiPopupWindow = uiManager.getChildById(UITemplatesManager.EDIT_TEMPLATE) ;
      uiPopupWindow.setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
