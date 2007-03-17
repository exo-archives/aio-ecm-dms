package org.jbpm.security.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jbpm.security.Authentication;

public class JbpmAuthenticationFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    String actorId = null;

    // see if we can get the authenticated swimlaneActorId
    if (servletRequest instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      Principal userPrincipal = httpServletRequest.getUserPrincipal();
      if (userPrincipal != null) {
        actorId = userPrincipal.getName();
      }
    }

    // if there is an authenticated user
    if (actorId != null) {
      // we put the handling of the request in an authenticated block
      Authentication.pushAuthenticatedActorId(actorId);
      try {
        filterChain.doFilter(servletRequest, servletResponse);
      } finally {
        Authentication.popAuthenticatedActorId();
      }
      
    // if this request is not authenticated, we proceed without jbpm authentication
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  public void destroy() {
  }

}
