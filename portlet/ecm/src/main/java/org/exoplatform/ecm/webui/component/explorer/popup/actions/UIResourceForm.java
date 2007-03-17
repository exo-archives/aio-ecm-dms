/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 7, 2006
 * 14:42:15 AM
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  type     = UIResourceForm.class,
  template =  "system:/groovy/webui/component/UIFormWithTitle.gtmpl",
  events = {
    @EventConfig(listeners = UIResourceForm.SaveActionListener.class),
    @EventConfig(listeners = UIResourceForm.BackActionListener.class)
  }
)

public class UIResourceForm extends UIForm {
  
  final static public String FIElD_NAME = "name" ;
  final static public String FIElD_TEXTTOMODIFY = "textToModify" ;
  final static public String FIElD_FILETOUPLOAD = "fileToUpload" ;
  
  private Node contentNode_ ;
  private Property mime_ ;
  private Session session_ ; 
  
  public UIResourceForm() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIElD_NAME, FIElD_NAME, null)) ;    
  }

  public void setContentNode(Node node, Session session) throws RepositoryException {
    session_ = session ;
    contentNode_ = node ;    
    mime_ = node.getProperty("jcr:mimeType") ;
    String name = node.getParent().getName() ;    
    getUIStringInput(FIElD_NAME).setValue(name) ;    
    if(mime_.getString().startsWith("text")) {
      String contentText = node.getProperty("jcr:data").getString() ;
      addUIFormInput(new UIFormTextAreaInput(FIElD_TEXTTOMODIFY, FIElD_TEXTTOMODIFY, contentText)) ;      
    }else {
      getUIStringInput(FIElD_NAME).setEditable(false);
      addUIFormInput(new UIFormUploadInput(FIElD_FILETOUPLOAD, FIElD_FILETOUPLOAD)) ;
    }
  }
  
  public boolean isText() throws RepositoryException {return (mime_.getString().startsWith("text")) ;}
  
  static  public class SaveActionListener extends EventListener<UIResourceForm> {
    public void execute(Event<UIResourceForm> event) throws Exception {
      UIResourceForm uiResourceForm = event.getSource() ;
      Property prop = uiResourceForm.contentNode_.getProperty("jcr:mimeType") ;
      UIJCRExplorer uiJCRExplorer = uiResourceForm.getAncestorOfType(UIJCRExplorer.class) ;
      if(prop.getString().startsWith("text")) {
        String text = uiResourceForm.getUIFormTextAreaInput(FIElD_TEXTTOMODIFY).getValue() ;
        uiResourceForm.contentNode_.setProperty("jcr:data", text) ;
      }else {
        //TODO: upload file here !
        UIFormUploadInput  fileUpload = 
          (UIFormUploadInput)uiResourceForm.getUIInput(FIElD_FILETOUPLOAD) ; 
        byte[] content =  fileUpload.getUploadData() ;
        uiResourceForm.contentNode_.setProperty("jcr:data", new ByteArrayInputStream(content)) ;
      }
      if(uiResourceForm.session_ != null) uiResourceForm.session_.save() ;
      else uiJCRExplorer.getSession().save() ;
      uiResourceForm.setRenderSibbling(UIDocumentInfo.class);      
    }
  }
  
  static  public class BackActionListener extends EventListener<UIResourceForm> {
    public void execute(Event<UIResourceForm> event) throws Exception {
      UIResourceForm uiResourceForm = event.getSource() ;
      uiResourceForm.setRenderSibbling(UIDocumentInfo.class) ;
    }
  }
}

