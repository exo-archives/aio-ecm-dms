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
