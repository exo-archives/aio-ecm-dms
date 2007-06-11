/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Sep 29, 2006
 * 11:57:24 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITaxonomyForm.SaveActionListener.class),
      @EventConfig(listeners = UITaxonomyForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITaxonomyForm extends UIForm {
  
  final static private String FIELD_PARENT = "parentPath" ;
  final static private String FIELD_NAME = "taxonomyName" ;
  final static private String ROOT_PATH = "/jcr:system/exo:ecm/" ;
  
  public UITaxonomyForm() throws Exception {
    addUIFormInput(new UIFormInputInfo(FIELD_PARENT, FIELD_PARENT, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).
                   addValidator(ECMNameValidator.class)) ;
  }
  
  public void setParent(String path) {
    path = path.replaceFirst(ROOT_PATH, "") ;
    getUIFormInputInfo(FIELD_PARENT).setValue(path) ;
    getUIStringInput(FIELD_NAME).setValue(null) ;
  }
  
  static public class SaveActionListener extends EventListener<UITaxonomyForm> {
    public void execute(Event<UITaxonomyForm> event) throws Exception {
      UITaxonomyForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UITaxonomyManager uiManager = uiForm.getAncestorOfType(UITaxonomyManager.class) ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      if(name == null || name.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyForm.msg.name-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(!Utils.isNameValid(name, new String[]{"[","]",":"})) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String parentPath = ROOT_PATH + uiForm.getUIFormInputInfo(FIELD_PARENT).getValue() ;
      try {
        uiManager.addTaxonomy(parentPath, name)  ;
      } catch(Exception e) {
        Object[] arg = {name} ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyForm.msg.exist", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiForm.reset() ;
      UIPopupWindow uiPopup = uiForm.getParent() ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class CancelActionListener extends EventListener<UITaxonomyForm> {
    public void execute(Event<UITaxonomyForm> event) throws Exception {
      UITaxonomyForm uiForm = event.getSource() ;
      UIPopupWindow uiPopup = uiForm.getParent() ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}
