package org.jbpm.context.exe;

import java.io.*;

/**
 * converts plain objects to objects that are 
 * persistable via a subclass of VariableInstance. 
 */
public interface Converter extends Serializable {

  /**
   * is true if this converter supports the given type, false otherwise.
   */
  boolean supports(Class clazz);
  
  /**
   * converts a given object to its persistable format.
   */
  Object convert(Object o);

  /**
   * reverts a persisted object to its original form.
   */
  Object revert(Object o);
}
