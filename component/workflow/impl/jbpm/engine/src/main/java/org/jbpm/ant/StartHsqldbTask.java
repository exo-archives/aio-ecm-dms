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
