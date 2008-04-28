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
