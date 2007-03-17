/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL        All rights reserved.   *
 * Please look at license.txt in info directory for more license detail.   *
 ***************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.exoplatform.services.workflow.WorkflowFilterService;

/**
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 29, 2006
 */
public class WorkflowFilterServiceImpl implements WorkflowFilterService {

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    // Currently do nothing for Bonita
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain)
      throws IOException, ServletException {
    
    // Currently do nothing for Bonita
    chain.doFilter(request, response);
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    // Currently do nothing for Bonita
  }

}
