package org.jbpm.instantiation;

import java.lang.reflect.*;
import org.apache.commons.logging.*;

public class ConfigurationPropertyInstantiator implements Instantiator {

  private static final Class[] parameterTypes = new Class[] {String.class};

  public Object instantiate(Class clazz, String configuration) {
    Object newInstance = null;
    try {
      // create the object
      newInstance = clazz.newInstance();
      
      // set the configuration with the bean-style setter
      Method setter = clazz.getDeclaredMethod( "setConfiguration", parameterTypes );
      setter.setAccessible(true);
      setter.invoke( newInstance, new Object[]{ configuration } );
      
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
    return newInstance;
  }
  
  private static final Log log = LogFactory.getLog(ConfigurationPropertyInstantiator.class);
}
