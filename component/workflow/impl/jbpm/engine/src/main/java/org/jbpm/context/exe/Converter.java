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
package org.jbpm.context.exe;

import java.io.*;

/**
 * converts plain objects to objects that are 
 * persistable via a subclass of VariableInstance. 
 */
public interface Converter extends Serializable {

  /**
   * is true if this converter supports the given type, false otherwise.
   */
  boolean supports(Class clazz);
  
  /**
   * converts a given object to its persistable format.
   */
  Object convert(Object o);

  /**
   * reverts a persisted object to its original form.
   */
  Object revert(Object o);
}
