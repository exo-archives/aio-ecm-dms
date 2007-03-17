package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.*;

public class ByteToLongConverter implements Converter {
  
  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Byte.class);
  }

  public Object convert(Object o) {
    return new Long( ((Number)o).longValue() );
  }

  public Object revert(Object o) {
    return new Byte(((Long)o).byteValue());
  }
}