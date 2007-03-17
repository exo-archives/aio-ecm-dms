package org.jbpm.taskmgmt.exe;

import java.util.*;

import org.jbpm.JbpmConfiguration;
import org.jbpm.db.JbpmSession;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.*;
import org.jbpm.instantiation.ClassLoaderUtil;
import org.jbpm.instantiation.Delegation;
import org.jbpm.module.exe.*;
import org.jbpm.security.Authentication;
import org.jbpm.taskmgmt.def.*;
import org.jbpm.taskmgmt.log.TaskCreateLog;

/**
 * process instance extension for managing tasks on a process instance.
 */
public class TaskMgmtInstance extends ModuleInstance {

  private static final long serialVersionUID = 1L;

  private TaskMgmtDefinition taskMgmtDefinition = null;
  private Map swimlaneInstances = null;
  private Set taskInstances = null;
  
  public TaskMgmtInstance() {
  }
  
  public TaskMgmtInstance(TaskMgmtDefinition taskMgmtDefinition) {
    this.taskMgmtDefinition = taskMgmtDefinition;
  }

  // the task instance class is configurable so that users can extend from TaskInstance to create their custom extensions.
  private static Class taskInstanceClass = null;
  
  public static Class getTaskInstanceClass() {
    if (taskInstanceClass==null) {
      String taskInstanceClassName = JbpmConfiguration.getString("jbpm.task.instance.class");
      taskInstanceClass = ClassLoaderUtil.loadClass(taskInstanceClassName);
    }
    return taskInstanceClass;
  }
  
  // task instances ///////////////////////////////////////////////////////////

  public TaskInstance createTaskInstance() {
    return createTaskInstance(null, (ExecutionContext)null);
  }

  public TaskInstance createTaskInstance(Task task) {
    return createTaskInstance(task, (ExecutionContext)null);
  }

  public TaskInstance createTaskInstance(Token token) {
    return createTaskInstance(null, new ExecutionContext(token));
  }

  /**
   * creates a new task instance on the given token, for the given task.
   */
  public TaskInstance createTaskInstance(Task task, Token token) {
    return createTaskInstance(task, new ExecutionContext(token));
  }

  /**
   * creates a new task instance on the given task, in the given execution context.
   */
  public TaskInstance createTaskInstance(Task task, ExecutionContext executionContext) {
    TaskInstance taskInstance = null;

    // instantiate the new task instance
    taskInstance = instantiateNewTaskInstance();

    // bind the task instance to the TaskMgmtInstance
    addTaskInstance(taskInstance);

    // initialize the task instance
    if (task!=null) taskInstance.setTask(task);

    // if there is database session
    JbpmSession currentSession = JbpmSession.getCurrentJbpmSession();
    if (currentSession!=null) {
      // save the new task instance to give it an id 
      currentSession.getSession().save(taskInstance);
    }

    if (executionContext!=null) {
      Token token = executionContext.getToken();

      taskInstance.setToken(token);
      
      try {
        // update the executionContext
        executionContext.setTask(task);
        executionContext.setTaskInstance(taskInstance);
        executionContext.setEventSource(task);
        
        // if this task instance is created for a task, perform assignment
        if (task!=null) {
          taskInstance.assign(executionContext);
        }
        
        taskInstance.create(executionContext);
      } finally {
        // clean the executionContext
        executionContext.setTask(null);
        executionContext.setTaskInstance(null);
        executionContext.setEventSource(null);
      }
      
      // log this creation 
      token.addLog(new TaskCreateLog(taskInstance, taskInstance.getActorId()));

    } else {
      taskInstance.create();
    }
    
    return taskInstance;
  }

  public SwimlaneInstance getInitializedSwimlaneInstance(ExecutionContext executionContext, Swimlane swimlane) {
    // initialize the swimlane
    if (swimlaneInstances==null) swimlaneInstances = new HashMap();
    SwimlaneInstance swimlaneInstance = (SwimlaneInstance) swimlaneInstances.get(swimlane.getName());
    if (swimlaneInstance==null) {
      swimlaneInstance = new SwimlaneInstance(swimlane);
      addSwimlaneInstance(swimlaneInstance);
      // assign the swimlaneInstance
      invokeAssignmentHandler(swimlane.getAssignmentDelegation(), swimlaneInstance, executionContext);
    }

    return swimlaneInstance;
  }
  
