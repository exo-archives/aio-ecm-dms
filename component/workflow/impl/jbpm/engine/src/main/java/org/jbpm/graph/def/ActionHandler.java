package org.jbpm.graph.def;

import java.io.*;

import org.jbpm.graph.exe.*;

public interface ActionHandler extends Serializable {
  
  void execute( ExecutionContext executionContext ) throws Exception;
}
