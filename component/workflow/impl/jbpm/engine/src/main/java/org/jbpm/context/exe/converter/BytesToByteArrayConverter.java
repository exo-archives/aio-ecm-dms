package org.jbpm.context.exe.converter;

import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.Converter;

public class BytesToByteArrayConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==byte[].class);
  }

  public Object convert(Object o) {
    return new ByteArray((byte[]) o);
  }

  public Object revert(Object o) {
    return ((ByteArray)o).getBytes();
  }

}
