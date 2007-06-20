/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.administration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.exoplatform.services.workflow.WorkflowServiceContainer;
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
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 5, 2007 2:43:15 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIUploadProcess.SaveActionListener.class), 
      @EventConfig(listeners = UIUploadProcess.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIUploadProcess extends UIForm {
  
  final static public String FIELD_NAME =  "name" ;
  final static public String FIELD_UPLOAD = "upload" ;
  
  public UIUploadProcess() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    uiInput.setEditable(false);
    addUIFormInput(uiInput) ;
  }
  
  static  public class SaveActionListener extends EventListener<UIUploadProcess> {
    public void execute(Event<UIUploadProcess> event) throws Exception {
      UIUploadProcess uiUploadProcess = event.getSource() ;
      UIWorkflowAdministrationPortlet uiWorkflowAdministrationPortlet = 
        uiUploadProcess.getAncestorOfType(UIWorkflowAdministrationPortlet.class) ;
      WorkflowServiceContainer workflowServiceContainer = 
        uiUploadProcess.getApplicationComponent(WorkflowServiceContainer.class) ;
      UIApplication uiApp = uiUploadProcess.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiUploadProcess.getUIInput(FIELD_UPLOAD);
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIUploadProcess.msg.fileName-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
        
      }
      String fileName = input.getUploadResource().getFileName() ;
      if(fileName == null || fileName.equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIUploadProcess.msg.fileName-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      byte[] content = input.getUploadData() ;
      String name = uiUploadProcess.getUIStringInput(FIELD_NAME).getValue() ;
      if(name == null) name = fileName;
      String[] arrFilterChar = {"&", "$", "@", ":","]", "[", "*", "%", "!"} ;
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIUploadProcess.msg.fileName-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      try {
        InputStream inputStream = new ByteArrayInputStream(content);
        workflowServiceContainer.deployProcess(inputStream) ;
        uiApp.addMessage(new ApplicationMessage("UIUploadProcess.msg.process-successful", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch(Exception e) {
        e.printStackTrace() ;
      }
      UIAdminstrationManager uiAdminstrationManager = 
        uiWorkflowAdministrationPortlet.getChild(UIAdminstrationManager.class) ;
      uiAdminstrationManager.updateMonitorGrid() ;
      UIPopupWindow uiPopup = uiWorkflowAdministrationPortlet.getChildById("UploadProcessPopup") ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkflowAdministrationPortlet) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIUploadProcess> {
    public void execute(Event<UIUploadProcess> event) throws Exception {
      UIWorkflowAdministrationPortlet uiWorkflowAdministrationPortlet = 
        event.getSource().getAncestorOfType(UIWorkflowAdministrationPortlet.class) ;
      UIPopupWindow uiPopup = uiWorkflowAdministrationPortlet.getChildById("UploadProcessPopup") ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkflowAdministrationPortlet) ;
    }
  }

}
