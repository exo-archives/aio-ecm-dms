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

import java.util.*;

import org.apache.commons.logging.*;
import org.hibernate.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

/**
 * are the graph related database operations.
 */
public class GraphSession {

  JbpmSession jbpmSession = null;
  Session session = null;
  
  public GraphSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }
  
  // process definitions //////////////////////////////////////////////////////
  
  /**
   * saves the process definitions.  this method does not assign a version 
   * number.  that is the responsibility of the {@link org.jbpm.jpdl.par.ProcessArchiveDeployer}.
   */
  public void saveProcessDefinition( ProcessDefinition processDefinition ) {
    try {
      session.save(processDefinition);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't save process definition '" + processDefinition + "'", e);
    } 
  }
  
  /**
   * loads a process definition from the database by the identifier.
   */
  public ProcessDefinition loadProcessDefinition(long processDefinitionId) {
    try {
      return (ProcessDefinition) session.load( ProcessDefinition.class, new Long(processDefinitionId) );
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't load process definition '" + processDefinitionId + "'", e);
    } 
  }
  
  private static final String findProcessDefinitionByNameAndVersionQuery = 
    "select pd " +
    "from org.jbpm.graph.def.ProcessDefinition as pd " +
    "where pd.name = :name " +
    "  and pd.version = :version"; 
  /**
   * queries the database for a process definition with the given name and version.
   */
  public ProcessDefinition findProcessDefinition(String name, int version) {
    ProcessDefinition processDefinition = null;
    try {
      Query query = session.createQuery(findProcessDefinitionByNameAndVersionQuery);
      query.setString("name", name);
      query.setInteger("version", version);
      Iterator result = query.iterate();
      if ( result.hasNext() ) {
        processDefinition = (ProcessDefinition) result.next();
      }
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get process definition with name '"+name+"' and version '"+version+"'", e);
    } 
    return processDefinition;
  }
  
  private static final String findLatestProcessDefinitionQuery = 
    "select pd " +
    "from org.jbpm.graph.def.ProcessDefinition as pd " +
    "where pd.name = :name " +
    "order by pd.version desc"; 
  /**
   * queries the database for the latest version of a process definition with the given name.
   */
  public ProcessDefinition findLatestProcessDefinition(String name) {
    ProcessDefinition processDefinition = null;
    try {
      Query query = session.createQuery(findLatestProcessDefinitionQuery);
      query.setString("name", name);
      Iterator result = query.iterate();
      if ( result.hasNext() ) {
        processDefinition = (ProcessDefinition) result.next();
      }
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't find process definition '" + name + "'", e);
    }
    return processDefinition;
  }

  /**
   * queries the database for the latest version of each process definition.
   * Process definitions are distinct by name.
   */
  public List findLatestProcessDefinitions() {
    List processDefinitions = new ArrayList();
    Map processDefinitionsByName = new HashMap();
    try {
      Query query = session.createQuery(findAllProcessDefinitionsQuery);
      Iterator iter = query.list().iterator();
      while (iter.hasNext()) {
        ProcessDefinition processDefinition = (ProcessDefinition) iter.next();
        String processDefinitionName = processDefinition.getName();
        ProcessDefinition previous = (ProcessDefinition) processDefinitionsByName.get(processDefinitionName);
        if ( (previous==null)
             || (previous.getVersion()<processDefinition.getVersion()) 
           ){
          processDefinitionsByName.put(processDefinitionName, processDefinition);
        }
      }
      processDefinitions = new ArrayList(processDefinitionsByName.values());
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't find latest versions of process definitions", e);
    }
    return processDefinitions;
  }

  private static final String findAllProcessDefinitionsQuery = 
    "select pd " +
    "from org.jbpm.graph.def.ProcessDefinition as pd " +
    "order by pd.name, pd.version desc"; 
  /**
   * queries the database for all process definitions, ordered by name (ascending), then by version (descending).
   */
  public List findAllProcessDefinitions() {
    try {
      Query query = session.createQuery(findAllProcessDefinitionsQuery);
      return query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't find all process definitions", e);
    } 
  }

  private static final String findAllProcessDefinitionVersionsQuery = 
    "select pd " +
    "from org.jbpm.graph.def.ProcessDefinition as pd " +
    "where pd.name = :name " +
    "order by pd.version desc"; 
  /**
   * queries the database for all versions of process definitions with the given name, ordered by version (descending).
   */
  public List findAllProcessDefinitionVersions(String name) {
    try {
      Query query = session.createQuery(findAllProcessDefinitionVersionsQuery);
      query.setString("name", name);
      return query.list();
    } catch (HibernateException e) {
      log.error(e);
      throw new RuntimeException("couldn't find all versions of process definition '"+name+"'", e);
    } 
  }

  public void deleteProcessDefinition(long processDefinitionId) {
    deleteProcessDefinition(loadProcessDefinition(processDefinitionId)); 
  }

  public void deleteProcessDefinition(ProcessDefinition processDefinition) {
    if (processDefinition==null) throw new NullPointerException("processDefinition is null in JbpmSession.deleteProcessDefinition()");
    try {
      // delete all the process instances of this definition
      List processInstances = findProcessInstances(processDefinition.getId());
      if (processInstances!=null) {
        Iterator iter = processInstances.iterator();
        while (iter.hasNext()) {
          deleteProcessInstance((ProcessInstance) iter.next());
        }
      }
      
      // then delete the process definition
      session.delete(processDefinition);

    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't delete process definition '" + processDefinition.getId() + "'", e);
    } 
  }

  // process instances ////////////////////////////////////////////////////////

  /**
   * save a new process instance.
   */
  public void saveProcessInstance(ProcessInstance processInstance) {
    try {
      // saveProcessInstanceLogs(processInstance);
      session.saveOrUpdate(processInstance);
      jbpmSession.getLoggingSession().saveLogs(processInstance);
      jbpmSession.getSchedulerSession().saveTimers(processInstance);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't save process instance '" + processInstance + "'", e);
    } 
  }

  /**
   * loads a process instance from the database by the identifier.
   */
  public ProcessInstance loadProcessInstance(long processInstanceId) {
    try {
      ProcessInstance processInstance = (ProcessInstance) session.load( ProcessInstance.class, new Long(processInstanceId) );
      return processInstance;
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't load process instance '" + processInstanceId + "'", e);
    } 
  }

  /**
   * loads a token from the database by the identifier.
   */
  public Token loadToken(long tokenId) {
    try {
      Token token = (Token) session.load(Token.class, new Long(tokenId));
      return token;
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't load token '" + tokenId + "'", e);
    } 
  }

  /**
   * locks a process instance in the database.
   */
  public void lockProcessInstance(long processInstanceId) {
    lockProcessInstance(loadProcessInstance(processInstanceId)); 
  }

  /**
   * locks a process instance in the database.
   */
  public void lockProcessInstance(ProcessInstance processInstance) {
    try {
      session.lock( processInstance, LockMode.UPGRADE );
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't lock process instance '" + processInstance.getId() + "'", e);
    } 
  }

  private static final String findAllProcessInstancesForADefinitionQuery = 
    "select pi " +
    "from org.jbpm.graph.exe.ProcessInstance as pi " +
    "where pi.processDefinition.id = :processDefinitionId " +
    "order by pi.start desc"; 
  /**
   * fetches all processInstances for the given process definition from the database.
   * The returned list of process instances is sorted start date, youngest first.
   */
  public List findProcessInstances(long processDefinitionId) {
    List processInstances = null;
    try {
      Query query = session.createQuery(findAllProcessInstancesForADefinitionQuery);
      query.setLong("processDefinitionId", processDefinitionId);
      processInstances = query.list();

    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't load process instances for process definition '" + processDefinitionId + "'", e);
    } 
    return processInstances;
  }

  public void deleteProcessInstance(long processInstanceId) {
    deleteProcessInstance(loadProcessInstance(processInstanceId)); 
  }

  private static final String findTokensForProcessInstance = 
    "select token " +
    "from org.jbpm.graph.exe.Token token " +
    "where token.processInstance = :processInstance ";
  private static final String selectLogsForTokens = 
    "select pl " +
    "from org.jbpm.logging.log.ProcessLog as pl " +
    "where pl.token in (:tokens)";
  public void deleteProcessInstance(ProcessInstance processInstance) {
    if (processInstance==null) throw new NullPointerException("processInstance is null in JbpmSession.deleteProcessInstance()");
    try {
      // find the tokens
      Query query = session.createQuery(findTokensForProcessInstance);
      query.setEntity("processInstance", processInstance);
      List tokens = query.list();
      
      // delete the logs for all the process instance's tokens
      query = session.createQuery(selectLogsForTokens);
      query.setParameterList("tokens", tokens);
      List logs = query.list();
      Iterator iter = logs.iterator();
      while (iter.hasNext()) {
        session.delete(iter.next());
      }

      // then delete the process instance
      session.delete(processInstance);
      
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't delete process instance '" + processInstance.getId() + "'", e);
    } 
  }

  private static final Log log = LogFactory.getLog(GraphSession.class);
}
