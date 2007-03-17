package org.jbpm.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class StartJBossTask extends Task {
  
  private static final String END_MESSAGE = " Started in ";
  
  private String configuration = null;
  
  public void execute() throws BuildException {
    try {
      // get some environment variables
      String fileSeparator = System.getProperty( "file.separator" );
      String jbossHome = getProject().getProperty( "jboss.home" );
      String os = getProject().getProperty( "os.name" ).toLowerCase();
      
      // build the command string
      String command = null; 
      if ( os.indexOf( "windows" ) != -1 ) {
        command = jbossHome + fileSeparator + "bin" + fileSeparator + "run.bat " + getConfigParameter();          
      } else if ( os.indexOf( "linux" ) != -1 ) {
        command = jbossHome + fileSeparator + "bin" + fileSeparator + "run.sh " + getConfigParameter(); 
      } else {
        throw new BuildException( "os '" + os + "' not supported in the startjboss task." );
      }

      // launch the command and wait till the END_MESSAGE appears
      Thread launcher = new Launcher(this, command, END_MESSAGE);
      launcher.start();
      launcher.join();
      
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  private String getConfigParameter() {
    if (configuration==null) return "";
    return "-c "+configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }
}
