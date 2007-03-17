package org.jbpm.graph.node;

import java.util.*;

import org.dom4j.Element;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.jpdl.xml.*;
import org.jbpm.taskmgmt.def.*;
import org.jbpm.taskmgmt.exe.*;

/**
 * is a node that relates to one or more tasks.
 * Property <code>signal</code> specifies how task completion 
 * triggers continuation of execution. 
 */
public class TaskNode extends Node implements Parsable {

  private static final long serialVersionUID = 1L;

  /**
   * execution always continues, regardless wether tasks are created or still unfinished.
   */
  public static final int SIGNAL_UNSYNCHRONIZED = 0;
  /**
   * execution never continues, regardless wether tasks are created or still unfinished.
   */
  public static final int SIGNAL_NEVER = 1;
  /**
   * proceeds execution when the first task instance is completed.   
   * when no tasks are created on entrance of this node, execution is continued.   
   */
  public static final int SIGNAL_FIRST = 2;
  /**
   * proceeds execution when the first task instance is completed.   
   * when no tasks are created on entrance of this node, execution is continued.   
   */
  public static final int SIGNAL_FIRST_WAIT = 3;
  /**
   * proceeds execution when the last task instance is completed.
   * when no tasks are created on entrance of this node, execution waits in the task node till tasks are created.   
   */
  public static final int SIGNAL_LAST = 4;
  /**
   * proceeds execution when the last task instance is completed.
   * when no tasks are created on entrance of this node, execution waits in the task node till tasks are created.   
   */
  public static final int SIGNAL_LAST_WAIT = 5;
  
  public static int parseSignal(String text) {
    if ("unsynchronized".equalsIgnoreCase(text)) {
      return SIGNAL_UNSYNCHRONIZED;
    } else if ("never".equalsIgnoreCase(text)) {
      return SIGNAL_NEVER;
    } else if ("first".equalsIgnoreCase(text)) {
      return SIGNAL_FIRST;
    } else if ("first-wait".equalsIgnoreCase(text)) {
      return SIGNAL_FIRST_WAIT;
    } else if ("last-wait".equalsIgnoreCase(text)) {
      return SIGNAL_LAST_WAIT;
    } else { // return default
      return SIGNAL_LAST;
    }
  }
  
  public static String signalToString(int signal) {
    if (signal==SIGNAL_UNSYNCHRONIZED) {
      return "unsynchronized";
    } else if (signal==SIGNAL_NEVER) {
      return "never";
    } else if (signal==SIGNAL_FIRST) {
      return "first";
    } else if (signal==SIGNAL_FIRST_WAIT) {
      return "first-wait";
    } else if (signal==SIGNAL_LAST) {
      return "last";
    } else if (signal==SIGNAL_LAST_WAIT) {
      return "last-wait";
    } else {
      return null;
    }
  }
  
  private long id = 0;
  private Set tasks = null;
  private int signal = SIGNAL_LAST;
  private boolean createTasks = true;
  
  public TaskNode() {
  }

  public TaskNode(String name) {
    super(name);
  }

  public void read(Element element, JpdlXmlReader jpdlReader) {
    // get the signal
    String signalText = element.attributeValue("signal");
    if (signalText!=null) {
      signal = parseSignal(signalText);
    }

    // create tasks
    String createTasksText = element.attributeValue("create-tasks");
    if (createTasksText!=null) {
      if (("no".equalsIgnoreCase(createTasksText))
           || ("false".equalsIgnoreCase(createTasksText)) ) {
        createTasks = false;
      }
    }
    
    // parse the tasks
    jpdlReader.readTasks(element, this);
  }

  public void addTask(Task task) {
    if (tasks==null) tasks = new HashSet();
    tasks.add(task);
    task.setTaskNode(this);
  }

  // node behaviour methods
  /////////////////////////////////////////////////////////////////////////////
  
