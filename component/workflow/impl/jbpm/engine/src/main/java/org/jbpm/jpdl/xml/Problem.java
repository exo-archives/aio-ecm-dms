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
package org.jbpm.jpdl.xml;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class Problem implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  public static final int LEVEL_FATAL = 1;
  public static final int LEVEL_ERROR = 2;
  public static final int LEVEL_WARNING = 3;
  public static final int LEVEL_INFO = 4;

  private static String getTypeDescription(int level) {
    if (level==LEVEL_FATAL) return "FATAL";
    if (level==LEVEL_ERROR) return "ERROR";
    if (level==LEVEL_WARNING) return "WARNING";
    if (level==LEVEL_INFO) return "INFO";
    return null;
  }

  protected int level;
  protected String description;
  protected String resource;
  protected String folder;
  protected Integer line;
  protected Throwable exception;
  
  public Problem(int level, String description) {
    this.level = level;
    this.description = description;
  }

  public Problem(int level, String description, Throwable exception) {
    this.level = level;
    this.description = description;
    this.exception = exception;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("["+getTypeDescription(level)+"]");
    if (resource!=null) buffer.append(" "+resource);
    if (line!=null) buffer.append("("+line+")");
    if (folder!=null) buffer.append(" "+folder);
	if (description!=null) buffer.append(" "+description);
    return buffer.toString();
  }
  
  public static boolean containsProblemsOfLevel(Collection c, int level) {
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      Problem problem = (Problem) iter.next();
      if (problem.level <= level) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Throwable getException() {
    return exception;
  }
  public void setException(Throwable exception) {
    this.exception = exception;
  }
  public String getFolder() {
    return folder;
  }
  public void setFolder(String folder) {
    this.folder = folder;
  }
  public Integer getLine() {
    return line;
  }
  public void setLine(Integer line) {
    this.line = line;
  }
  public String getResource() {
    return resource;
  }
  public void setResource(String resource) {
    this.resource = resource;
  }
  public int getLevel() {
    return level;
  }
}
