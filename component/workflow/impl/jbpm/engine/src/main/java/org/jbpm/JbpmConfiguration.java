package org.jbpm;

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * access the configuration of jbpm from the jbpm.properties file and sets up 
 * properties. The jbpm.properties file is accessed from the classpath root or 
 * the org.jbpm package. 
 *
 */public class JbpmConfiguration {
  
  private JbpmConfiguration() {}
  
  static Properties properties = null;
 
  private static Properties getProperties() {
    if (properties==null) {
      properties = ClassLoaderUtil.getProperties("jbpm.properties", "org/jbpm");
      
      Iterator iter = properties.keySet().iterator();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        log.debug(key+"="+properties.getProperty(key));
      }
    }
    return properties;
  }

  public static String getString(String key) {
    return getProperties().getProperty(key);
  }

  public static String getString( String key, String defaultValue ) {
    return getProperties().getProperty( key, defaultValue );
  }
  
  public static long getLong( String key, long defaultValue ) {
    long value = defaultValue;
    
    String valueText = getProperties().getProperty( key );
    if ( valueText != null ) {
      try {
        value = Long.parseLong( valueText );
      } catch (NumberFormatException e) {
        throw new RuntimeException( "jbpm configuration property '" + key + "' is not parsable to a long : '" + valueText + "'", e );
      }  
    }
    
    return value;
  }
  
  public static boolean getBoolean(String key, boolean defaultValue) {
    boolean value = defaultValue;
    
    String valueText = getProperties().getProperty( key );
    if ( valueText != null ) {
      try {
        value = new Boolean( valueText ).booleanValue();
      } catch (NumberFormatException e) {
        throw new RuntimeException( "jbpm configuration property '" + key + "' is not parsable to a long : '" + valueText + "'", e );
      }  
    }
    
    return value;
  }

  public static Object getObject(String key) {
    return getObject(key, null);
  }

  public static Object getObject(String key, Object defaultValue) {
    Object instance = null;
    String className = getProperties().getProperty(key);
    if (className==null) {
      instance = defaultValue;
    } else {
      try {
        Class clazz = ClassLoaderUtil.loadClass( className );
        instance = clazz.newInstance();
      } catch (Exception e) {
        throw new RuntimeException( "couldn't instantiate class " + className + " configured in property " + key );
      }
    }
    return instance;
  }

  private static final Log log = LogFactory.getLog(JbpmConfiguration.class);
}
