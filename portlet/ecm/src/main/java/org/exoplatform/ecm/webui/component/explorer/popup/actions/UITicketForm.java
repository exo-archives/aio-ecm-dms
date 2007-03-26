/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.jcr.model.TicketCommandImpl;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.portletcontainer.ExoPortletRequest;
import org.exoplatform.services.ticket.TicketCommand;
import org.exoplatform.services.ticket.TicketService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormDateTimeInput;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmailAddressValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn 
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor :lebienthuy
 *        :pham tuan Oct 27, 2006
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UITicketForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UITicketForm.CancelActionListener.class),
      @EventConfig(listeners = UITicketForm.UpdateActionListener.class)
    }
)

public class UITicketForm extends UIForm implements UIPopupComponent {

  final static public String FIELD_USERS = "users" ;
  final static public String FIELD_PERMISSION = "permission" ;
  final static public String FIELD_CREATOR = "creator" ;
  final static public String FIELD_ACCESSTIME = "accessTime" ;
  final static public String FIELD_ACCESSLIMIT = "accessLimit" ;
  final static public String FIELD_DESCRIPTION = "description" ;
  final static public String FIELD_SENDTOMAIL = "sendToMail" ; 
  final static public String FIELD_TICKET_ID = "ticketID" ;
  final static public String DEFAULT_HOST = "http://localhost:8080/cms-content/" ;
  final static public String DEFAULT_REQUEST_STRING = "/ticket?ticketId=" ;
  final static public String DEFAULT_REQUEST_PORTAL_ID = "&portalId=" ;

  private boolean create = false, update = false ;

