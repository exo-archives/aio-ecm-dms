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
package org.jbpm.jpdl.par;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.dom4j.*;
import org.dom4j.io.*;
import org.jbpm.graph.def.*;
import org.jbpm.instantiation.*;
import org.jbpm.jpdl.xml.Problem;

public class ProcessArchive {
  
  static final int BUFFERSIZE = 4096;
  static List processArchiveParsers = getProcessArchiveParsers();
  
  // fields ///////////////////////////////////////////////////////////////////

  String name = "";
  // maps entry-names (String) to byte-arrays (byte[])
  Map entries = new HashMap();
  List problems = new ArrayList(); 

  // constructors /////////////////////////////////////////////////////////////

  public ProcessArchive(ZipInputStream zipInputStream) throws IOException {
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    while(zipEntry!=null) {
      String entryName = zipEntry.getName();
      byte[] bytes = readBytes(zipInputStream);
      if (bytes!=null) {
        entries.put(entryName, bytes);
      }
      zipEntry = zipInputStream.getNextEntry();
    }
  }
  
  // parse the process definition from the contents ///////////////////////////

  public ProcessDefinition parseProcessDefinition() {
    ProcessDefinition processDefinition = ProcessDefinition.createNewProcessDefinition();
    Iterator iter = processArchiveParsers.iterator();
    while (iter.hasNext()) {
      ProcessArchiveParser processArchiveParser = (ProcessArchiveParser) iter.next();
      processDefinition = processArchiveParser.readFromArchive(this, processDefinition);
    }
    return processDefinition;
  }
  
  // methods for the process archive parsers //////////////////////////////////

  public String toString() {
    return "process-archive("+name+")";
  }
  
  public Map getEntries() {
    return entries;
  }
  
  public byte[] getEntry(String entryName) {
    return (byte[]) entries.get(entryName);
  }

  public InputStream getEntryInputStream(String entryName) {
    return new ByteArrayInputStream(getEntry(entryName));
  }

  public byte[] removeEntry(String entryName) {
    return (byte[]) entries.remove(entryName);
  }

  public InputStream removeEntryInputStream(String entryName) {
    return new ByteArrayInputStream(removeEntry(entryName));
  }
  public void addProblem(Problem problem) {
    problems.add(problem);
  }
  
  public void addError(String description) {
    addProblem(new Problem(Problem.LEVEL_ERROR, description));
  }

  public void addError(String description, Throwable exception) {
    addProblem(new Problem(Problem.LEVEL_ERROR, description, exception));
  }

  public void addWarning(String description) {
    addProblem(new Problem(Problem.LEVEL_WARNING, description));
  }

  public List getProblems() {
    return problems;
  }

  public void resetProblems() {
    problems = new ArrayList();
  }

  static byte[] readBytes(InputStream inputStream) throws IOException {
    byte[] bytes = null;
    if (inputStream==null) {
      throw new NullPointerException("inputStream is null in ProcessArchive.readBytes()");
    }
    byte[] buffer = new byte[BUFFERSIZE];
    int bytesRead = 0;
    while ( (bytesRead = inputStream.read(buffer)) != -1) {
      if (bytes!=null) {
        byte[] oldBytes = bytes;
        bytes = new byte[oldBytes.length+bytesRead];
        System.arraycopy(oldBytes, 0, bytes, 0, oldBytes.length);
        System.arraycopy(buffer, 0, bytes, oldBytes.length, bytesRead);
      } else {
        bytes = new byte[bytesRead];
        System.arraycopy(buffer, 0, bytes, 0, bytesRead);
      }
    }
    return bytes;
  }

  private static List getProcessArchiveParsers() {
    List processArchiveParsers = new ArrayList();
    try {
      InputStream parsersStream = ClassLoaderUtil.getStream("jbpm.parsers.xml", "org/jbpm/jpdl/par");
      Document document = new SAXReader().read(parsersStream);
      Iterator iter = document.getRootElement().elementIterator("parser");
      while (iter.hasNext()) {
        Element element = (Element) iter.next();
        String className = element.attributeValue("class");
        ProcessArchiveParser processArchiveParser= (ProcessArchiveParser) ClassLoaderUtil.loadClass(className).newInstance();
        processArchiveParsers.add(processArchiveParser);
      }
    } catch (Exception e) {
      throw new RuntimeException("couldn't parse process archive parsers (jbpm.parsers.xml)", e);
    }
    return processArchiveParsers;
  }

}
