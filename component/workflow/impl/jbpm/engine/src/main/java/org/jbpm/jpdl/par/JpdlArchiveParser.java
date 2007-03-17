package org.jbpm.jpdl.par;

import java.io.*;

import org.jbpm.graph.def.*;
import org.jbpm.jpdl.*;
import org.jbpm.jpdl.xml.*;

public class JpdlArchiveParser implements ProcessArchiveParser {

  public ProcessDefinition readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
    try {
      // getting the value
      byte[] processBytes = processArchive.removeEntry("processdefinition.xml");
      
      if (processBytes==null) {
        throw new JpdlException("no processdefinition.xml inside process archive");
      }
      
      // creating the JpdlXmlReader
      InputStream processInputStream  = new ByteArrayInputStream(processBytes);
      Reader processReader = new InputStreamReader(processInputStream);
      JpdlXmlReader jpdlReader = new JpdlXmlReader(processReader);
      
      try {
        // pump the problems from the jpdlReader over to the processArchive
        processDefinition = jpdlReader.readProcessDefinition();
      } catch (JpdlException e) {
        e.printStackTrace();
        // pump the problems from the jpdlReader over to the processArchive
        processArchive.getProblems().addAll(e.getProblems());
      }

      // close all the streams
      jpdlReader.close();
      processReader.close();
      processInputStream.close();
      
    } catch (IOException e) {
      processArchive.addWarning("io problem while reading processdefinition.xml: "+e.getMessage() );
    }
    
    return processDefinition;
  }
}
