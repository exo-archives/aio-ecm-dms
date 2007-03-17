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
