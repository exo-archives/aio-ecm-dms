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

import org.exoplatform.services.log.Log;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentContainer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
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
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

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
  
  private static final Log LOG = ExoLogger.getLogger(UICommentForm.class); 

  private boolean edit;
  
  private String nodeCommentPath;
  
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
  
  private Node document_ ;
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
      Node comment = getAncestorOfType(UIJCRExplorer.class).getNodeByPath(nodeCommentPath, document_.getSession());
      if(comment.hasProperty("exo:commentContent")){
        getChild(UIFormWYSIWYGInput.class).setValue(comment.getProperty("exo:commentContent").getString());
      }
    }
  }
 
  public void activate() throws Exception {
    document_ = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
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
      UICommentForm uiForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      String comment = uiForm.getChild(UIFormWYSIWYGInput.class).getValue() ;
      CommentsService commentsService = uiForm.getApplicationComponent(CommentsService.class) ; 
      if(comment == null || comment.trim().length() == 0) {
        throw new MessageException(new ApplicationMessage("UICommentForm.msg.content-null", null, ApplicationMessage.WARNING)) ;
      }
      if (uiForm.isEdit()) {
        Node commentNode = uiExplorer.getNodeByPath(uiForm.getNodeCommentPath(), uiForm.document_.getSession());
        commentsService.updateComment(commentNode, comment);
      } else {
        String userName = event.getRequestContext().getRemoteUser() ;
        String website = null;
        String email = null;
        if(userName == null || userName.length() == 0){ 
          userName = "anonymous" ;
          website = uiForm.getUIStringInput(FIELD_WEBSITE).getValue() ;
          email = uiForm.getUIStringInput(FIELD_EMAIL).getValue() ;
        }else{
          OrganizationService organizationService = uiForm.getApplicationComponent(OrganizationService.class);
            UserProfileHandler profileHandler = organizationService.getUserProfileHandler();
            UserHandler userHandler = organizationService.getUserHandler();
            User user = userHandler.findUserByName(userName);
            UserProfile userProfile = profileHandler.findUserProfileByName(userName);
            website = userProfile.getUserInfoMap().get("user.business-info.online.uri");
            email = user.getEmail();
        }
        
        try {
          String language = uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).
          getChild(UIDocumentContainer.class).getChild(UIDocumentInfo.class).getLanguage() ;
          commentsService.addComment(uiForm.document_, userName, email, website, comment, language);
        } catch (Exception e) {        
          LOG.error(e);
        }  
      }
      UIPopupWindow uiPopup = uiExplorer.getChildById("ViewSearch");
      if (uiPopup != null) 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      uiExplorer.updateAjax(event) ;
    }
  }
}