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
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

public class TaskMgmtSession {

  JbpmSession jbpmSession = null;
  Session session = null;
  
  public TaskMgmtSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }
  
  private static final String findTaskInstancesByActorId = 
    "select ti " +
    "from org.jbpm.taskmgmt.exe.TaskInstance as ti " +
    "where ti.actorId = :actorId " +
    "  and ti.end is null " +
    "  and ti.isCancelled = false"; 
  /**
   * get the tasllist for a given actor.
   */
  public List findTaskInstances(String actorId) {
    List result = new ArrayList();
    try {
      Query query = session.createQuery(findTaskInstancesByActorId);
      query.setString("actorId", actorId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      //return result;
      //throw new RuntimeException("couldn't get task instances list for actor '"+actorId+"'", e);
    } 
    return result;
  }
  
  /**
   * get all the task instances for all the given actorIds.
   * @throws NullPointerException if actorIds is null.
   */
  public List findTaskInstances(List actorIds) {
    return findTaskInstances((String[])actorIds.toArray(new String[actorIds.size()]));
  }

  private static final String findTaskInstancesByActorIds = 
    "select ti " +
    "from org.jbpm.taskmgmt.exe.TaskInstance as ti " +
    "where ti.actorId in ( :actorIds )" + 
    "  and ti.end is null " +
    "  and ti.isCancelled = false"; 
  /**
   * get all the task instances for all the given actorIds.
   */
  public List findTaskInstances(String[] actorIds) {
    List result = null;
    try {
      Query query = session.createQuery(findTaskInstancesByActorIds);
      query.setParameterList("actorIds", actorIds);
      result = query.list();
    } catch (Exception e) {
      e.printStackTrace() ;
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get task instances list for actors '"+actorIds+"'", e);
    } 
    return result;
  }

  private static final String findPooledTaskInstancesByActorId = 
    "select distinct ti " +
    "from org.jbpm.taskmgmt.exe.PooledActor pooledActor " +
    "     join pooledActor.taskInstances ti " +
    "where pooledActor.actorId = :swimlaneActorId " +
    "  and ti.actorId is null " +
    "  and ti.end is null " +
    "  and ti.isCancelled = false";
  /**
   * get the taskinstances for which the given actor is in the pool.
   */
  public List findPooledTaskInstances(String actorId) {
    List result = null;
    try {
      Query query = session.createQuery(findPooledTaskInstancesByActorId);
      query.setString("swimlaneActorId", actorId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get pooled task instances list for actor '"+actorId+"'", e);
    } 
    return result;
  }
  
  private static final String findPooledTaskInstancesByActorIds = 
    "select distinct ti " +
    "from org.jbpm.taskmgmt.exe.PooledActor pooledActor " +
    "     join pooledActor.taskInstances ti " +
    "where pooledActor.actorId in ( :actorIds ) " +
    "  and ti.actorId is null " +
    "  and ti.end is null " +
    "  and ti.isCancelled = false";
  /**
   * get the taskinstances for which the given actor is in the pool.
   */
  public List findPooledTaskInstances(List actorIds) {
    List result = null;
    try {
      Query query = session.createQuery(findPooledTaskInstancesByActorIds);
      query.setParameterList("actorIds", actorIds);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get pooled task instances list for actors '"+actorIds+"'", e);
    } 
    return result;
  }

  private static final String findTaskInstancesByTokenId = 
    "select ti " +
    "from org.jbpm.taskmgmt.exe.TaskInstance ti " +
    "where ti.token.id = :tokenId " +
    "  and ti.end is null " +
    "  and ti.isCancelled = false";
  /**
   * get active taskinstances for a given token.
   */
  public List findTaskInstancesByToken(long tokenId) {
    List result = null;
    try {
      Query query = session.createQuery(findTaskInstancesByTokenId);
      query.setLong("tokenId", tokenId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get task instances by token '"+tokenId+"'", e);
    } 
    return result;
  }

  /**
   * get the task instance for a given task instance-id.
   */
  public TaskInstance loadTaskInstance(long taskInstanceId) {
    TaskInstance taskInstance = null;
    try {
      taskInstance = (TaskInstance) session.load(TaskMgmtInstance.getTaskInstanceClass(), new Long(taskInstanceId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new RuntimeException("couldn't get task instance '"+taskInstanceId+"'", e);
    } 
    return taskInstance;
  }
  

  private static final Log log = LogFactory.getLog(TaskMgmtSession.class);
}
