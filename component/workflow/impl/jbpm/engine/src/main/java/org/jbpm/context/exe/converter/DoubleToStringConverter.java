package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.*;

public class DoubleToStringConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Double.class);
  }

  public Object convert(Object o) {
    return o.toString();
  }

  public Object revert(Object o) {
    return new Double((String)o);
  }

}
