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
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@gmail.com
 * Jan 31, 2007  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIVoteForm.gtmpl",
    events = {    
        @EventConfig(listeners = UICBVoteForm.VoteActionListener.class),
        @EventConfig(listeners = UICBVoteForm.CancelActionListener.class)
    }
) 

public class UICBVoteForm extends UIComponent implements UIPopupComponent{
  public UICBVoteForm() {}

  public Node getDocument() throws Exception { 
    UIBrowseContentPortlet portlet = getAncestorOfType(UIBrowseContentPortlet.class) ;
    UIBrowseContainer uiBCContainer =  portlet.findFirstComponentOfType(UIBrowseContainer.class) ;
    UIDocumentDetail uiDocumentDetail = uiBCContainer.findFirstComponentOfType(UIDocumentDetail.class) ;
    return uiDocumentDetail.node_;
  }

  public double getRating() throws Exception {
    return  getDocument().getProperty("exo:votingRate").getDouble();
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static  public class VoteActionListener extends EventListener<UICBVoteForm> {
    public void execute(Event<UICBVoteForm> event) throws Exception {
      UICBVoteForm uiForm = event.getSource() ;
      UIBrowseContentPortlet uiPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer uiBCContainer = uiPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocumentDetail = uiBCContainer.getChild(UIDocumentDetail.class) ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      long objId = Long.parseLong(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiForm.getApplicationComponent(VotingService.class) ;
      String language = uiDocumentDetail.getLanguage() ;
      Node currentDoc = uiForm.getDocument() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(language == null && currentDoc.hasProperty(Utils.EXO_LANGUAGE)) {
        language = currentDoc.getProperty(Utils.EXO_LANGUAGE).getValue().getString() ;
      }
      try {
        votingService.vote(currentDoc, objId, userName, language) ;
      } catch (LockException le) {
        uiApp.addMessage(new ApplicationMessage("UICBVoteForm.msg.locked-doc", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UICBVoteForm.msg.versioning-doc", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UICBVoteForm.msg.error-vote", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiBCContainer) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UICBVoteForm> {
    public void execute(Event<UICBVoteForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).cancelPopupAction() ;
    }
  }
}