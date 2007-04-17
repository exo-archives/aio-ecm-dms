/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Session;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
import org.exoplatform.webui.component.UIFormUploadInput;
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
 * Oct 5, 2006  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/popup/admin/UIFormWithMultiRadioBox.gtmpl",
    events = {
      @EventConfig(listeners = UIImportNode.ImportActionListener.class),
      @EventConfig(listeners = UIImportNode.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIImportNode extends UIForm implements UIPopupComponent {

  public static final String FORMAT = "format" ;
  public static final String DOCUMENT_VIEW = "Document View" ;
  public static final String SYSTEM_VIEW = "System View" ;
  public static final String DOC_VIEW = "docview" ;
  public static final String SYS_VIEW = "sysview" ;
  public static final String FILE_UPLOAD = "upload" ;
  
  public UIImportNode() throws Exception {
    this.setMultiPart(true) ;
    addUIFormInput(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD)) ;
    List<SelectItemOption<String>> formatItem = new ArrayList<SelectItemOption<String>>() ;
    formatItem.add(new SelectItemOption<String>(DOCUMENT_VIEW, DOC_VIEW));
    formatItem.add(new SelectItemOption<String>(SYSTEM_VIEW, SYS_VIEW));
    addUIFormInput(new UIFormRadioBoxInput(FORMAT, DOC_VIEW, formatItem).
                   setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;
  }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static public class ImportActionListener extends EventListener<UIImportNode> {
    public void execute(Event<UIImportNode> event) throws Exception {
      UIImportNode uiImport = event.getSource() ;
      UIJCRExplorer uiExplorer = uiImport.getAncestorOfType(UIJCRExplorer.class) ;
      Session session = uiExplorer.getSession() ;
      UIApplication uiApp = uiImport.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = uiImport.getUIInput(FILE_UPLOAD) ;
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.filename-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      byte[] content = input.getUploadData() ;
      try {
        session.importXML(uiExplorer.getCurrentNode().getPath(), new ByteArrayInputStream(content),
                          ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
        return ;
      }
      if(!uiExplorer.getPreference().isJcrEnable()) session.save() ; 
      uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.import-successful", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIImportNode> {
    public void execute(Event<UIImportNode> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

}
