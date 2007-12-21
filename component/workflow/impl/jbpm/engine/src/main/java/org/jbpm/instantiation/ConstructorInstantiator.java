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
package org.jbpm.instantiation;

import java.lang.reflect.*;
import org.apache.commons.logging.*;

public class ConstructorInstantiator implements Instantiator {
  
  private static final Class[] parameterTypes = new Class[] {String.class};

  public Object instantiate(Class clazz, String configuration) {
    Object newInstance = null;
    try {
      Constructor constructor = clazz.getDeclaredConstructor( parameterTypes );
      constructor.setAccessible(true);
      newInstance = constructor.newInstance( new Object[] { configuration } );
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
    return newInstance;
  }

  private static final Log log = LogFactory.getLog(ConstructorInstantiator.class);
}
