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
package org.jbpm.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.logging.exe.LoggingInstance;
import org.jbpm.logging.log.ProcessLog;

public class LoggingSession {

  JbpmSession jbpmSession;
  Session session;
  
  public LoggingSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }
  
  /**
   * returns a map that maps {@link Token}s to {@link List}s.  The lists contain the ordered
   * logs for the given token.  The lists are retrieved with {@link #findLogsByToken(long)}. 
   */
  public Map findLogsByProcessInstance(long processInstanceId) {
    Map tokenLogs = new HashMap();
    try {
      ProcessInstance processInstance = (ProcessInstance) session.load(ProcessInstance.class, new Long(processInstanceId));
      collectTokenLogs(tokenLogs, processInstance.getRootToken());
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get logs for process instance '"+processInstanceId+"'", e);
    } 
    return tokenLogs;
  }

  private void collectTokenLogs(Map tokenLogs, Token token) {
    tokenLogs.put(token, findLogsByToken(token.getId()));
    Map children = token.getChildren();
    if ( (children!=null)
         && (!children.isEmpty()) 
       ) {
      Iterator iter = children.values().iterator();
      while (iter.hasNext()) {
        Token child = (Token) iter.next();
        collectTokenLogs(tokenLogs, child);
      }
    }
  }
  
  private static final String findLogsByToken = 
    "select pl " +
    "from org.jbpm.logging.log.ProcessLog as pl " +
    "where pl.token = :token " +
    "order by pl.index"; 
  /**
   * collects the logs for a given token, ordered by creation time.
   */
  public List findLogsByToken(long tokenId) {
    List result = null;
    try {
      Token token = (Token) session.load(Token.class, new Long(tokenId));
      Query query = session.createQuery(findLogsByToken);
      query.setEntity("token", token);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get logs for token '"+tokenId+"'", e);
    } 
    return result;
  }
  
  void saveLogs(ProcessInstance processInstance) {
    LoggingInstance loggingInstance = processInstance.getLoggingInstance();
    if (loggingInstance!=null) {
      Iterator iter = loggingInstance.getLogs().iterator();
      while (iter.hasNext()) {
        ProcessLog processLog = (ProcessLog) iter.next();
        saveProcessLog(processLog);
      }
    }
  }

  /**
   * saves the given process log to the database.
   */
  public void saveProcessLog(ProcessLog processLog) {
    try {
      session.save(processLog);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't save process log '"+processLog+"'", e);
    } 
  }
  
  /**
   * get the process log for a given id.
   */
  public ProcessLog loadProcessLog(long processLogId) {
    ProcessLog processLog = null;
    try {
      processLog = (ProcessLog) session.load(ProcessLog.class, new Long(processLogId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get process log '"+processLogId+"'", e);
    } 
    return processLog;
  }
  
  private static final Log log = LogFactory.getLog(LoggingSession.class);
}
