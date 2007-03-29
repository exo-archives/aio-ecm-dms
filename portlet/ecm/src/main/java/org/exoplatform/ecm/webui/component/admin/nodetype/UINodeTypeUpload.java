/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValuesList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 2, 2006
 * 9:39:51 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeUpload.UploadActionListener.class),
      @EventConfig(listeners = UINodeTypeUpload.CancelActionListener.class)
    }
)
public class UINodeTypeUpload extends UIForm {

  final static public String FIELD_UPLOAD = "upload" ;

  public UINodeTypeUpload() throws Exception {
    this.setMultiPart(true) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD)) ;
  }
  
  @SuppressWarnings("unchecked")
  static public class UploadActionListener extends EventListener<UINodeTypeUpload> {
    public void execute(Event<UINodeTypeUpload> event) throws Exception {
      UINodeTypeUpload uiUploadForm = event.getSource() ;
      UINodeTypeManager uiManager = uiUploadForm.getAncestorOfType(UINodeTypeManager.class) ;
      UIPopupWindow uiPopup = uiManager.findComponentById(UINodeTypeManager.IMPORT_POPUP) ;
      UINodeTypeImportPopup uiImportPopup = uiManager.findComponentById("UINodeTypeImportPopup") ;
      UIApplication uiApp = uiUploadForm.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = uiUploadForm.getUIInput(FIELD_UPLOAD) ;
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.filename-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = input.getUploadResource().getFileName();
      if(fileName == null || fileName.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.filename-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MimeTypeResolver resolver = new MimeTypeResolver();
      String mimeType = resolver.getMimeType(fileName);
      if(!mimeType.trim().equals("text/xml")){        
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.data-file-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;      
      }
      ByteArrayInputStream is = new ByteArrayInputStream(input.getUploadData()) ;
      try {
        IBindingFactory factory = BindingDirectory.getFactory(NodeTypeValuesList.class);
        IUnmarshallingContext uctx = factory.createUnmarshallingContext();
        NodeTypeValuesList nodeTypeValuesList = (NodeTypeValuesList)uctx.unmarshalDocument(is, null);
        List<NodeType> ntvList = nodeTypeValuesList.getNodeTypeValuesList();
        UINodeTypeImport uiImport = uiImportPopup.getChild(UINodeTypeImport.class) ; 
        uiImport.update(ntvList) ;
        Class[] childrenToRender = {UINodeTypeImport.class, UIPopupWindow.class} ;
        uiImportPopup.setRenderedChildrenOfTypes(childrenToRender) ;
        uiPopup.setShow(true) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.data-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UINodeTypeUpload> {
    public void execute(Event<UINodeTypeUpload> event) throws Exception {
      UINodeTypeUpload uiUpload = event.getSource() ;
      UIPopupWindow uiPopup = uiUpload.getAncestorOfType(UIPopupWindow.class) ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      UINodeTypeManager uiManager = uiUpload.getAncestorOfType(UINodeTypeManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
