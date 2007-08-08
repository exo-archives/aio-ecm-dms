/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValuesList;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;
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
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
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
      if(fileName == null || fileName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.filename-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MimeTypeResolver resolver = new MimeTypeResolver();
      String mimeType = resolver.getMimeType(fileName) ;
      ByteArrayInputStream is = null;
      if(mimeType.trim().equals("text/xml")) {
        is = new ByteArrayInputStream(input.getUploadData()) ;
      }else if(mimeType.trim().equals("application/zip")) {
        ZipInputStream zipInputStream = new ZipInputStream(input.getUploadDataAsStream()) ;
        is = Utils.extractFromZipFile(zipInputStream) ;
      }else {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeUpload.msg.data-file-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }                                   
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
        UploadService uploadService = uiUploadForm.getApplicationComponent(UploadService.class) ;
        UIFormUploadInput uiUploadInput = uiUploadForm.getChild(UIFormUploadInput.class) ;
        uploadService.removeUpload(uiUploadInput.getUploadId()) ;
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
