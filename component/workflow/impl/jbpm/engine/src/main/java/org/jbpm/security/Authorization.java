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
package org.jbpm.security;

import java.security.*;

import org.jbpm.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.security.authorizer.*;

/**
 * provides central access point to the configurable authorization.
 * <p>
 * By default, jBPM does not do authorization checking.  To activate 
 * authorization checking, set the property 'jbpm.authorizer' to the 
 * fully qualified class name of an {@link org.jbpm.security.authorizer.Authorizer}
 * implementation.
 * </p>
 * <p>
 * When an {@link org.jbpm.security.authorizer.Authorizer} is configured, 
 * the jBPM code will call its checkPermission method for security sensitive 
 * workflow operations.  The workflow operation being performed will be indicated 
 * with a java.security.Permission.
 * </p>
 * <p>
 * /*
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
package org.jbpm.security.authorizer contains a number of {@link org.jbpm.security.authorizer.Authorizer}
 * implementations and /*
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
package org.jbpm.security.permission contains the 
 * permissions that are checked by the jBPM code.
 * </p>
 */ 
public class Authorization {
  
  protected static Authorizer authorizer = (Authorizer) JbpmConfiguration.getObject("jbpm.authorizer", null);

  /**
   * central method called by the jBPM code to check authorization.  This method
   * will delegate to the configured {@link Authorizer}
   * @throws AccessControlException if the current authenticated actor is not 
   * authorized.  
   */
  public static void checkPermission(Permission permission, ProcessDefinition processDefinition, Token token) {
    if (authorizer!=null) {
      authorizer.checkPermission(permission, processDefinition, token);
    }
  }
}
