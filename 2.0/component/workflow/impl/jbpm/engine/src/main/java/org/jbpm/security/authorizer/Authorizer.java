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
package org.jbpm.security.authorizer;

import java.security.*;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

/**
 * defines the interface for plugging in authorization mechanisms into jBPM.
 * 
 * <p>
 * If Authroization implementations need the current authenticated user, they 
 * can look that up via {@link org.jbpm.security.Authentication#getAuthenticatedActorId()}.
 * </p>
 */
public interface Authorizer {

  /**
   * verify if the currently authenticated has the given permission.
   * @throws AccessControlException if the current authenticated actor is not 
   * authorized.  
   */
  void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token)
  throws AccessControlException;
}