  public UITicketForm() throws Exception {
    GregorianCalendar calendar = new GregorianCalendar();    
    calendar.set(GregorianCalendar.HOUR_OF_DAY, 0) ;
    calendar.set(GregorianCalendar.MINUTE, 0) ;
    calendar.set(GregorianCalendar.SECOND, 0) ;
    calendar.set(GregorianCalendar.MILLISECOND, 0) ;
    calendar.set(GregorianCalendar.HOUR_OF_DAY, 24) ;
    addUIFormInput(new UIFormStringInput(FIELD_USERS, FIELD_USERS, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION , null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_CREATOR, FIELD_CREATOR, Util.getUIPortal().getOwner())) ;    
    addUIFormInput(new UIFormDateTimeInput(FIELD_ACCESSTIME, FIELD_ACCESSTIME, calendar.getTime())) ;    
    addUIFormInput(new UIFormStringInput(FIELD_ACCESSLIMIT, FIELD_ACCESSLIMIT, "-1")) ;
    addUIFormInput(new UIFormStringInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;    
    addUIFormInput(new UIFormStringInput(FIELD_SENDTOMAIL, FIELD_SENDTOMAIL, null).
                                        addValidator(EmailAddressValidator.class)) ;
    addUIFormInput(new UIFormStringInput(FIELD_TICKET_ID, FIELD_TICKET_ID, null)) ;  
    setActions(new String[]{"Save", "Cancel"}) ;
  }

  public void activate() throws Exception {
    reset() ;
    getUIStringInput(FIELD_CREATOR).setValue(Util.getUIPortal().getOwner())  ;
    GregorianCalendar calendar = new GregorianCalendar() ;    
    calendar.set(GregorianCalendar.MONTH,calendar.get(GregorianCalendar.MONTH) + 1) ;
    getUIStringInput(FIELD_ACCESSLIMIT).setValue("-1") ;
    getUIStringInput(FIELD_TICKET_ID).setValue(DEFAULT_HOST) ;
    create = false ;
    update = true ;    
  } 

  public void deActivate() throws Exception {}

  private void lockForm() {      
    getUIStringInput(FIELD_USERS).setEditable(false) ;
    getUIStringInput(FIELD_PERMISSION).setEditable(false) ;
    getUIStringInput(FIELD_CREATOR).setEditable(false) ;
    getUIFormDateTimeInput(FIELD_ACCESSTIME).setEnable(false) ;
    getUIStringInput(FIELD_ACCESSLIMIT).setEditable(false) ;
    getUIStringInput(FIELD_DESCRIPTION).setEditable(false) ;
    getUIStringInput(FIELD_SENDTOMAIL).setEditable(false) ; 
    getUIStringInput(FIELD_TICKET_ID).setEditable(false) ;
    create = true ;
    update = false ;
    setActions(new String[]{"Update", "Cancel"}) ;
  }

  private void unLockForm() {
    getUIStringInput(FIELD_USERS).setEditable(true) ;
    getUIStringInput(FIELD_PERMISSION).setEditable(true) ;
    getUIStringInput(FIELD_CREATOR).setEditable(true) ;
    getUIFormDateTimeInput(FIELD_ACCESSTIME).setEnable(true) ;
    getUIStringInput(FIELD_ACCESSLIMIT).setEditable(true) ;
    getUIStringInput(FIELD_DESCRIPTION).setEditable(true) ;
    getUIStringInput(FIELD_SENDTOMAIL).setEditable(true) ;         
    getUIStringInput(FIELD_TICKET_ID).setValue("") ; 
    create = false ;
    update = true ;
    setActions(new String[]{"Save", "Cancel"}) ;
  }    
  
  @SuppressWarnings("unused")
  static  public class SaveActionListener extends EventListener<UITicketForm> {
    public void execute(Event<UITicketForm> event) throws Exception {
      UITicketForm uiTicketForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiTicketForm.getAncestorOfType(UIJCRExplorer.class) ;
      PortalContainer pcontainer =  PortalContainer.getInstance() ;
      String portalName = pcontainer.getPortalContainerInfo().getContainerName() ;      
      String users = uiTicketForm.getUIStringInput(FIELD_USERS).getValue() ;      
      if(users == "") users = null ;
      String permission=uiTicketForm.getUIStringInput(FIELD_PERMISSION).getValue() ;
      if(permission == "") permission = null ;
      String creator = uiTicketForm.getUIStringInput(FIELD_CREATOR).getValue() ;
      if(creator == "") creator = null ;
      String description = uiTicketForm.getUIStringInput(FIELD_DESCRIPTION).getValue() ;
      if(description == "") description = null ;
      String mail = uiTicketForm.getUIStringInput(FIELD_SENDTOMAIL).getValue() ;
      long accesstime = uiTicketForm.getUIFormDateTimeInput(FIELD_ACCESSTIME).getCalendar().
                                     getTimeInMillis() - new Date().getTime() ; 
      int accesslimit ;
      try{
        accesslimit = Integer.parseInt(uiTicketForm.getUIStringInput(FIELD_ACCESSLIMIT).getValue()) ;
      } catch (NumberFormatException e) {
        throw new Exception("Input only number") ;
      }
      TicketService service  = uiTicketForm.getApplicationComponent(TicketService.class) ;
      Session session = uiExplorer.getSession() ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      Property prop = currentNode.getProperty("jcr:primaryType") ;
      String nodeType = prop.getValue().toString() ;      
      String nodePath = currentNode.getPath().substring(1) ; 
      TicketCommand tc = new TicketCommandImpl() ;
      StringBuilder properties = new StringBuilder() ;
      properties.append("nodeType=").append(nodeType).append("\nnodePath").append(nodePath).
                 append("\nworkspace=").append(session.getWorkspace().getName()).
                 append("\nportalName=").append(portalName) ;
      tc.setProperty(TicketCommand.PROPERTIES_NAME, properties.toString()) ;

      if((!uiTicketForm.create) && (accesstime > 0)){        
        String ticketId = service.createTicket(accesstime, accesslimit, description, creator, 
                                               null, null, tc) ;
        uiTicketForm.getUIStringInput(FIELD_TICKET_ID).setValue(getUrl(ticketId, portalName)) ;
        uiTicketForm.lockForm() ;
      }
    }

    public String getUrl(String ticketId,String portalId) throws Exception {    
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      ExoPortletRequest request = (ExoPortletRequest) context.getRequest();
      String hostName = request.getServerName() ;
      String port = Integer.toString(request.getServerPort()) ;
      String protocol = request.getScheme() ;
      String contextPath = request.getContextPath() ;      
      StringBuilder url = new StringBuilder() ;
      url.append(DEFAULT_HOST).append(DEFAULT_REQUEST_STRING).append(ticketId).
          append(DEFAULT_REQUEST_PORTAL_ID).append(portalId) ;      
      return url.toString() ;
    }
  }

  static public class CancelActionListener extends EventListener<UITicketForm> {
    public void execute(Event<UITicketForm> event) throws Exception {
      UITicketForm uiTicketForm = event.getSource() ;      
      uiTicketForm.reset() ;
      UIJCRExplorer uiExplorer = uiTicketForm.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;     
    }
  }


  static public class UpdateActionListener extends EventListener<UITicketForm> {
    public void execute(Event<UITicketForm> event) throws Exception {
      UITicketForm uiTicketForm = event.getSource() ;
      if(!uiTicketForm.update) uiTicketForm.unLockForm() ;
    }      
  }
}