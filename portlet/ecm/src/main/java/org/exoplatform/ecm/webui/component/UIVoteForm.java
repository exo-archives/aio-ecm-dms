/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 30, 2006
 * 10:45:01 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIVoteForm.gtmpl",
    events = {    
        @EventConfig(listeners = UIVoteForm.VoteActionListener.class),
        @EventConfig(listeners = UIVoteForm.CancelActionListener.class)
    }
)
public class UIVoteForm extends UIComponent implements UIPopupComponent {
  public UIVoteForm() throws Exception {}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public double getRating() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode().
                                                  getProperty("exo:votingRate").getDouble() ;
  }
  
  static  public class VoteActionListener extends EventListener<UIVoteForm> {
    public void execute(Event<UIVoteForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      UIDocumentInfo uiDocumentInfo = uiExplorer.findFirstComponentOfType(UIDocumentInfo.class) ;
      String language = uiDocumentInfo.getLanguage() ;
      double objId = Double.parseDouble(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiExplorer.getApplicationComponent(VotingService.class) ;
      votingService.vote(uiExplorer.getCurrentNode(), objId, userName, language) ;
      event.getSource().getAncestorOfType(UIPopupAction.class).cancelPopupAction() ;
      uiExplorer.updateAjax(event) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIVoteForm> {
    public void execute(Event<UIVoteForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).cancelPopupAction() ;
    }
  }
}