  public void invokeAssignmentHandler(Delegation assignmentDelegation, Assignable assignable, ExecutionContext executionContext) {
    try {
      if (assignmentDelegation!=null) {
        // instantiate the assignment handler
        AssignmentHandler assignmentHandler = (AssignmentHandler) assignmentDelegation.instantiate();
        // invoke the assignment handler
        assignmentHandler.assign(assignable, executionContext);
      }
      
    } catch (Exception exception) {
      GraphElement graphElement = executionContext.getEventSource();
      if (graphElement!=null) {
        graphElement.raiseException(exception, executionContext);
      } else {
        throw new DelegationException(exception, executionContext);
      }
    }
  }


  /**
   * creates a task instance on the rootToken, and assigns it 
   * to the currently authenticated user.
   */
  public TaskInstance createStartTaskInstance() {
    TaskInstance taskInstance = null;
    Task startTask = taskMgmtDefinition.getStartTask();
    Token rootToken = processInstance.getRootToken();
    ExecutionContext executionContext = new ExecutionContext(rootToken);
    taskInstance = createTaskInstance(startTask, executionContext);
    taskInstance.setActorId(Authentication.getAuthenticatedActorId());
    return taskInstance;
  }

  private TaskInstance instantiateNewTaskInstance() {
    TaskInstance newTaskInstance = null;
    try {
      newTaskInstance = (TaskInstance) getTaskInstanceClass().newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("couldn't instantiate task instance '"+taskInstanceClass.getName()+"'", e);
    }
    return newTaskInstance;
  }

  /**
   * is true if the given token has task instances that keep the 
   * token from leaving the current node.
   */
  public boolean hasBlockingTaskInstances(Token token) {
    boolean hasBlockingTasks = false;
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while ( (iter.hasNext())
              && (! hasBlockingTasks)) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if ( (! taskInstance.hasEnded())
             && (taskInstance.isBlocking())
             && (taskInstance.getToken()==token) ) {
          hasBlockingTasks = true;
        }
      }
    }
    return hasBlockingTasks;
  }

  /**
   * is true if the given token has task instances that are not yet ended.
   */
  public boolean hasUnfinishedTasks(Token token) {
    return (getUnfinishedTasks(token).size()>0);
  }

  /**
   * is the collection of {@link TaskInstance}s on the given token that are not ended.
   */
  public Collection getUnfinishedTasks(Token token) {
    Collection unfinishedTasks = new ArrayList();
    if ( taskInstances != null ) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance task = (TaskInstance) iter.next();
        if ( (!task.hasEnded())
             && (token == task.getToken()) ) {
          unfinishedTasks.add( task );
        }
      }
    }
    return unfinishedTasks;
  }

  /**
   * is true if there are {@link TaskInstance}s on the given token that can trigger 
   * the token to continue.
   */
  public boolean hasSignallingTasks(ExecutionContext executionContext) {
    return (getSignallingTasks(executionContext).size()>0);
  }

  /**
   * is the collection of {@link TaskInstance}s for the given token that can trigger 
   * the token to continue.
   */
  public Collection getSignallingTasks(ExecutionContext executionContext) {
    Collection signallingTasks = new ArrayList();
    if ( taskInstances != null ) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if (taskInstance.isSignalling()
            &&(executionContext.getToken()==taskInstance.getToken())) {
          signallingTasks.add(taskInstance);
        }
      }
    }
    return signallingTasks;
  }
  
  /**
   * returns all the taskInstances for the this process instance.  This 
   * includes task instances that have been completed previously.
   */
  public Collection getTaskInstances() {
    return taskInstances;
  }
  public void addTaskInstance(TaskInstance taskInstance) {
    if (taskInstances==null) taskInstances = new HashSet();
    taskInstances.add(taskInstance);
    taskInstance.setTaskMgmtInstance(this);
  }
  public void removeTaskInstance(TaskInstance taskInstance) {
    if (taskInstances!=null) {
      taskInstances.remove(taskInstance);
    }
  }

  // swimlane instances ///////////////////////////////////////////////////////

  public Map getSwimlaneInstances() {
    return swimlaneInstances;
  }
  public void addSwimlaneInstance( SwimlaneInstance swimlaneInstance ) {
    if (swimlaneInstances==null) swimlaneInstances = new HashMap();
    swimlaneInstances.put(swimlaneInstance.getName(), swimlaneInstance);
    swimlaneInstance.setTaskMgmtInstance(this);
  }
  public SwimlaneInstance getSwimlaneInstance(String swimlaneName) {
    return (SwimlaneInstance) (swimlaneInstances!=null ? swimlaneInstances.get(swimlaneName) : null );
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public TaskMgmtDefinition getTaskMgmtDefinition() {
    return taskMgmtDefinition;
  }

  // private static final Log log = LogFactory.getLog(TaskMgmtInstance.class);
}
