package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.*;

public class IntegerToLongConverter implements Converter {
  
  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Integer.class);
  }

  public Object convert(Object o) {
    return new Long( ((Number)o).longValue() );
  }

  public Object revert(Object o) {
    return new Integer(((Long)o).intValue());
  }
}
