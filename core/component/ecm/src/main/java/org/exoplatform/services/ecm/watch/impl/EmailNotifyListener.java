/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.watch.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.watch.WatchDocumentService;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Jun 2, 2008  
 */
public class EmailNotifyListener implements EventListener {
  private Node observedNode_ ;
  final public static String EMAIL_WATCHERS_PROP = "exo:emailWatcher".intern() ;
  
  public EmailNotifyListener(Node oNode) {
    this.observedNode_ = oNode;
  }
  
  public void onEvent(EventIterator arg0) {   
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    MailService mailService = (MailService)container.getComponentInstanceOfType(MailService.class);
    WatchDocumentServiceImpl watchService = (WatchDocumentServiceImpl)container.getComponentInstanceOfType(WatchDocumentService.class);
    MessageConfig messageConfig = watchService.getMessageConfig();
    List<String> emailList = getEmailList(observedNode_);
    for (String receiver : emailList) {
      Message message = createMessage(receiver, messageConfig);
      try {        
        mailService.sendMessage(message);
        System.out.println("\n\n----------------------Message: " + message.getBody() + "to " + message.getTo() + " sent!\n\n");
      } catch (Exception e) {        
        e.printStackTrace() ;
      }
    }
  }
  
  private List<String> getEmailList(Node observedNode) {
    List<String> emailList = new ArrayList<String>();
    ExoContainer container = ExoContainerContext.getCurrentContainer();    
    OrganizationService orgService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
    try {
      if (observedNode.hasProperty(EMAIL_WATCHERS_PROP)) {
        Value[] values = observedNode.getProperty(EMAIL_WATCHERS_PROP).getValues();
        for (Value value : values) {
          String username = value.getString();
          User user = orgService.getUserHandler().findUserByName(username);
          if (user != null) {
            emailList.add(user.getEmail());
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }    
    return emailList;   
  }
  
  private Message createMessage(String receiver, MessageConfig messageConfig) {
    Message message = new Message(); 
    message.setFrom(messageConfig.getSender());
    message.setTo(receiver);
    message.setSubject(messageConfig.getSubject());
    message.setBody(messageConfig.getContent());
    message.setMimeType(messageConfig.getMimeType());    
    return message;    
  }
}
