package org.jbpm.context.exe.converter;

import java.util.*;
import org.jbpm.context.exe.*;

public class DateToLongConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Date.class);
  }

  public Object convert(Object o) {
    return new Long(((Date)o).getTime());
  }

  public Object revert(Object o) {
    return new Date(((Long)o).longValue());
  }
}
