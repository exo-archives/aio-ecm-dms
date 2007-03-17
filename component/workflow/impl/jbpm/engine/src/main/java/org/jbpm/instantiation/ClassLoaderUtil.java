package org.jbpm.instantiation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jbpm.graph.def.ProcessDefinition;

/**
 * provides centralized classloader lookup. 
 */
public class ClassLoaderUtil {

  public static Class loadClass(String className) {
    try {
      return getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("class not found '"+className+"'", e);
    }
  }
  
  public static ClassLoader getClassLoader() {
    // if this is made configurable, make sure it's done with the
    // jvm system properties
    // System.getProperty("jbpm.classloader")
    //  - 'jbpm'
    //  - 'context'
    return ClassLoaderUtil.class.getClassLoader();
  }
  
  public static InputStream getStream(String resource) {
    return getClassLoader().getResourceAsStream(resource);
  }

  /**
   * searches the given resource, first on the root of the classpath and if not 
   * not found there, in the given directory.
   */
  public static InputStream getStream(String resource, String directory) {
    InputStream is = getClassLoader().getResourceAsStream(resource);
    if (is==null) {
      is = getClassLoader().getResourceAsStream(directory+"/"+resource);
    }
    return is;
  }

  public static Properties getProperties(String resource, String directory) {
    Properties properties = new Properties();
    try {
      properties.load(getStream(resource, directory));
    } catch (IOException e) {
      throw new RuntimeException("couldn't load properties file '"+resource+"'", e);
    }
    return properties;
  }

  public static ClassLoader getProcessClassLoader(ProcessDefinition processDefinition) {
    return new ProcessClassLoader(ClassLoaderUtil.class.getClassLoader(), processDefinition);
  }

}
