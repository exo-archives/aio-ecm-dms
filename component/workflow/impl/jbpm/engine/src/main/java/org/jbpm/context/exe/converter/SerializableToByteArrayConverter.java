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

import java.io.*;

import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.*;

public class SerializableToByteArrayConverter implements Converter {

  private static final long serialVersionUID = 1L;
  
  public boolean supports(Class clazz) {
    return Serializable.class.isAssignableFrom(clazz);
  }

  public Object convert(Object o) {
    byte[] bytes = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(o);
      oos.flush();
      bytes = baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("couldn't serialize '"+o+"'", e);
    }
    
    return new ByteArray(bytes);
  }

  public Object revert(Object o) {
    ByteArray byteArray = (ByteArray) o;
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(byteArray.getBytes());
      ObjectInputStream ois = new ObjectInputStream(bais);
      return ois.readObject();
    } catch (Exception e) {
      throw new RuntimeException("couldn't deserialize object", e);
    }
  }
}
