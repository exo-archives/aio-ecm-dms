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
package org.jbpm.file.def;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.bytes.ByteArray;
import org.jbpm.module.def.ModuleDefinition;
import org.jbpm.module.exe.ModuleInstance;

public class FileDefinition extends ModuleDefinition {

  private static final long serialVersionUID = 1L;

  static String rootDir = JbpmConfiguration.getString("jbpm.files.dir");

  private String dir = null;

  private Map processFiles = null;

  public FileDefinition() {
  }

  public ModuleInstance createInstance() {
    return null;
  }

  // storing files
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * add a file to this definition.
   */
  public void addFile(String name, byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    addFile(name, bais);
  }

  /**
   * add a file to this definition.
   */
  public void addFile(String name, InputStream is) {
    try {
      if (isStoredOnFileSystem()) {
        storeFileOnFileSystem(name, is);

      } else { // its stored in the database
        storeFileInDb(name, is);
      }
    } catch (Exception e) {
      throw new RuntimeException("file '" + name + "' could not be stored", e);
    }
  }

  private void storeFileOnFileSystem(String name, InputStream is) throws FileNotFoundException, IOException {
    String fileName = getFilePath(name);
    log.trace("storing file '" + name + "' on file system to '" + fileName + "'");
    FileOutputStream fos = new FileOutputStream(fileName);
    transfer(is, fos);
    fos.close();
  }

  private void storeFileInDb(String name, InputStream is) throws IOException {
    if (processFiles == null) {
      processFiles = new HashMap();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    log.trace("preparing file '" + name + "' for storage in the database");
    transfer(is, baos);
    processFiles.put(name, new ByteArray(name, baos.toByteArray()));
  }

  // retrieving files
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * retrieve a file of this definition as an inputstream.
   */
  public InputStream getInputStream(String name) {
    InputStream inputStream = null;
    try {
      if (isStoredOnFileSystem()) {
        inputStream = getInputStreamFromFileSystem(name);
      } else { // its stored in the database
        inputStream = getInputStreamFromDb(name);
      }
    } catch (Exception e) {
      throw new RuntimeException("couldn't get inputstream for file '" + name + "'", e);
    }
    return inputStream;
  }

  public Map getInputStreamMap() {
    HashMap result = new HashMap();
    if (processFiles != null) {
      Iterator iterator = processFiles.keySet().iterator();
      while (iterator.hasNext()) {
        String name = (String) iterator.next();
        result.put(name, getInputStream(name));
      }
    }
    return result;
  }

  public Map getBytesMap() {
    HashMap result = new HashMap();
    if (processFiles != null) {
      Iterator iterator = processFiles.keySet().iterator();
      while (iterator.hasNext()) {
        String name = (String) iterator.next();
        result.put(name, getBytes(name));
      }
    }
    return result;
  }

  private InputStream getInputStreamFromFileSystem(String name) throws FileNotFoundException {
    InputStream inputStream = null;
    String fileName = getFilePath(name);
    log.trace("loading file '" + name + "' from file system '" + fileName + "'");
    inputStream = new FileInputStream(fileName);
    return inputStream;
  }

  private InputStream getInputStreamFromDb(String name) {
    InputStream inputStream = null;
    log.trace("loading file '" + name + "' from database");
    ByteArray byteArray = getByteArray(name);
    inputStream = new ByteArrayInputStream(byteArray.getBytes());
    return inputStream;
  }

  /**
   * retrieve a file of this definition as a byte array.
   */
  public byte[] getBytes(String name) {
    byte[] bytes = null;
    try {
      if (isStoredOnFileSystem()) {
        bytes = getBytesFromFileSystem(name);
      } else { // its stored in the database
        bytes = getBytesFromDb(name);
      }
    } catch (Exception e) {
      throw new RuntimeException("couldn't get value for file '" + name + "'", e);
    }
    return bytes;
  }

  private byte[] getBytesFromFileSystem(String name) throws IOException {
    byte[] bytes = null;
    InputStream in = getInputStreamFromFileSystem(name);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    transfer(in, out);
    bytes = out.toByteArray();
    return bytes;
  }

  private byte[] getBytesFromDb(String name) {
    byte[] bytes;
    ByteArray byteArray = getByteArray(name);
    bytes = byteArray.getBytes();
    return bytes;
  }

  private ByteArray getByteArray(String name) {
    ByteArray byteArray = (ByteArray) (processFiles != null ? processFiles.get(name) : null);
    if (byteArray == null) {
      throw new RuntimeException("file '" + name + "' not found in db");
    }
    return byteArray;
  }

  private boolean isStoredOnFileSystem() {
    boolean isStoredOnFileSystem = (rootDir != null);
    // if files should be stored on the file system and no directory has been
    // created yet...
    if ((isStoredOnFileSystem) && (dir == null)) {
      // create a new directory
      dir = findNewDirName();
      new File(rootDir + "/" + dir).mkdirs();
    }
    return isStoredOnFileSystem;
  }

  private String findNewDirName() {
    String newDirName = "files-1";

    File parentFile = new File(rootDir);
    if (parentFile.exists()) {
      // get the current contents of the directory
      String[] children = parentFile.list();
      List fileNames = new ArrayList();
      if (children != null) {
        fileNames = new ArrayList(Arrays.asList(children));
      }

      // find an unused name for the directory to be created
      int seqNr = 1;
      while (fileNames.contains(newDirName)) {
        seqNr++;
        newDirName = "files-" + seqNr;
      }
    }

    return newDirName;
  }

  private String getFilePath(String name) {
    String filePath = rootDir + "/" + dir + "/" + name;
    new File(filePath).getParentFile().mkdirs();
    return filePath;
  }
  
  private static final int BUFFER_SIZE = 512;
  public static int transfer(InputStream in, OutputStream out) throws IOException {
    int total = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = in.read( buffer );
    while ( bytesRead != -1 ) {
      out.write( buffer, 0, bytesRead );
      total += bytesRead;
      bytesRead = in.read( buffer );
    }
    return total;
  }

  private static final Log log = LogFactory.getLog(FileDefinition.class);

}
