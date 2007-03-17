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
