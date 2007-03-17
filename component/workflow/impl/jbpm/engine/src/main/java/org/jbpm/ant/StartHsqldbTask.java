package org.jbpm.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class StartHsqldbTask extends Task {
  
  String lib;
  String database;
  String port;
  
  public StartHsqldbTask() {
  }

  public void execute() throws BuildException {
    try {
      // build the command string
      String command = "java -cp "+ lib +
                       " org.hsqldb.Server " +
                       " -database " + database + 
                       " -port "+port;

      // launch the command and wait till the END_MESSAGE appears
      Thread launcher = new Launcher(this, command, "is online");
      launcher.start();
      launcher.join();
      
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void setDatabase(String database) {
    this.database = database;
  }
  public void setLib(String lib) {
    this.lib = lib;
  }
  public void setPort(String port) {
    this.port = port;
  }
}
