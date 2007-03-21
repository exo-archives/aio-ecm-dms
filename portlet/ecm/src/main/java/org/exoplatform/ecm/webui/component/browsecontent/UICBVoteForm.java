/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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

  public Node getDocument() { 
    UIBrowseContentPortlet portlet = getAncestorOfType(UIBrowseContentPortlet.class) ;
    UIDocumentDetail uiDocumentDetail = portlet.findFirstComponentOfType(UIDocumentDetail.class) ;
    return uiDocumentDetail.getNode();
  }
 
  public double getRating() throws Exception {
    return  getDocument().getProperty("exo:votingRate").getDouble();
  }

  public void activate() throws Exception { }
  
  public void deActivate() throws Exception { }

  static  public class VoteActionListener extends EventListener<UICBVoteForm> {
    public void execute(Event<UICBVoteForm> event) throws Exception {
      UICBVoteForm uiForm = event.getSource() ;
      String userName = Util.getUIPortal().getOwner() ;
      long objId = Long.parseLong(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiForm.getApplicationComponent(VotingService.class) ;
      votingService.vote(uiForm.getDocument(), objId, userName) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }
 
  static  public class CancelActionListener extends EventListener<UICBVoteForm> {
    public void execute(Event<UICBVoteForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).cancelPopupAction() ;
    }
  }
}