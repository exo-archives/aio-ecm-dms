package org.jbpm.taskmgmt.def;

import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.instantiation.Delegation;

/**
 * defines a task and how the actor must be calculated at runtime.
 */
public class Task extends GraphElement {
  
  private static final long serialVersionUID = 1L;
  
  public static final int PRIORITY_HIGHEST = 1;
  public static final int PRIORITY_HIGH = 2;
  public static final int PRIORITY_NORMAL = 3;
  public static final int PRIORITY_LOW = 4;
  public static final int PRIORITY_LOWEST = 5;
  
  public static int parsePriority(String priorityText) {
    if ("highest".equalsIgnoreCase(priorityText)) return PRIORITY_HIGHEST;
    else if ("high".equalsIgnoreCase(priorityText)) return PRIORITY_HIGH;
    else if ("normal".equalsIgnoreCase(priorityText)) return PRIORITY_NORMAL;
    else if ("low".equalsIgnoreCase(priorityText)) return PRIORITY_LOW;
    else if ("lowest".equalsIgnoreCase(priorityText)) return PRIORITY_LOWEST;
    try {
      return Integer.parseInt(priorityText);
    } catch (NumberFormatException e) {
      throw new RuntimeException("priority '"+priorityText+"' could not be parsed as a priority");
    }
  }

  protected String description = null;
  protected boolean isBlocking = false;
  protected String dueDate = null;
  protected int priority = PRIORITY_NORMAL;
  protected TaskNode taskNode = null;
  protected StartState startState = null;
  protected TaskMgmtDefinition taskMgmtDefinition = null;
  protected Swimlane swimlane = null;
  protected Delegation assignmentDelegation = null;
  protected TaskController taskController = null;
  
  public Task() {
  }

  public Task(String name) {
    this.name = name;
  }

  // event types //////////////////////////////////////////////////////////////

  private static final String[] supportedEventTypes = new String[]{
    Event.EVENTTYPE_TASK_CREATE,
    Event.EVENTTYPE_TASK_ASSIGN,
    Event.EVENTTYPE_TASK_START,
    Event.EVENTTYPE_TASK_END
  };
  public String[] getSupportedEventTypes() {
    return supportedEventTypes;
  }

  // task instance factory methods ////////////////////////////////////////////
  
  /**
   * sets the swimlane unidirectionally.  Since a task can have max one of swimlane or assignmentHandler, 
   * this method removes the assignmentHandler if it is set.  To create a bidirectional relation, use 
   * {@link Swimlane#addTask(Task)}.
   */
  public void setSwimlane(Swimlane swimlane) {
    this.swimlane = swimlane;
    assignmentDelegation = null;
  }
  /**
   * sets the taskNode unidirectionally.  use {@link TaskNode#addTask(Task)} to create 
   * a bidirectional relation.
   */
  public void setTaskNode(TaskNode taskNode) {
    this.taskNode = taskNode;
  }
  
  /**
   * sets the taskMgmtDefinition unidirectionally.  use TaskMgmtDefinition.addTask to create 
   * a bidirectional relation.
   */
  public void setTaskMgmtDefinition(TaskMgmtDefinition taskMgmtDefinition) {
    this.taskMgmtDefinition = taskMgmtDefinition;
  }

  /**
   * sets the swimlane.  Since a task can have max one of swimlane or assignmentHandler, 
   * this method removes the swimlane if it is set.
   */
  public void setAssignmentDelegation(Delegation assignmentDelegation) {
    this.assignmentDelegation = assignmentDelegation;
    this.swimlane = null;
  }

  // parent ///////////////////////////////////////////////////////////////////

  public GraphElement getParent() {
    if (taskNode!=null) {
      return taskNode;
    } 
    if (startState!=null) {
      return startState;
    } 
    return processDefinition;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public TaskMgmtDefinition getTaskMgmtDefinition() {
    return taskMgmtDefinition;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Swimlane getSwimlane() {
    return swimlane;
  }
  public boolean isBlocking() {
    return isBlocking;
  }
  public void setBlocking(boolean isBlocking) {
    this.isBlocking = isBlocking;
  }
  public TaskNode getTaskNode() {
    return taskNode;
  }
  public Delegation getAssignmentDelegation() {
    return assignmentDelegation;
  }
  public String getDueDate() {
    return dueDate;
  }
  public void setDueDate(String duedate) {
    this.dueDate = duedate;
  }
  public TaskController getTaskController() {
    return taskController;
  }
  public void setTaskController(TaskController taskController) {
    this.taskController = taskController;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public StartState getStartState() {
    return startState;
  }
  public void setStartState(StartState startState) {
    this.startState = startState;
  }
}
