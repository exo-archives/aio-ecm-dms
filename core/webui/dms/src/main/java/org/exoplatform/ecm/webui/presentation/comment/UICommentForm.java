/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.presentation.comment;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Vu
 *          vuna@exoplatform.com
 *          anhvurz90@gmail.com
 * Jul 14, 2011  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "system:/groovy/webui/form/UIForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UICommentForm.SaveActionListener.class),
                   @EventConfig(listeners = UICommentForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
             ) 

public abstract class UICommentForm extends UIForm implements UIPopupComponent {

  public static final String FIELD_EMAIL = "email" ;
  public static final String FIELD_WEBSITE = "website" ;
  public static final String FIELD_COMMENT = "comment" ;
  
  private static final Log LOG = ExoLogger.getLogger(UICommentForm.class); 

  protected Node document_ ;

  private boolean edit;
  
  protected String nodeCommentPath;
  
  public abstract String getLanguage();
  public abstract Node getCommentNode() throws Exception;
  public abstract Node getCurrentNode() throws Exception;
  public abstract void updateAjax(Event<UICommentForm> event) throws Exception;
  
  public boolean isEdit() {
    return edit;
  }

  public void setEdit(boolean edit) {
    this.edit = edit;
  }

  public String getNodeCommentPath() {
    return nodeCommentPath;
  }

  public void setNodeCommentPath(String nodeCommentPath) {
    this.nodeCommentPath = nodeCommentPath;
  }

  public UICommentForm() throws Exception {
  }
 
  private void prepareFields() throws Exception{
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    String userName = requestContext.getRemoteUser();
    if(userName == null || userName.length() == 0){
      addUIFormInput(new UIFormStringInput(FIELD_EMAIL, FIELD_EMAIL, null).addValidator(EmailAddressValidator.class)) ;
      addUIFormInput(new UIFormStringInput(FIELD_WEBSITE, FIELD_WEBSITE, null)) ;
    } 
    addUIFormInput(new UIFormWYSIWYGInput(FIELD_COMMENT, FIELD_COMMENT, null).addValidator(MandatoryValidator.class)) ;
    if (isEdit()) {
      Node comment = getCommentNode();
      if(comment.hasProperty("exo:commentContent")){
        getChild(UIFormWYSIWYGInput.class).setValue(comment.getProperty("exo:commentContent").getString());
      }
    }
  }
  
  public void activate() throws Exception {
    document_ = getCurrentNode();
    prepareFields();
  }  
  
  public void deActivate() throws Exception {
    document_ = null ;
  }  
  
  public Node getDocument() { return document_ ; }
  public void setDocument(Node doc) { document_ = doc ; }
  
  public static class CancelActionListener extends EventListener<UICommentForm>{
    public void execute(Event<UICommentForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupContainer.class).cancelPopupAction() ;
    }
  }  
  
  public static class SaveActionListener extends EventListener<UICommentForm>{
    public void execute(Event<UICommentForm> event) throws Exception {
      UICommentForm uiForm = event.getSource();
      CommentsService commentsService = uiForm.getApplicationComponent(CommentsService.class);
      String comment = (String) uiForm.<UIFormInputBase> getUIInput(FIELD_COMMENT).getValue();
      if (comment == null || comment.trim().length() == 0) {
        throw new MessageException(new ApplicationMessage("UICommentForm.msg.content-null", null,
            ApplicationMessage.WARNING));
      }
      if (uiForm.isEdit()) {
        Node commentNode = uiForm.getCommentNode();
        commentsService.updateComment(commentNode, comment);
      } else {
        String userName = event.getRequestContext().getRemoteUser();
        String website = null;
        String email = null;
        if (userName == null || userName.length() == 0) {
          userName = "anonymous";
          website = uiForm.getUIStringInput(FIELD_WEBSITE).getValue();
          email = uiForm.getUIStringInput(FIELD_EMAIL).getValue();
        } else {
          OrganizationService organizationService = uiForm
              .getApplicationComponent(OrganizationService.class);
          UserProfileHandler profileHandler = organizationService.getUserProfileHandler();
          UserHandler userHandler = organizationService.getUserHandler();
          User user = userHandler.findUserByName(userName);
          UserProfile userProfile = profileHandler.findUserProfileByName(userName);
          website = userProfile.getUserInfoMap().get("user.business-info.online.uri");
          email = user.getEmail();
        }
        try {
          String language = uiForm.getLanguage();
          commentsService.addComment(uiForm.document_, userName, email, website, comment, language);
        } catch (Exception e) {
          LOG.error(e);
        }
      }
      uiForm.updateAjax(event);
    }
  }

}
