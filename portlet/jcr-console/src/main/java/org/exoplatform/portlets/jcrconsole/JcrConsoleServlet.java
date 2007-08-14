/*
 * Copyright 2001-2006 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

/*
package org.exoplatform.portlets.jcrconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.frameworks.jcr.cli.CliAppContext;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;

public class JcrConsoleServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    CliAppContext context = (CliAppContext) request.getSession().getAttribute("context");
    ArrayList<String> params = new ArrayList<String>();
    String PARAMETERS_KEY = "parameterss";
    response.setContentType("text/html");
    PrintWriter printWriter = response.getWriter();
    try {
      String containerName = request.getParameter("containerName").trim();
      ExoContainer container = ExoContainerContext.getContainerByName(containerName);

      String commandLine = request.getParameter("myaction").trim();
      String commandFromCommandLine = commandLine.substring(0,
          (commandLine.indexOf(" ") < 0) ? commandLine.length() : commandLine.indexOf(" "));

      commandLine = commandLine.substring(commandLine.indexOf(commandFromCommandLine)
          + commandFromCommandLine.length());
      commandLine = commandLine.trim();
      CommandService cservice = (CommandService) container
          .getComponentInstanceOfType(CommandService.class);
      Catalog catalog = cservice.getCatalog("CLI");

      parseQuery(commandLine, params);

      if (context == null) {
        RepositoryService repService = (RepositoryService) container
            .getComponentInstanceOfType(RepositoryService.class);
        String workspace = repService.getRepository().getConfiguration().getDefaultWorkspaceName();
        context = new CliAppContext(repService.getRepository(), PARAMETERS_KEY);
        context.setCurrentWorkspace(workspace);
        context.setCurrentItem(context.getSession().getRootNode());
      }
      Command commandToExecute = catalog.getCommand(commandFromCommandLine);
      context.put(PARAMETERS_KEY, params);
      commandToExecute.execute(context);
      printWriter.print(context.getOutput());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("[ERROR] [jcr-concole] Can't execute command - " + e.getMessage());
      printWriter.print("Invalid command\n");
    } finally {
      request.getSession().setAttribute("context", context);
    }

  }

  private void parseQuery(String query, ArrayList params) {
    try {
      params.clear();
      if (query.indexOf("\"") == -1) {
        while (!query.equals("")) {
          String item = query.substring(0, (query.indexOf(" ") < 0) ? query.length() : query
              .indexOf(" "));
          params.add(item);
          query = query.substring(query.indexOf(item) + item.length());
          query = query.trim();
        }
      } else {
        while (!query.equals("")) {
          String item = "";
          if (query.startsWith("\"")) {
            item = query.substring(query.indexOf("\"") + 1, (query.indexOf("\"", 1) < 0) ? query
                .length() : query.indexOf("\"", 1));
          } else {
            item = query.substring(0, (query.indexOf(" ") < 0) ? query.length() : query
                .indexOf(" "));
          }
          item = item.trim();
          if (item != null && !(item.equals(""))) {
            params.add(item);
          }
          int index = query.indexOf(item) + item.length() + 1;
          if (query.length() > index) {
            query = query.substring(query.indexOf(item) + item.length() + 1);
            query = query.trim();
          } else {
            query = "";
          }
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

}
*/