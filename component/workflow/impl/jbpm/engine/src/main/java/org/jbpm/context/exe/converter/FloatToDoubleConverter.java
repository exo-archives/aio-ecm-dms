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
