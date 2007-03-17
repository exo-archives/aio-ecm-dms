package org.jbpm.taskmgmt.exe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.security.Authentication;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.log.TaskAssignLog;
import org.jbpm.taskmgmt.log.TaskEndLog;

/**
 * is one task instance that can be assigned to an actor (read: put in 
 * someones task list) and that can trigger the coninuation of execution 
 * of the token upon completion.
 */
public class TaskInstance implements Serializable, Assignable {

  private static final long serialVersionUID = 1L;

  private long id = 0;
  protected String name = null;
  protected String description = null;
  protected String actorId = null;
  protected Date create = null;
  protected Date start = null;
  protected Date end = null;
  protected Date dueDate = null;
  protected int priority = Task.PRIORITY_NORMAL;
  protected boolean isCancelled = false;
  protected boolean isSignalling = true;
  protected boolean isBlocking = false;
  protected Task task = null;
  protected Token token = null;
  protected SwimlaneInstance swimlaneInstance = null;
  protected TaskMgmtInstance taskMgmtInstance = null;
  protected Set pooledActors = null;
  protected List comments = null;
  
  protected String previousActorId = null; // not persisted.  just extra information for listeners of the assign-event  
  
  public TaskInstance() {
  }

  public TaskInstance(String taskName) {
    this.name = taskName;
  }

  public TaskInstance(String taskName, String actorId) {
    this.name = taskName;
    this.actorId = actorId;
  }

  public void setTask(Task task) {
    this.name = task.getName();
    this.description = task.getDescription();
    this.task = task;
    this.isBlocking = task.isBlocking();
    this.priority = task.getPriority();
    if (task.getTaskNode()!=null) {
      int signal = task.getTaskNode().getSignal();
      this.isSignalling = ( (signal==TaskNode.SIGNAL_FIRST ) 
                            || (signal==TaskNode.SIGNAL_LAST ) 
                            || (signal==TaskNode.SIGNAL_FIRST_WAIT ) 
                            || (signal==TaskNode.SIGNAL_LAST_WAIT ) 
                          );
    }
    if (task.getDueDate()!=null) {
      BusinessCalendar businessCalendar = new BusinessCalendar();
      this.dueDate = businessCalendar.add(new Date(), new Duration(task.getDueDate()));
    }
  }
  
  public void create() {
    create(null);
  }

  public void create(ExecutionContext executionContext) {
    if (create!=null) {
      throw new IllegalStateException("task instance '"+id+"' was already created");
    }
    create = new Date();
    
    // if this task instance is associated with a task...
    if ( (task!=null)
         && (executionContext!=null)
       ) {
      // the TASK_CREATE event is fired
      executionContext.setTaskInstance(this);
      executionContext.setTask(task);
      task.fireEvent(Event.EVENTTYPE_TASK_CREATE, executionContext);
    }
  }

  public void assign(ExecutionContext executionContext) {
    TaskMgmtInstance taskMgmtInstance = executionContext.getTaskMgmtInstance();
    
    Swimlane swimlane = task.getSwimlane();
    // if this task is in a swimlane
    if (swimlane!=null) {
      
      // if this is a task assignment for a start-state
      if (isStartTaskInstance()) {
        // initialize the swimlane
        swimlaneInstance = new SwimlaneInstance(swimlane);
        taskMgmtInstance.addSwimlaneInstance(swimlaneInstance);
        // with the current authenticated actor
        swimlaneInstance.setActorId(Authentication.getAuthenticatedActorId());
        
      } else {
        
        // lazy initialize the swimlane...
        // get the swimlane instance (if there is any) 
        swimlaneInstance = taskMgmtInstance.getInitializedSwimlaneInstance(executionContext, swimlane);
        
        // copy the swimlaneInstance assignment into the taskInstance assignment
        copySwimlaneInstanceAssignment(swimlaneInstance);
      }

    } else { // this task is not in a swimlane
      taskMgmtInstance.invokeAssignmentHandler(task.getAssignmentDelegation(), this, executionContext);
    }
    
    updatePooledActorsReferences(swimlaneInstance);
  }


  public boolean isStartTaskInstance() {
    boolean isStartTaskInstance = false;
    if ( (taskMgmtInstance!=null)
         && (taskMgmtInstance.getTaskMgmtDefinition()!=null)
       ) {
         isStartTaskInstance = (task==taskMgmtInstance.getTaskMgmtDefinition().getStartTask());
    }
    return isStartTaskInstance;
  }

