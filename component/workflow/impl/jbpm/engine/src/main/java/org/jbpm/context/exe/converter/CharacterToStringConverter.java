package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.*;

public class CharacterToStringConverter implements Converter {

  private static final long serialVersionUID = 1L;

  public boolean supports(Class clazz) {
    return (clazz==Character.class);
  }

  public Object convert(Object o) {
    return o.toString();
  }

  public Object revert(Object o) {
    return new Character(((String)o).charAt(0));
  }
}
