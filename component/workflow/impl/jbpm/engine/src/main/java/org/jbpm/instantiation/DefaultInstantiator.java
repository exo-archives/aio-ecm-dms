package org.jbpm.instantiation;

import org.apache.commons.logging.*;

public class DefaultInstantiator implements Instantiator {

  public Object instantiate(Class clazz, String configuration) {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
  }

  private static final Log log = LogFactory.getLog(DefaultInstantiator.class);
}
