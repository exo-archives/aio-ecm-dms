/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL        All rights reserved.   *
 * Please look at license.txt in info directory for more license detail.   *
 ***************************************************************************/
package org.exoplatform.services.workflow;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

/**
 * This Filter looks up for the Workflow Filter Service and delegates the call
 * to it. This enables to customize the processing based on the Workflow
 * implementation without having to provide different versions of web.xml.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 29, 2006
 */
public class WorkflowFilter implements Filter {
  
  /** Reference to the Workflow Filter Service */
  private WorkflowFilterService filterService = null;

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    
    // Get the Portal name in which the Filter is invoked
    String portalName = filterConfig.getServletContext().
      getServletContextName();
    
    // Look up the reference to the Workflow Filter Service
    PortalContainer portalContainer = RootContainer.
      getInstance().getPortalContainer(portalName);
    filterService = (WorkflowFilterService)
      portalContainer.getComponentInstanceOfType(WorkflowFilterService.class);
    
    // Delegate the call to the Workflow Filter Service
    filterService.init(filterConfig);
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest  request,
                       ServletResponse response,
                       FilterChain     chain)
    throws IOException, ServletException {
    
    // Delegate the call to the Workflow Filter Service
    filterService.doFilter(request, response, chain);
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    
    // Delegate the call to the Workflow Filter Service
    filterService.destroy();
  }
}
