/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.namespace;

import javax.jcr.NamespaceRegistry;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL 
 * Author : phamtuan
 * phamtuanchip@yahoo.de September 20, 2006 16:37:15 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl", 
    events = {
      @EventConfig(listeners = UINamespaceForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UINamespaceForm.CancelActionListener.class)
    }
)
public class UINamespaceForm extends UIForm {

  final static public String FIELD_PREFIX = "namespace" ;
  final static public String FIELD_URI = "uri" ;

  public UINamespaceForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_PREFIX, FIELD_PREFIX, null).
                   addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormStringInput(FIELD_URI, FIELD_URI, null).
                   addValidator(EmptyFieldValidator.class)) ;
  }

  static public class SaveActionListener extends EventListener<UINamespaceForm> {
    public void execute(Event<UINamespaceForm> event) throws Exception {
      UINamespaceForm uiForm = event.getSource() ;
      String uri = uiForm.getUIStringInput(FIELD_URI).getValue() ;
      RepositoryService repositoryService = uiForm.getApplicationComponent(RepositoryService.class) ;
      NamespaceRegistry namespaceRegistry = repositoryService.getRepository().getNamespaceRegistry() ;
      UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
      String prefix = uiForm.getUIStringInput(FIELD_PREFIX).getValue() ;
      Object[] args = { prefix } ; 
      try {
        namespaceRegistry.registerNamespace(prefix, uri) ;
      } catch (Exception e) {
        app.addMessage(new ApplicationMessage("UINamespaceForm.msg.prefix-already-exists", args, ApplicationMessage.ERROR)) ;
        return ;
      }
      UINamespaceManager uiManager = uiForm.getAncestorOfType(UINamespaceManager.class) ;
      uiManager.refresh() ;
      uiManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class CancelActionListener extends EventListener<UINamespaceForm> {
    public void execute(Event<UINamespaceForm> event) throws Exception {
      UINamespaceForm uiForm = event.getSource() ;
      UINamespaceManager uiManager = uiForm.getAncestorOfType(UINamespaceManager.class) ;
      uiManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}