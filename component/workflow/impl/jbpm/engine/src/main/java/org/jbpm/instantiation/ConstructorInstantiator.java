package org.jbpm.instantiation;

import java.lang.reflect.*;
import org.apache.commons.logging.*;

public class ConstructorInstantiator implements Instantiator {
  
  private static final Class[] parameterTypes = new Class[] {String.class};

  public Object instantiate(Class clazz, String configuration) {
    Object newInstance = null;
    try {
      Constructor constructor = clazz.getDeclaredConstructor( parameterTypes );
      constructor.setAccessible(true);
      newInstance = constructor.newInstance( new Object[] { configuration } );
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
    return newInstance;
  }

  private static final Log log = LogFactory.getLog(ConstructorInstantiator.class);
}
