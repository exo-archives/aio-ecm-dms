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

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentContainer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.NullFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 30, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UICommentForm.SaveActionListener.class),
      @EventConfig(listeners = UICommentForm.CancelActionListener.class, phase = Phase.DECODE)
    }
) 

public class UICommentForm extends UIForm implements UIPopupComponent {
  final public static String FIELD_EMAIL = "email" ;
  final public static String FIELD_WEBSITE = "website" ;
  final public static String FIELD_COMMENT = "comment" ;
  
  private Node document_ ;
  
  public UICommentForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_EMAIL, FIELD_EMAIL, null).addValidator(NullFieldValidator.class).addValidator(EmailAddressValidator.class)) ;
    addUIFormInput(new UIFormStringInput(FIELD_WEBSITE, FIELD_WEBSITE, null)) ;
    addUIFormInput(new UIFormWYSIWYGInput(FIELD_COMMENT, FIELD_COMMENT, null, true)) ;
  }

  public void activate() throws Exception {
    document_ = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
  }
  
  public void deActivate() throws Exception {
    document_ = null ;
  }  
  
  public Node getDocument() { return document_ ; }
  public void setDocument(Node doc) { document_ = doc ; }
  
  public static class CancelActionListener extends EventListener<UICommentForm>{
    public void execute(Event<UICommentForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).cancelPopupAction() ;
    }
  }  
  
  public static class SaveActionListener extends EventListener<UICommentForm>{
    public void execute(Event<UICommentForm> event) throws Exception {
      UICommentForm uiForm = event.getSource() ;
      String name = event.getRequestContext().getRemoteUser() ;
      if(name == null || name.trim().length() == 0) name = "anonymous" ;
      String email = uiForm.getUIStringInput(FIELD_EMAIL).getValue() ;
      String website = uiForm.getUIStringInput(FIELD_WEBSITE).getValue() ;
      String comment = (String)uiForm.<UIFormInputBase>getUIInput(FIELD_COMMENT).getValue() ;
      if(comment == null || comment.trim().length() == 0) {
        throw new MessageException(new ApplicationMessage("UICommentForm.msg.content-null", null, 
                                                          ApplicationMessage.WARNING)) ;
      }
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      try {
        String language = uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).
        getChild(UIDocumentContainer.class).getChild(UIDocumentInfo.class).getLanguage() ;
        CommentsService commentsService = uiForm.getApplicationComponent(CommentsService.class) ; 
        commentsService.addComment(uiForm.document_, name, email, website, comment, language) ;
      } catch (Exception e) {        
        e.printStackTrace() ;
      }
      uiExplorer.updateAjax(event) ;
    }
  }
}