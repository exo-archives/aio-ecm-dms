package org.jbpm.jpdl.par;

import org.jbpm.graph.def.*;

public interface ProcessArchiveParser {

  ProcessDefinition readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition);

//  void writeToArchive(ProcessDefinition processDefinition, ProcessArchive archive);

}
