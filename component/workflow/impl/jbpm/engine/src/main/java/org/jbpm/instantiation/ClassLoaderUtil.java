/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
