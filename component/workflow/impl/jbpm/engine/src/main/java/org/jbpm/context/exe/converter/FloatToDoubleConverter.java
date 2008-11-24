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
package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.Converter;

public class FloatToDoubleConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Float.class);
  }

  public Object convert(Object o) {
    return new Double(((Float)o).doubleValue());
  }

  public Object revert(Object o) {
    return new Float(((Double)o).floatValue());
  }

}
