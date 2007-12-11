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

import org.apache.commons.logging.*;

public class DefaultInstantiator implements Instantiator {

  public Object instantiate(Class clazz, String configuration) {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
  }

  private static final Log log = LogFactory.getLog(DefaultInstantiator.class);
}
