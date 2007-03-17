package org.jbpm.taskmgmt.exe;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PooledActor implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String actorId = null;
  protected Set taskInstances = null;
  protected SwimlaneInstance swimlaneInstance = null;

  public static Set createPool(String[] actorIds) {
    Set pooledActors = new HashSet();
    for (int i=0; i<actorIds.length; i++) {
      pooledActors.add(new PooledActor(actorIds[i]));
    }
    return pooledActors;
  }

  public PooledActor() {
  }

  public PooledActor(String actorId) {
    this.actorId = actorId;
  }
  
  public void addTaskInstance(TaskInstance taskInstance) {
    if (taskInstances==null) taskInstances = new HashSet();
    taskInstances.add(taskInstance);
  }
  public Set getTaskInstances() {
    return taskInstances;
  }
  public void removeTaskInstance(TaskInstance taskInstance) {
    if (taskInstances!=null) {
      taskInstances.remove(taskInstance);
    }
  }

  public String getActorId() {
    return actorId;
  }
  public void setActorId(String actorId) {
    this.actorId = actorId;
  }
  public SwimlaneInstance getSwimlaneInstance() {
    return swimlaneInstance;
  }
  public void setSwimlaneInstance(SwimlaneInstance swimlaneInstance) {
    this.swimlaneInstance = swimlaneInstance;
  }
  public long getId() {
    return id;
  }
}
