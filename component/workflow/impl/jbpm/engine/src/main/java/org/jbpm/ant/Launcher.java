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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Launcher extends Thread {

  Task task;
  String command;
  String endMsg;

  public Launcher(Task task, String command, String endMsg) {
    this.task = task;
    this.command = command;
    this.endMsg = endMsg;
  }

  public void run() {
    try {
      task.log("starting '" + command + "'...");
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while (line.indexOf(endMsg) == -1) {
        line = reader.readLine();
        task.log(line);
      }
      task.log("'" + command + "' started.");
    } catch (IOException e) {
      throw new BuildException("couldn't start '" + command + "'", e);
    }
  }

}
