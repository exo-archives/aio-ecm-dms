/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.jcrext;

import java.util.GregorianCalendar;

import javax.jcr.Item;
import javax.jcr.Node;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Jul 11, 2007  
 */
public class AddDateTimeAction implements Action{

  public boolean execute(Context context) throws Exception {
    int eventType = (Integer)context.get("event") ;    
    Item item =(Item)context.get("currentItem") ;    
    if(eventType == ExtendedEvent.NODE_ADDED) {      
      Node node = (Node)item ;
      if(node.canAddMixin("exo:datetime")) {
        node.addMixin("exo:datetime") ;
        node.setProperty("exo:dateCreated",new GregorianCalendar()) ;
        node.setProperty("exo:dateModified",new GregorianCalendar()) ;
        return true ;
      }
    } if( eventType == ExtendedEvent.PROPERTY_ADDED 
        || eventType == ExtendedEvent.PROPERTY_CHANGED || eventType == ExtendedEvent.PROPERTY_REMOVED ) {
      // now it can't run
//      Node parent = item.getParent() ;      
//      if(parent.isNodeType("exo:datetime")) {
//        parent.setProperty("exo:dateModified",new GregorianCalendar()) ;
//      }           
    }
    return false;
  }

}
