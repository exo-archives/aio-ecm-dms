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
