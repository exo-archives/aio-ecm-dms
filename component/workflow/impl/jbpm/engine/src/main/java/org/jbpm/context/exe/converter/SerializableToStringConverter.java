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
import org.apache.commons.codec.binary.*;
import org.jbpm.context.exe.*;

public class SerializableToStringConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (Serializable.class.isAssignableFrom(clazz));
  }

  private Object getBase64() {
    try {
      return new Base64();
    } catch (RuntimeException e) {
      throw new RuntimeException("for storing serializable objects as variables, you need to put the commons-codec-x.x.jar in the classpath", e);
    }
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
    
    // encode the value using base64 encoding
    bytes = ((Base64)getBase64()).encode(bytes);
    // create a string with the encoded value
    return new String(bytes);
  }

  public Object revert(Object o) {
    // get the value of the text
    byte[] bytes = ((String)o).getBytes();
    // decode the value of the serializedValue
    bytes = ((Base64)getBase64()).decode(bytes);
    
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bais);
      return ois.readObject();
    } catch (Exception e) {
      throw new RuntimeException("couldn't deserialize object", e);
    }
  }
}