  public void execute(ExecutionContext executionContext) {
    
    TaskMgmtInstance tmi = getTaskMgmtInstance(executionContext.getToken());
    
    // if this tasknode should create instances
    if ( (createTasks)
         && (tasks!=null) ) {
      Iterator iter = tasks.iterator();
      while (iter.hasNext()) {
        Task task = (Task) iter.next();
        executionContext.setTask(task);
        tmi.createTaskInstance(task, executionContext);
      }
    }

    // check if we should continue execution
    boolean continueExecution = false;
    switch (signal) {
      case SIGNAL_UNSYNCHRONIZED:
        continueExecution = true;
        break;
      case SIGNAL_FIRST_WAIT:
      case SIGNAL_LAST_WAIT:
      case SIGNAL_NEVER:
        continueExecution = false;
        break;
      case SIGNAL_FIRST:
      case SIGNAL_LAST:
        continueExecution = tmi.getSignallingTasks(executionContext).isEmpty();
    }

    if (continueExecution) {
      leave(executionContext);
    }
  }
  
  public void leave(ExecutionContext executionContext, Transition transition) {
    TaskMgmtInstance tmi = getTaskMgmtInstance(executionContext.getToken());
    if (tmi.hasBlockingTaskInstances(executionContext.getToken()) ) { 
      throw new IllegalStateException("task-node '"+name+"' still has blocking tasks");
    }
    removeTaskInstanceSynchronization(executionContext.getToken());
    super.leave(executionContext, transition);
  }
  
  // task behaviour methods
  /////////////////////////////////////////////////////////////////////////////

  public boolean completionTriggersSignal(TaskInstance taskInstance) {
    boolean completionTriggersSignal = false;
    if ( (signal==SIGNAL_FIRST)
         || (signal==SIGNAL_FIRST_WAIT) ) {
      completionTriggersSignal = true;
    } else if ( ( (signal==SIGNAL_LAST)
                  || (signal==SIGNAL_LAST_WAIT) )
                && (isLastToComplete(taskInstance) ) ){
      completionTriggersSignal = true;
    }
    return completionTriggersSignal;
  }

  private boolean isLastToComplete(TaskInstance taskInstance) {
    Token token = taskInstance.getToken();
    TaskMgmtInstance tmi = getTaskMgmtInstance(token);
    
    boolean isLastToComplete = true;
    Iterator iter = tmi.getTaskInstances().iterator();
    while ( iter.hasNext()
            && (isLastToComplete) ) {
      TaskInstance other = (TaskInstance) iter.next();
      if ( (other.getToken()==token)
           && (other!=taskInstance)
           && (!other.hasEnded()) ) {
        isLastToComplete = false;
      }
    }
    
    return isLastToComplete;
  }

  public void removeTaskInstanceSynchronization(Token token) {
    TaskMgmtInstance tmi = getTaskMgmtInstance(token);
    Collection taskInstances = tmi.getTaskInstances();
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        // remove signalling
        if (taskInstance.isSignalling()
            &&(token==taskInstance.getToken())) {
          taskInstance.setSignalling(false);
        }
        // remove blocking
        if (taskInstance.isBlocking()
            &&(token==taskInstance.getToken())) {
          taskInstance.setBlocking(false);
        }
      }
    }
  }

  private TaskMgmtInstance getTaskMgmtInstance(Token token) {
    return (TaskMgmtInstance) token.getProcessInstance().getInstance(TaskMgmtInstance.class);
  }

  // getters and setters
  /////////////////////////////////////////////////////////////////////////////

  /**
   * is a Map with the tasks, keyed by task-name or an empty map in case 
   * no tasks are present in this task-node. 
   */
  public Map getTasksMap() {
    Map tasksMap = new HashMap();
    if (tasks!=null) {
      Iterator iter = tasks.iterator();
      while (iter.hasNext()) {
        Task task = (Task) iter.next();
        tasksMap.put(task.getName(), task);
      }
    }
    return tasksMap;
  }
  
  /**
   * is the task in this task-node with the given name or null if the given task 
   * does not exist in this node. 
   */
  public Task getTask(String taskName) {
    return (Task) getTasksMap().get(taskName);
  }

  public long getId() {
    return id;
  }
  public Set getTasks() {
    return tasks;
  }
  public int getSignal() {
    return signal;
  }
  public boolean getCreateTasks() {
    return createTasks;
  }
}
