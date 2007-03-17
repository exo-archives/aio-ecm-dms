package org.jbpm.taskmgmt.exe;

import java.io.Serializable;

/**
 * common superclass for {@link org.jbpm.taskmgmt.exe.TaskInstance}s and 
 * {@link org.jbpm.taskmgmt.exe.SwimlaneInstance}s used by 
 * the {@link org.jbpm.taskmgmt.def.AssignmentHandler} interface.
 */
public interface Assignable extends Serializable {

  /**
   * sets the responsible for this assignable object.
   * Use this method to assign the task into a user's personal 
   * task list.
   */
  public void setActorId(String actorId);
  
  /**
   * sets the resource pool for this assignable as a set of {@link PooledActor}s.
   * Use this method to offer the task to a group of users.  Each user in the group
   * can then take the task by calling the {@link #setActorId(String)}.
   */
  public void setPooledActors(String[] pooledActors);
}
