/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.watch.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.watch.WatchDocumentService;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 6, 2006  
 */
public class EmailNotifyListener implements EventListener {  
  
  private Node observedNode_ ;
  final public static String EMAIL_WATCHERS_PROP = "exo:emailWatcher".intern() ;
  
  public EmailNotifyListener(Node oNode) {
    observedNode_ = oNode ;
  }
  
  public void onEvent(EventIterator arg0) {
    MailService mailService = 
      (MailService)PortalContainer.getComponent(MailService.class) ;
    WatchDocumentServiceImpl watchService= 
      (WatchDocumentServiceImpl)PortalContainer.getComponent(WatchDocumentService.class) ;
    MessageConfig messageConfig = watchService.getMessageConfig() ;
    List<String> emailList = getEmailList(observedNode_) ;
    for(String receiver: emailList) {      
      Message message = createMessage(receiver,messageConfig) ;
      try {
        mailService.sendMessage(message) ; 
      }catch (Exception e) {
        System.out.println("===> Exeption when send message to: " + message.getTo());
        //e.printStackTrace() ;
        
      }      
    }
  }
  
  private Message createMessage(String receiver, MessageConfig messageConfig) {
    Message message = new Message() ;
    message.setFrom(messageConfig.getSender()) ;
    message.setTo(receiver) ;
    message.setSubject(messageConfig.getSubject()) ;
    message.setBody(messageConfig.getContent()) ;
    message.setMimeType(messageConfig.getMimeType()) ;
    return message ;
  }
  
  private List<String> getEmailList(Node observedNode) {
    List<String> emailList = new ArrayList<String>() ;
    OrganizationService orgService = 
      (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    
    try{
      if(observedNode.hasProperty(EMAIL_WATCHERS_PROP)) {
        Value[] watcherNames = observedNode.getProperty(EMAIL_WATCHERS_PROP).getValues() ;
        for(Value value: watcherNames) {  
          String userName = value.getString() ;
          User user = orgService.getUserHandler().findUserByName(userName) ;
          if(user != null) {
            emailList.add(user.getEmail()) ;
          }
        }
      } 
    }catch (Exception e) {
    }
    return emailList ;
  }
}
  
