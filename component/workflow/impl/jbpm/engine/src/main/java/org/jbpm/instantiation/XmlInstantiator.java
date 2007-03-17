package org.jbpm.instantiation;

import java.lang.reflect.*;
import org.apache.commons.logging.*;
import org.dom4j.*;

public class XmlInstantiator implements Instantiator {

  private static final Class[] parameterTypes = new Class[] {Element.class};

  public Object instantiate(Class clazz, String configuration) {
    Object newInstance = null;
    try {
      // parse the bean configuration
      Element configurationElement = parseConfiguration(configuration);

      Constructor constructor = clazz.getDeclaredConstructor( parameterTypes );
      constructor.setAccessible(true);
      newInstance = constructor.newInstance( new Object[] { configurationElement } );
    } catch (Exception e) {
      log.error( "couldn't instantiate '" + clazz.getName() + "'", e );
      throw new RuntimeException( e );
    }
    return newInstance;
  }
  
  protected Element parseConfiguration(String configuration) {
    Element element = null;
    try {
      element = DocumentHelper.parseText( "<action>"+configuration+"</action>" ).getRootElement();
    } catch (DocumentException e) {
      log.error( "couldn't parse bean configuration : " + configuration, e );
      throw new RuntimeException(e);
    }
    return element;
  }

  private static final Log log = LogFactory.getLog(XmlInstantiator.class);
}
