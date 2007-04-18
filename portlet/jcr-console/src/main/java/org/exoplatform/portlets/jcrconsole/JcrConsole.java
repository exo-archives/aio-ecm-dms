/*
 * Copyright 2001-2006 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */
 
package org.exoplatform.portlets.jcrconsole;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.SecurityService;

public class JcrConsole extends GenericPortlet {

  public static final String SESSION_CONTAINER = "exoplatform.exocontainer";
  
  private static final String JSP_CODE = "/WEB-INF/console.jsp";
  
 
  protected void doView(RenderRequest renderRequest,
      RenderResponse renderResponse) throws PortletException, IOException {


    ExoContainer container = ExoContainerContext.getCurrentContainer();
    String cont_name = (String)container.getContext().getName();
    
    //SecurityService securityService = (SecurityService) container.getComponentInstanceOfType(SecurityService.class);
    //securityService.setCurrentUser(renderRequest.getRemoteUser());
    
    //renderRequest.getPortletSession().setAttribute(SESSION_CONTAINER,cont_name);
    renderRequest.setAttribute(SESSION_CONTAINER,cont_name);
    
    renderResponse.setContentType("text/html; charset=UTF-8");
    PrintWriter w = renderResponse.getWriter();
    PortletURL actionURL = renderResponse.createActionURL();
    PortletContext context = getPortletContext();
    
    PortletRequestDispatcher rd = context.getRequestDispatcher(JSP_CODE);
    rd.include(renderRequest, renderResponse);

    
  }

  public void processAction(ActionRequest actionRequest,
      ActionResponse actionResponse) throws PortletException, IOException {

  }

}