  private void updatePooledActorsReferences(SwimlaneInstance swimlaneInstance) {
    if (pooledActors!=null) {
      Iterator iter = pooledActors.iterator();
      while (iter.hasNext()) {
        PooledActor pooledActor = (PooledActor) iter.next();
        pooledActor.setSwimlaneInstance(swimlaneInstance);
        pooledActor.addTaskInstance(this);
      }
    }
  }

  /**
   * copies the assignment (that includes both the swimlaneActorId and the set of pooledActors) of 
   * the given swimlane into this taskInstance. 
   */
  public void copySwimlaneInstanceAssignment(SwimlaneInstance swimlaneInstance) {
    setSwimlaneInstance(swimlaneInstance);
    setActorId(swimlaneInstance.actorId);
    setPooledActors(swimlaneInstance.pooledActors!=null ? new HashSet(swimlaneInstance.pooledActors) : null);
  }

  /**
   * gets the pool of actors for this task instance.  If this task has a simlaneInstance 
   * and no pooled actors, the pooled actors of the swimlane instance are returned.
   */
  public Set getPooledActors() {
    if ( (swimlaneInstance!=null)
         && ( (pooledActors==null)
              || (pooledActors.isEmpty())
            )
       ){
      return swimlaneInstance.pooledActors; 
    }
    return pooledActors;
  }

  /**
   * (re)assign this task to the given actor.  If this task is related 
   * to a swimlane instance, that swimlane instance will be updated as well.
   */
  public void setActorId(String actorId) {
    setActorId(actorId, true);
  }

  /**
   * (re)assign this task to the given actor.
   * @param actorId is reference to the person that is assigned to this task.
   * @param overwriteSwimlane specifies if the related swimlane 
   * should be overwritten with the given swimlaneActorId.
   */
  public void setActorId(String actorId, boolean overwriteSwimlane){
    if ( (task!=null)
         && (token!=null) 
       ) {
      ExecutionContext executionContext = new ExecutionContext(token);
      executionContext.setTask(task);
      executionContext.setTaskInstance(this);
      task.fireEvent(Event.EVENTTYPE_TASK_ASSIGN, executionContext);
    }
    
    this.previousActorId = this.actorId;
    this.actorId = actorId;
    if ( (swimlaneInstance!=null)
         && (overwriteSwimlane) ) {
      swimlaneInstance.setActorId(actorId);
    }
    
    if (token!=null) {
      // log this assignment
      token.addLog(new TaskAssignLog(this, previousActorId, actorId));
    }
  }

  public void setPooledActors(String[] actorIds) {
    this.pooledActors = PooledActor.createPool(actorIds);
  }

  /**
   * can optionally be used to indicate that the actor is starting to 
   * work on this task instance. 
   */
  public void start(){
    if (start!=null) {
      throw new IllegalStateException("task instance '"+id+"' is already started");
    }
    
    start = new Date();
    if ( (task!=null)
         && (token!=null)
       ) {
      ExecutionContext executionContext = new ExecutionContext(token);
      executionContext.setTaskInstance(this);
      task.fireEvent(Event.EVENTTYPE_TASK_START, executionContext);
    }
  }

  /**
   * convenience method that combines a {@link #setActorId(String)} and
   * a {@link #start()}.
   */
  public void start(String actorId){
    setActorId(actorId);
    start();
  }
  
  /**
   * cancels this task.
   */
  public void cancel() {
    isCancelled = true;
    end();
  }

  /**
   * marks this task as done.  If this task is related to a task node 
   * this might trigger a signal on the token.
   * @see #end(Transition)
   */
  public void end() {
    end((Transition)null);
  }

  /**
   * marks this task as done and specifies the name of a transition  
   * leaving the task-node for the case that the completion of this 
   * task instances triggers a signal on the token.
   * If this task leads to a signal on the token, the given transition 
   * name will be used in the signal.
   * If this task completion does not trigger execution to move on, 
   * the transitionName is ignored.
   */
  public void end(String transitionName) {
    Transition leavingTransition = null;
    
    if (task!=null) {
      Node node = task.getTaskNode();
      if (node==null) {
        node = (Node) task.getParent();
      }

      if (node!=null) {
        leavingTransition = node.getLeavingTransition(transitionName);
      }
    }
    if (leavingTransition==null) {
      throw new NullPointerException("task node does not have leaving transition '"+transitionName+"'");
    }
    end(leavingTransition);
  }
  
