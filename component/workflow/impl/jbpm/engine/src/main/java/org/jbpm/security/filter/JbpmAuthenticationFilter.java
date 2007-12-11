/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
