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
package org.jbpm.context.exe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jbpm.db.hibernate.Converters;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * specifies for one java-type how jbpm is able to persist objects of that type in the database. 
 */
public class JbpmType {
  
  private static List jbpmTypes = null;
  
  public String variableClassName = null;
  public Class variableClass = null;
  public Converter converter = null;
  public Class variableInstanceClass = null;

  public JbpmType(String line) {
    // parse the line
    List stringTokens = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(line, " ");
    while (tokenizer.hasMoreTokens()) {
      stringTokens.add(tokenizer.nextToken().trim());
    }
    
    if ( (stringTokens.size()<2)
         || (stringTokens.size()>3) ) {
      throw new RuntimeException("invalid format in jbpm.varmapping.properties of line '"+line+"'");
    }
    this.variableClassName = (String) stringTokens.get(0);
    
    // if this class represents an array or a collection of classes
    if ( (! this.variableClassName.startsWith("["))
         && (! this.variableClassName.startsWith("{")) ) {
      this.variableClass = ClassLoaderUtil.loadClass(variableClassName);
    }
    
    if (stringTokens.size()==2) {
      this.variableInstanceClass = getVariableInstanceClass((String) stringTokens.get(1));
    } else {
      this.converter = getConverter((String) stringTokens.get(1));
      this.variableInstanceClass = getVariableInstanceClass((String) stringTokens.get(2));
    }
  }

  private Converter getConverter(String converterClassName) {
    return Converters.getConverterByClassName(converterClassName);
  }

  private Class getVariableInstanceClass(String variableInstanceClassName) {
    return ClassLoaderUtil.loadClass(variableInstanceClassName);
  }

  public static List getJbpmTypes() {
    if (jbpmTypes==null) {
      jbpmTypes = new ArrayList();
      InputStream is = ClassLoaderUtil.getStream("jbpm.varmapping.properties", "org/jbpm/context/exe");
      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = bufferedReader.readLine();
        while(line!=null) {
          line = line.trim();
          if ( !line.startsWith("#")
               && (line.length()!=0)) {
            jbpmTypes.add(new JbpmType(line));
          }
          line = bufferedReader.readLine();
        }
      } catch (IOException e) {
        throw new RuntimeException("couldn't parse the jbpm.varmapping.properties", e);
      } finally {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return jbpmTypes;
  }
}
