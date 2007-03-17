/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr.model;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.ticket.TicketCommand;

/**
 * Created by The eXo Platform SARL
 * Author : Chung Nguyen
 *          nguyenchung136@yahoo.com
 * Jan 19, 2006
 */
public class TicketCommandImpl extends TicketCommand{
  private String typeTicketCommand;
  //private void setType(String type){  typeTicketCommand = type; }
  
  public Object execute() throws Exception {        
    RepositoryService repoService = 
      (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;    
    Map<String, String> prop = getProperties() ;
    Session session = repoService.getRepository().getSystemSession(prop.get("workspace")) ;        
    Node child = session.getRootNode().getNode(prop.get("nodePath")) ;
    return child ;
  }
  
  public String getType() throws Exception { return typeTicketCommand ;  }
}
