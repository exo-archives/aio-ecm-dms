/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;
import javax.jcr.Node;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormWYSIWYGInput;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@gmail.com
 * Jan 30, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UICBCommentForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UICBCommentForm.CancelActionListener.class)
    }
) 

public class UICBCommentForm extends UIForm implements UIPopupComponent {
  final public static String DEFAULT_LANGUAGE = "default".intern() ;
  final private static String FIELD_EMAIL = "email" ;
  final private static String FIELD_WEBSITE = "website" ;
  final private static String FIELD_COMMENT = "comment" ;
  private Node docNode_ ;


  public UICBCommentForm() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    context.getJavascriptManager().importJavascript("eXo.ecm.ExoEditor","/ecm/javascript/");
    addChild(new UIFormStringInput(FIELD_EMAIL, FIELD_EMAIL, null).addValidator(EmailAddressValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_WEBSITE, FIELD_WEBSITE, null)) ;
    addChild(new UIFormWYSIWYGInput(FIELD_COMMENT, FIELD_COMMENT, null)) ;
    setActions(new String[] {"Save", "Cancel"}) ;
  }

  public Node getDocument() { return docNode_ ;}
  public void setDocument(Node node) { docNode_ = node ;}

  public static class CancelActionListener extends EventListener<UICBCommentForm>{
    public void execute(Event<UICBCommentForm> event) throws Exception {
      UICBCommentForm uiForm = event.getSource() ;
      uiForm.reset() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }  

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public static class SaveActionListener extends EventListener<UICBCommentForm>{
    public void execute(Event<UICBCommentForm> event) throws Exception {
      UICBCommentForm uiForm = event.getSource() ;
      String name = event.getRequestContext().getRemoteUser() ;
      if(Utils.isNameEmpty(name)) name = "anonymous" ;
      String email = uiForm.getUIStringInput(FIELD_EMAIL).getValue() ;
      String website = uiForm.getUIStringInput(FIELD_WEBSITE).getValue() ;
      String comment = (String)uiForm.<UIFormInputBase>getUIInput(FIELD_COMMENT).getValue() ;
      UIBrowseContentPortlet uiPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer uiBCContainer = uiPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocumentDetail = uiBCContainer.getChild(UIDocumentDetail.class) ;
      Node currentDoc = uiForm.getDocument() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String language = uiDocumentDetail.getLanguage() ;
      if(comment == null || comment.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UICBCommentForm.msg.comment-required", null, ApplicationMessage.WARNING)) ;
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        if(DEFAULT_LANGUAGE.equals(language)) { 
          if(!uiForm.getDocument().hasProperty(Utils.EXO_LANGUAGE)){
            currentDoc.addMixin("mix:i18n") ;
            currentDoc.save() ;
            language = DEFAULT_LANGUAGE ;
          } else {
            language = uiForm.getDocument().getProperty(Utils.EXO_LANGUAGE).getString() ;
          }
        }
        CommentsService commentsService = uiForm.getApplicationComponent(CommentsService.class) ; 
        try {
          commentsService.addComment(uiForm.getDocument(), name, email, website, comment, language) ;
        } catch (LockException le) {
          uiApp.addMessage(new ApplicationMessage("UICBCommentForm.msg.locked-doc", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } catch (VersionException ve) {
          uiApp.addMessage(new ApplicationMessage("UICBCommentForm.msg.versioning-doc", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UICBCommentForm.msg.error-vote", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiBCContainer) ;
      }
    }
  }
}