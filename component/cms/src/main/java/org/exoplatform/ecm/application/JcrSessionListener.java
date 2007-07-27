/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.application;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Jul 24, 2007  
 */
public class JcrSessionListener implements HttpSessionListener {

  public void sessionCreated(HttpSessionEvent event) {
  }

  public void sessionDestroyed(HttpSessionEvent event) {   
    try{
      HttpSession session = event.getSession() ;      
      ServletContext context = session.getServletContext() ;    
      PortalContainer pcontainer = 
        RootContainer.getInstance().getPortalContainer(context.getServletContextName()) ;
      PortalContainer.setInstance(pcontainer) ;       
      SessionProviderService providerService = 
        (SessionProviderService)pcontainer.getComponentInstanceOfType(SessionProviderService.class) ;
//    remove all SystemSessionProvider & AnonimSessionProvider
      String systemKey = session.getId() + ":/" + SystemIdentity.SYSTEM ;
      String anonimKey = session.getId() + ":/" + SystemIdentity.ANONIM ;     
      providerService.removeSessionProvider(session.getId()) ;      
      providerService.removeSessionProvider(systemKey) ;      
      providerService.removeSessionProvider(anonimKey) ;       
      PortalContainer.setInstance(null) ;           

    }catch (Exception e) {
      e.printStackTrace();
    }finally {
      PortalContainer.setInstance(null) ;
    }    
  }

}
