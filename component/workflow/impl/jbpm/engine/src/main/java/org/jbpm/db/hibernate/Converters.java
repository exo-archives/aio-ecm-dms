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
package org.jbpm.db.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.jbpm.context.exe.Converter;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * provides access to the list of converters and ensures that the converter objects are unique.
 */
public class Converters {

  // maps class names to unique converter objects
  static HashMap convertersByClassNames = null;

  // maps converter database-id-strings to unique converter objects 
  static HashMap convertersByDatabaseId = null;
  
  // maps unique converter objects to their database-id-string
  static HashMap convertersIds = null;

  public static Converter getConverterByClassName(String className) {
    initConvertionMaps();
    Converter converter = (Converter) convertersByClassNames.get(className);
    if (converter==null) {
      throw new RuntimeException("converter '"+className+"' is not declared in jbpm.converter.properties");
    }
    return converter; 
  }

  public static Converter getConverterByDatabaseId(String converterDatabaseId) {
    initConvertionMaps();
    return (Converter) convertersByDatabaseId.get(converterDatabaseId); 
  }

  public static String getConverterId(Converter converter) {
    initConvertionMaps();
    return (String) convertersIds.get(converter); 
  }

  static void initConvertionMaps() {
    if (convertersByClassNames==null) {
      Properties converterProperties = ClassLoaderUtil.getProperties("jbpm.converter.properties", "org/jbpm/db/hibernate");
      
      convertersByClassNames = new HashMap();
      convertersByDatabaseId = new HashMap();
      convertersIds = new HashMap();

      Iterator iter = converterProperties.keySet().iterator();
      while (iter.hasNext()) {
        String converterDatabaseId = (String) iter.next();
        if (converterDatabaseId.length()!=1) throw new RuntimeException("converter-ids must be of length 1 (to be stored in a char)");
        if (convertersByDatabaseId.containsKey(converterDatabaseId)) throw new RuntimeException("duplicate converter id : '"+converterDatabaseId+"'");

        String converterClassName = converterProperties.getProperty(converterDatabaseId);
        try {
          Class converterClass = ClassLoaderUtil.loadClass(converterClassName);
          Converter converter = (Converter) converterClass.newInstance();
          convertersByClassNames.put(converterClassName, converter);
          convertersByDatabaseId.put(converterDatabaseId, converter);
          convertersIds.put(converter, converterDatabaseId);
        } catch (Exception e) {
          throw new RuntimeException("couldn't instantiate converter '"+converterClassName+"'");
        }
      }
    }
  }
}
