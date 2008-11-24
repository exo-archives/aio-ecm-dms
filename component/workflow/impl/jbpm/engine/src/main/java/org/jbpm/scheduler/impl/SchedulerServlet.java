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
package org.jbpm.scheduler.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * the jBPM timer execution servlet.
 * <p>Config parameters : 
 * <ul>
 *   <li><b>interval</b>: maximum time in milliseconds between 2 calls to {@link org.jbpm.scheduler.spi.TimerExecutor#executeTimers()}. defaults to 5000.</li>
 *   <li><b>historyMaxSize</b>: maximum number of logs to be kept for display on the page. defaults to 50.</li>
 * </ul>
 * </p>
 * 
 * <p>Configuration example with:
 * <pre>
 * &lt;web-app&gt;
 *   ...
 *   &lt;servlet &gt;
 *     &lt;servlet-name>SchedulerServlet&lt;/servlet-name>
 *     &lt;servlet-class>org.jbpm.scheduler.impl.SchedulerServlet&lt;/servlet-class>
 *     &lt;init-param&gt;
 *       &lt;param-name&gt;interval&lt;/param-name&gt;
 *       &lt;param-value&gt;5000&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *       &lt;param-name&gt;historyMaxSize&lt;/param-name&gt;
 *       &lt;param-value&gt;50&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping &gt;
 *     &lt;servlet-name&gt;SchedulerServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;/jbpmscheduler&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 * </p>
 */
public class SchedulerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  
  Scheduler scheduler = null;

  public void init() throws ServletException {
    // create a new scheduler
    scheduler = new Scheduler();
    
    // initialize it with the servlet init parameters 
    int interval = Integer.parseInt(getInitParameter("interval", "5000"));
    scheduler.setInterval(interval);
    int historyMaxSize = Integer.parseInt(getInitParameter("historyMaxSize", "50"));
    scheduler.setHistoryMaxSize(historyMaxSize);
    
    // put the scheduler in the web app context
    getServletContext().setAttribute("scheduler", scheduler);
    
    // start the scheduler
    scheduler.start();
  }
  
  public void destroy() {
    scheduler.stop();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<body>");
    out.println("<h2>JBoss jBPM Scheduler Servlet</h2><hr />");
    out.println("</body>");
    out.println("</html>");
  }

  private String getInitParameter(String name, String defaultValue) {
    String value = getInitParameter(name);
    if (value!=null) {
      return value;
    }
    return defaultValue;
  }
}
