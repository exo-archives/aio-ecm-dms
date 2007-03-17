package org.jbpm.taskmgmt.exe;

import java.io.*;
import java.util.Set;

import org.jbpm.taskmgmt.def.*;

/**
 * is a process role for a one process instance.
 */
public class SwimlaneInstance implements Serializable, Assignable {

  private static final long serialVersionUID = 1L;

  long id = 0;
  protected String name = null;
  protected String actorId = null;
  protected Set pooledActors = null;
  protected Swimlane swimlane = null;
  protected TaskMgmtInstance taskMgmtInstance = null;
  
  public SwimlaneInstance() {
  }
  
  public SwimlaneInstance(Swimlane swimlane) {
    this.name = swimlane.getName();
    this.swimlane = swimlane;
  }

  public void setPooledActors(String[] actorIds) {
    this.pooledActors = PooledActor.createPool(actorIds);
  }

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Swimlane getSwimlane() {
    return swimlane;
  }
  public String getActorId() {
    return actorId;
  }
  public void setActorId(String actorId) {
    this.actorId = actorId;
  }
  public TaskMgmtInstance getTaskMgmtInstance() {
    return taskMgmtInstance;
  }
  public void setTaskMgmtInstance(TaskMgmtInstance taskMgmtInstance) {
    this.taskMgmtInstance = taskMgmtInstance;
  }
  public Set getPooledActors() {
    return pooledActors;
  }
  public void setPooledActors(Set pooledActors) {
    this.pooledActors = pooledActors;
  }
  
}
