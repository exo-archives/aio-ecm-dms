/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL        All rights reserved.   *
 * Please look at license.txt in info directory for more license detail.   *
 ***************************************************************************/

package org.exoplatform.services.workflow.impl.jbpm;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.workflow.WorkflowFilterService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.jbpm.security.authenticator.JbpmDefaultAuthenticator;

/**
 * This is the jBPM implementation of the Workflow Filter Service.
 * It basically caches the Actor identifier and commits changes at the end.
 * 
 * Created by eXo Platform SARL
 * @author Benjamin Mestrallet
 * Sep 25, 2005
 */
public class WorkflowFilterServiceImpl implements WorkflowFilterService {

  private String portalName_;
  private boolean exceptionOccurred;

  public void init(FilterConfig filterConfig) throws ServletException {
    portalName_ = filterConfig.getServletContext().getServletContextName();
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
      FilterChain filterChain) throws IOException, ServletException {
    PortalContainer pcontainer = RootContainer.getInstance().getPortalContainer(portalName_);
    WorkflowServiceContainerImpl workflowService = (WorkflowServiceContainerImpl) pcontainer.getComponentInstanceOfType(
        WorkflowServiceContainer.class);
    String remoteUser = null;
    try {
      remoteUser = ((HttpServletRequest)servletRequest).getRemoteUser();
      if(remoteUser != null)
        JbpmDefaultAuthenticator.pushAuthenticatedActorId(remoteUser);
      filterChain.doFilter(servletRequest, servletResponse);
      // if there is an exception, this line is not executed 
      exceptionOccurred = false;
    } finally {
      if(remoteUser != null)
        JbpmDefaultAuthenticator.popAuthenticatedActorId();
      if(exceptionOccurred)
        workflowService.rollback();
      else
        workflowService.closeSession();
    }  
  }

  public void destroy() {
  }

}