  /**
   * marks this task as done and specifies a transition  
   * leaving the task-node for the case that the completion of this 
   * task instances triggers a signal on the token.
   * If this task leads to a signal on the token, the given transition 
   * name will be used in the signal.
   * If this task completion does not trigger execution to move on, 
   * the transition is ignored.
   */
  public void end(Transition transition) {
    if (this.end!=null){
      throw new IllegalStateException("task instance '"+id+"'is already started");
    }
    
    // mark the end of this task instance
    this.end = new Date();

    // fire the task instance end event
    if ( (task!=null)
         && (token!=null)
       ) {
      ExecutionContext executionContext = new ExecutionContext(token);
      task.fireEvent(Event.EVENTTYPE_TASK_END, executionContext);
    }
    
    // log this assignment
    if (token!=null) {
      token.addLog(new TaskEndLog(this));
    }
    
    // verify if the end of this task triggers continuation of execution
    if (isSignalling) {
      this.isSignalling = false;
      
      
      
      if ( this.isStartTaskInstance() // ending start tasks always leads to a signal
           || ( (task!=null)
                && (token!=null)
                && (task.getTaskNode()!=null)
                && (task.getTaskNode().completionTriggersSignal(this))
              )
         ) {
        
        if (transition==null) {
          log.debug("completion of task '"+task.getName()+"' results in taking the default transition");
          token.signal();
        } else {
          log.debug("completion of task '"+task.getName()+"' results in taking transition '"+transition+"'");
          token.signal(transition);
        }
      }
    }
  }

  public boolean hasEnded() {
    return (end!=null);
  }
  
  // comments /////////////////////////////////////////////////////////////////

  public void addComment(String message) {
    Comment comment = new Comment(message);
    addComment(comment);
    if (token!=null) {
      token.addComment(comment);
    }
  }

  public void addComment(Comment comment) {
    if (comments==null) comments = new ArrayList();
    comments.add(comment);
    comment.setTaskInstance(this);
    comment.setToken(token);
  }
  
  public List getComments() {
    return comments;
  }
 
  // task form ////////////////////////////////////////////////////////////////
  
  public List getTaskFormParameters() {
    List taskFormParameters = null;
    if ( (task!=null)
         && (task.getTaskController()!=null) 
       ) {
      taskFormParameters = task.getTaskController().getTaskFormParameters(this);
    }
    return taskFormParameters;
  }
  
  public void submitParameters(Map parameters) {
    if ( (task!=null)
         && (task.getTaskController()!=null) 
       ) {
      task.getTaskController().submitParameters(parameters, this);
    }
  }
  
  public boolean isLast() {
    return ( (token!=null)
             && (taskMgmtInstance!=null) 
             && (! taskMgmtInstance.hasUnfinishedTasks(token))
           );
  }
  
  /**
   * is the list of transitions that can be used in the end method
   * and it is null in case this is not the last task instance.
   */
  public List getAvailableTransitions() {
    List transitions = null;
    if ( (! isLast())
         && (token!=null)
       ) {
      transitions = new ArrayList(token.getNode().getLeavingTransitions());
    }
    return transitions;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public String getActorId() {
    return actorId;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public Date getEnd() {
    return end;
  }
  public void setEnd(Date end) {
    this.end = end;
  }
  public void setCreate(Date create) {
    this.create = create;
  }
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public Date getStart() {
    return start;
  }
  public TaskMgmtInstance getTaskMgmtInstance() {
    return taskMgmtInstance;
  }
  public void setTaskMgmtInstance(TaskMgmtInstance taskMgmtInstance) {
    this.taskMgmtInstance = taskMgmtInstance;
  }
  public Token getToken() {
    return token;
  }
  public void setToken(Token token) {
    this.token = token;
  }
  public void setSignalling(boolean isSignalling) {
    this.isSignalling = isSignalling;
  }
  public boolean isSignalling() {
    return isSignalling;
  }
  public boolean isCancelled() {
    return isCancelled;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean isBlocking() {
    return isBlocking;
  }
  public void setBlocking(boolean isBlocking) {
    this.isBlocking = isBlocking;
  }
  public Date getCreate() {
    return create;
  }
  public Task getTask() {
    return task;
  }
  public void setPooledActors(Set pooledActors) {
    this.pooledActors = pooledActors;
  }
  public SwimlaneInstance getSwimlaneInstance() {
    return swimlaneInstance;
  }
  public void setSwimlaneInstance(SwimlaneInstance swimlaneInstance) {
    this.swimlaneInstance = swimlaneInstance;
  }
  public String getPreviousActorId() {
    return previousActorId;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  private static final Log log = LogFactory.getLog(TaskInstance.class);
}
