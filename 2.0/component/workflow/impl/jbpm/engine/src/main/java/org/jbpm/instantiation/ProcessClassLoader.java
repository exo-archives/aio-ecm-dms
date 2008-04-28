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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jbpm.file.def.FileDefinition;
import org.jbpm.graph.def.ProcessDefinition;

public class ProcessClassLoader extends ClassLoader {
  
  private ProcessDefinition processDefinition = null;

  public ProcessClassLoader( ClassLoader parent, ProcessDefinition processDefinition ) {
    super(parent);
    this.processDefinition = processDefinition;
  }

  public InputStream getResourceAsStream(String name) {
    InputStream inputStream = null;
    FileDefinition fileDefinition = processDefinition.getFileDefinition();
    if (fileDefinition!=null) {
      byte[] bytes = fileDefinition.getBytes(name);
      if (bytes!=null) {
        inputStream = new ByteArrayInputStream(bytes);
      }
    }
    return inputStream;
  }

  public Class findClass(String name) throws ClassNotFoundException {
    Class clazz = null;

    FileDefinition fileDefinition = processDefinition.getFileDefinition();
    if (fileDefinition!=null) {
      String fileName = "classes/" + name.replace( '.', '/' ) + ".class";
      byte[] classBytes;
      try {
        classBytes = fileDefinition.getBytes(fileName);
        clazz = defineClass(name, classBytes, 0, classBytes.length);
      } catch (RuntimeException e) {
        clazz = null;
      }
    }

    if (clazz==null) {
      throw new ClassNotFoundException("class '"+name+"' could not be found by the process classloader");
    }

    return clazz;
  }
}
