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
package org.jbpm.security.authenticator;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.jbpm.JbpmConfiguration;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * gets the authenticated actor id from the current Subject.
 * This Authenticator requires another configuration parameter 
 * 'jbpm.authenticator.principal.classname'.  This configuration property 
 * specifies the class name of the principal that should be used from 
 * the current subject.  The name of that principal is used as the 
 * currently authenticated actorId. 
 */
public class SubjectAuthenticator implements Authenticator {
  
  private static final String principalClassName = JbpmConfiguration.getString("jbpm.authenticator.principal.classname");
  private static Class principalClass = ClassLoaderUtil.loadClass(principalClassName);

  public String getAuthenticatedActorId() {
    String authenticatedActorId = null;
    Subject subject = Subject.getSubject(AccessController.getContext());
    Set principals = subject.getPrincipals(principalClass);
    if ( (principals!=null)
         && (!principals.isEmpty()) 
       ) {
      Principal principal = (Principal) principals.iterator().next();
      authenticatedActorId = principal.getName();
    }
    return authenticatedActorId;
  }
}
