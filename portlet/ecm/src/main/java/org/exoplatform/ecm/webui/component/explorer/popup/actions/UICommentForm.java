/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmailAddressValidator;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 30, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UICommentForm.SaveActionListener.class),
      @EventConfig(listeners = UICommentForm.CancelActionListener.class, phase = Phase.DECODE)
    }
) 

public class UICommentForm extends UIForm implements UIPopupComponent {
  final public static String FIELD_NAME = "name" ;
  final public static String FIELD_EMAIL = "email" ;
  final public static String FIELD_WEBSITE = "website" ;
  final public static String FIELD_COMMENT = "comment" ;
  
  private Node document_ ;
  public UICommentForm() throws Exception {
    addChild(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    addChild(new UIFormStringInput(FIELD_EMAIL, FIELD_EMAIL, null).addValidator(EmailAddressValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_WEBSITE, FIELD_WEBSITE, null)) ;
    addChild(new UIFormTextAreaInput(FIELD_COMMENT, FIELD_COMMENT, null).addValidator(EmptyFieldValidator.class)) ;
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
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      String email = uiForm.getUIStringInput(FIELD_EMAIL).getValue() ;
      String website = uiForm.getUIStringInput(FIELD_WEBSITE).getValue() ;
      String comment = uiForm.getUIFormTextAreaInput(FIELD_COMMENT).getValue() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      try {
        String language = uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).
        getChild(UIDocumentInfo.class).getSelectedLanguage() ;
        if(UIDocumentInfo.DEFAULT_LANGUAGE.equals(language)) { 
          if(!uiForm.document_.hasProperty("exo:language")){
            uiForm.document_.addMixin("mix:i18n") ;
            uiForm.document_.save() ;
            language = UIDocumentInfo.DEFAULT_LANGUAGE ;
          } else {
            language = uiForm.document_.getProperty("exo:language").getString() ;
          }
        }
        CommentsService commentsService = uiForm.getApplicationComponent(CommentsService.class) ; 
        commentsService.addComment(uiForm.document_, name, email, website, comment, language) ;
      } catch (Exception e) {        
        e.printStackTrace() ;
      }
      uiExplorer.updateAjax(event) ;
    }
  }
}
