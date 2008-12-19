/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;

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
  template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
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

