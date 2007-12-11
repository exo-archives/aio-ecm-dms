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

import java.util.*;
import org.jbpm.file.def.*;
import org.jbpm.graph.def.*;

public class FileArchiveParser implements ProcessArchiveParser {

  public ProcessDefinition readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
    FileDefinition fileDefinition = (FileDefinition) processDefinition.getDefinition(FileDefinition.class);
    Map entries = processArchive.getEntries();
    Iterator iter = entries.keySet().iterator();
    while (iter.hasNext()) {
      String entryName = (String) iter.next();
      if (! "processdefinition.xml".equals(entryName)) {
        if (fileDefinition == null) {
          fileDefinition = new FileDefinition();
          processDefinition.addDefinition(fileDefinition);
        }
        byte[] entry = (byte[]) entries.get(entryName);
        if(entry != null) {
          fileDefinition.addFile(entryName, entry);
        }

      }
    }
    return processDefinition;
  }

}
