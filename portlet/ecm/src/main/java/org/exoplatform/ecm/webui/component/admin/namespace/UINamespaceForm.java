/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.namespace;

import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
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
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL 
 * Author : phamtuan
 * phamtuanchip@yahoo.de September 20, 2006 16:37:15 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
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
        addValidator(ECMNameValidator.class)) ;
    addUIFormInput(new UIFormStringInput(FIELD_URI, FIELD_URI, null).
        addValidator(EmptyFieldValidator.class)) ;
  }

  static public class SaveActionListener extends EventListener<UINamespaceForm> {
    public void execute(Event<UINamespaceForm> event) throws Exception {
      UINamespaceForm uiForm = event.getSource() ;
      String repository = uiForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      String uri = uiForm.getUIStringInput(FIELD_URI).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      NamespaceRegistry namespaceRegistry = uiForm.getApplicationComponent(RepositoryService.class)
      .getRepository(repository).getNamespaceRegistry() ;
      String prefix = uiForm.getUIStringInput(FIELD_PREFIX).getValue() ;
      if(prefix == null || prefix.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINamespaceForm.msg.prefix-null", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uri == null || uri.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINamespaceForm.msg.uri-null", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        namespaceRegistry.registerNamespace(prefix, uri) ;
      } catch (ItemExistsException IEE) {
        Object[] args = { prefix } ; 
        uiApp.addMessage(new ApplicationMessage("UINamespaceForm.msg.prefix-already-exists", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (NamespaceException NE) {
        Object[] args = { uri } ; 
        uiApp.addMessage(new ApplicationMessage("UINamespaceForm.msg.uri-already-exists", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        e.printStackTrace() ;
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