package org.jbpm.jpdl.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.db.JbpmSession;
import org.jbpm.graph.action.ActionTypes;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.ExceptionHandler;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.NodeCollection;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.NodeTypes;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.JpdlException;
import org.jbpm.scheduler.def.CancelTimerAction;
import org.jbpm.scheduler.def.CreateTimerAction;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.def.TaskMgmtDefinition;


public class JpdlXmlReader {
  
  Reader reader = null;
  List problems = new ArrayList();
  JbpmSession jbpmSession = null;
  ProcessDefinition processDefinition = null;
  Collection unresolvedTransitionDestinations = null;
  Collection unresolvedActionReferences = null;
  
  private static final String JPDL_SCHEMA_NAME = "jpdl-3.0.xsd";
  
  public JpdlXmlReader(Reader reader) {
    this.reader = reader;
  }

  public JpdlXmlReader(Reader reader, JbpmSession jbpmSession) {
    this.reader = reader;
    this.jbpmSession = jbpmSession;
  }

  public JbpmSession getJbpmSession() {
    return jbpmSession;
  }
  
  public void close() throws IOException {
    reader.close();
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    return reader.read(cbuf, off, len);
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  public void addProblem(Problem p) {
    problems.add(p);
  }
  
  public void addError(String description) {
    log.error("invalid process xml: "+description);
    addProblem(new Problem(Problem.LEVEL_ERROR, description));
  }

  public void addError(String description, Throwable exception) {
    log.error("invalid process xml: "+description, exception);
    addProblem(new Problem(Problem.LEVEL_ERROR, description, exception));
  }

  public void addWarning(String description) {
    log.warn("process xml warning: "+description);
    addProblem(new Problem(Problem.LEVEL_WARNING, description));
  }

  public ProcessDefinition readProcessDefinition() {
    // create a new definition
    processDefinition = ProcessDefinition.createNewProcessDefinition();

    // initialize lists
    problems = new ArrayList();
    unresolvedTransitionDestinations = new ArrayList();
    unresolvedActionReferences = new ArrayList();	
		
    // parse the document into a dom tree
    Document document = null;

    // validate the document using the process definition schema
    SchemaValidationHelper helper = new SchemaValidationHelper(reader, JPDL_SCHEMA_NAME, "process definition");

    if (helper.isValid()) {
      document = helper.getDocument();

    } else { // schema validation problems were encountered
      for (Iterator i = helper.getProblems().iterator(); i.hasNext();) {
        Problem problem = (Problem) i.next();
        this.addProblem(problem);
        log.debug(problem.getDescription());
      }

      throw new JpdlException(problems);
    }
	  
    Element root = document.getRootElement();
	        
    // read the process name
    processDefinition.setName(root.attributeValue("name"));

    // first pass: read most content
    readSwimlanes(root);
    readActions(root, null, null);
    readNodes(root, processDefinition);
    readEvents(root, processDefinition);
    readExceptionHandlers(root, processDefinition);
    readTasks(root, null);

    // second pass processing
    resolveTransitionDestinations();
    resolveActionReferences();
    verifySwimlaneAssignments();
    
    if (Problem.containsProblemsOfLevel(problems, Problem.LEVEL_ERROR)) {
      throw new JpdlException(problems);
    }

    return processDefinition;
  }
  
  private void readSwimlanes(Element processDefinitionElement) {
    Iterator iter = processDefinitionElement.elementIterator("swimlane");
    TaskMgmtDefinition taskMgmtDefinition = processDefinition.getTaskMgmtDefinition();
    while (iter.hasNext()) {
      Element swimlaneElement = (Element) iter.next();
      String swimlaneName = swimlaneElement.attributeValue("name");
      if (swimlaneName==null) {
        addWarning("there's a swimlane without a name");
      } else {
        Swimlane swimlane = new Swimlane(swimlaneName);
        Element assignmentElement = swimlaneElement.element("assignment");

        if (assignmentElement!=null) {
          Delegation assignmentDelegation = readAssignmentDelegation(assignmentElement);
          swimlane.setAssignmentDelegation(assignmentDelegation);
          
        } else {
          Task startTask = taskMgmtDefinition.getStartTask();
          if ( (startTask==null)
               || (startTask.getSwimlane()!=swimlane)
             ) {
            addWarning("swimlane '"+swimlaneName+"' does not have an assignment");
          }
        }
        taskMgmtDefinition.addSwimlane(swimlane);
      }
    }
  }

  public void readNodes(Element element, NodeCollection nodeCollection) {
    Iterator nodeElementIter = element.elementIterator();
    while (nodeElementIter.hasNext()) {
      Element nodeElement = (Element) nodeElementIter.next();
      String nodeName = nodeElement.getName();
      // get the node type
      Class nodeType = NodeTypes.getNodeType(nodeName);
      if (nodeType!=null) {
        try {
          // create a new instance
          Node node = (Node) nodeType.newInstance();
          node.setProcessDefinition(processDefinition);
          // read the common node parts of the element
          readNode(nodeElement, node, nodeCollection);
        
          // if the node is parsable 
          // (meaning: if the node has special configuration to parse, other then the 
          //  common node data)
          node.read(nodeElement, this);

        } catch (Exception e) {
          log.error("couldn't instantiate node '"+nodeName+"', of type '"+nodeType.getName()+"'", e);
        }
      }
    }
  }

  public void readTasks(Element element, TaskNode taskNode) {
    List elements = element.elements("task");
    TaskMgmtDefinition tmd = (TaskMgmtDefinition) processDefinition.getDefinition(TaskMgmtDefinition.class); 
    if (elements.size()>0) {
      if (tmd==null) {
        tmd = new TaskMgmtDefinition();
      }
      processDefinition.addDefinition(tmd);
      
      Iterator iter = elements.iterator();
      while (iter.hasNext()) {
        Element taskElement = (Element) iter.next();
        readTask(taskElement, tmd, taskNode);
      }
    }
  }

  public Task readTask(Element taskElement, TaskMgmtDefinition taskMgmtDefinition, TaskNode taskNode) {
    Task task = new Task();
    task.setProcessDefinition(processDefinition);
    
    // get the task name
    String name = taskElement.attributeValue("name");
    if (name!=null) {
      task.setName(name);
      taskMgmtDefinition.addTask(task);
    } else if (taskNode!=null) {
      task.setName(taskNode.getName());
      taskMgmtDefinition.addTask(task);
    }
    
    // parse common subelements
    readTaskTimers(taskElement, task);
    readEvents(taskElement, task);
    readExceptionHandlers(taskElement, task);

    // description and duration
    task.setDescription(taskElement.attributeValue("description"));
    String duedateText = taskElement.attributeValue("duedate");
    if (duedateText==null) {
      duedateText = taskElement.attributeValue("dueDate");
    }
    task.setDueDate(duedateText);
    String priorityText = taskElement.attributeValue("priority");
    if (priorityText!=null) {
      task.setPriority(Task.parsePriority(priorityText));
    }
    
    // if this task is in the context of a taskNode, associate them
    if (taskNode!=null) {
      taskNode.addTask(task);
    }

    // blocking
    String blockingText = taskElement.attributeValue("blocking");
    if (blockingText!=null) {
      if ( ("true".equalsIgnoreCase(blockingText))
           || ("yes".equalsIgnoreCase(blockingText))
           || ("on".equalsIgnoreCase(blockingText)) ) {
        task.setBlocking(true);
      }
    }
    
    // assignment
    String swimlaneName = taskElement.attributeValue("swimlane");
    Element assignmentElement = taskElement.element("assignment");

    // if there is a swimlane attribute specified
    if (swimlaneName!=null) {
      Swimlane swimlane = taskMgmtDefinition.getSwimlane(swimlaneName);
      if (swimlane==null) {
        addWarning("task references unknown swimlane '"+swimlaneName+"':"+taskElement.asXML());
      } else {
        task.setSwimlane(swimlane);
      }

    // else if there is a direct assignment specified
    } else if (assignmentElement!=null) {
      Delegation assignmentDelegation = readAssignmentDelegation(assignmentElement);
      task.setAssignmentDelegation(assignmentDelegation);

    // if no assignment or swimlane is specified
    } else {
      // the user has to manage assignment manually, so we better warn him/her.
      addWarning("warning: no swimlane or assignment specified for task '"+taskElement.asXML()+"'");
    }
    
    // task controller
    Element taskControllerElement = taskElement.element("controller");
    if (taskControllerElement!=null) {
      task.setTaskController(readTaskController(taskControllerElement));
    }
    
    return task;
  }

  private Delegation readAssignmentDelegation(Element assignmentElement) {
    Delegation assignmentDelegation = new Delegation();
    String expression = assignmentElement.attributeValue("expression");
    if (expression!=null) {
      assignmentDelegation.setProcessDefinition(processDefinition);
      assignmentDelegation.setClassName("org.jbpm.identity.assignment.ExpressionAssignmentHandler");
      assignmentDelegation.setConfiguration("<expression>"+expression+"</expression>");
      
    } else {
      assignmentDelegation.read(assignmentElement, this);
    }
    return assignmentDelegation;
  }

  private TaskController readTaskController(Element taskControllerElement) {
    TaskController taskController = new TaskController();

    if (taskControllerElement.attributeValue("class")!=null) {
      Delegation taskControllerDelegation = new Delegation();
      taskControllerDelegation.read(taskControllerElement, this);
      taskController.setTaskControllerDelegation(taskControllerDelegation);

    } else {
      List variableAccesses = readVariableAccesses(taskControllerElement);
      taskController.setVariableAccesses(variableAccesses);
    }
    return taskController;
  }
  
  public List readVariableAccesses(Element element) {
    List variableAccesses = new ArrayList();
    Iterator iter = element.elementIterator("variable");
    while (iter.hasNext()) {
      Element variableElement = (Element) iter.next();
      
      String variableName = variableElement.attributeValue("name");
      if (variableName==null) {
        addProblem(new Problem(Problem.LEVEL_WARNING, "the name attribute of a variable element is required: "+variableElement.asXML()));
      }
      String access = variableElement.attributeValue("access", "read,write");
      String mappedName = variableElement.attributeValue("mapped-name");
      
      variableAccesses.add(new VariableAccess(variableName, access, mappedName));
    }
    return variableAccesses;
  }

  public void readStartStateTask(Element startTaskElement, StartState startState) {
    TaskMgmtDefinition taskMgmtDefinition = processDefinition.getTaskMgmtDefinition();
    Task startTask = readTask(startTaskElement, taskMgmtDefinition, null);
    startTask.setStartState(startState);
    if (startTask.getName()==null) {
      startTask.setName(startState.getName());
    }
    taskMgmtDefinition.setStartTask(startTask);
  }

  public void readNode(Element nodeElement, Node node, NodeCollection nodeCollection) {
    // get the action name
    String name = nodeElement.attributeValue("name");
    if (name!=null) {
      node.setName(name);
    }

    // add the node to the parent
    nodeCollection.addNode(node);

    // parse common subelements
    readNodeTimers(nodeElement, node);
    readEvents(nodeElement, node);
    readExceptionHandlers(nodeElement, node);

    // save the transitions and parse them at the end
    addUnresolvedTransitionDestination(nodeElement, node);
  }

  private void readNodeTimers(Element nodeElement, Node node) {
    Iterator iter = nodeElement.elementIterator("timer");
    while (iter.hasNext()) {
      Element timerElement = (Element) iter.next();
      readNodeTimer(timerElement, node);
    }
  }

  private void readNodeTimer(Element timerElement, Node node) {
    String name = timerElement.attributeValue("name", node.getName());
    
    CreateTimerAction createTimerAction = new CreateTimerAction();
    createTimerAction.read(timerElement, this);
    createTimerAction.setTimerName(name);
    createTimerAction.setTimerAction(readSingleAction(timerElement));
    addAction(node, Event.EVENTTYPE_NODE_ENTER, createTimerAction);
    
    CancelTimerAction cancelTimerAction = new CancelTimerAction();
    cancelTimerAction.setTimerName(name);
    addAction(node, Event.EVENTTYPE_NODE_LEAVE, cancelTimerAction);
  }
  
  private void readTaskTimers(Element taskElement, Task task) {
    Iterator iter = taskElement.elementIterator("timer");
    while (iter.hasNext()) {
      Element timerElement = (Element) iter.next();
      readTaskTimer(timerElement, task);
    }
  }

  private void readTaskTimer(Element timerElement, Task task) {
    String name = timerElement.attributeValue("name", task.getName());
    if (name==null) name = "timer-for-task-"+task.getId();
    
    CreateTimerAction createTimerAction = new CreateTimerAction();
    createTimerAction.read(timerElement, this);
    createTimerAction.setTimerName(name);
    createTimerAction.setTimerAction(readSingleAction(timerElement));
    addAction(task, Event.EVENTTYPE_TASK_CREATE, createTimerAction);

    // read the cancel-event types
    Collection cancelEventTypes = new ArrayList();

    String cancelEventTypeText = timerElement.attributeValue("cancel-event");
    if (cancelEventTypeText!=null) {
      // cancel-event is a comma separated list of events
      StringTokenizer tokenizer = new StringTokenizer(cancelEventTypeText, ",");
      while (tokenizer.hasMoreTokens()) {
        cancelEventTypes.add(tokenizer.nextToken().trim());
      }
    } else {
      // set the default
      cancelEventTypes.add(Event.EVENTTYPE_TASK_END);
    }
    
    Iterator iter = cancelEventTypes.iterator();
    while (iter.hasNext()) {
      String cancelEventType = (String) iter.next();
      CancelTimerAction cancelTimerAction = new CancelTimerAction();
      cancelTimerAction.setTimerName(name);
      addAction(task, cancelEventType, cancelTimerAction);
    }
  }
  
  private void readEvents(Element parentElement, GraphElement graphElement) {
    Iterator iter = parentElement.elementIterator("event");
    while (iter.hasNext()) {
      Element eventElement = (Element) iter.next();
      String eventType = eventElement.attributeValue("type");
      if (!graphElement.hasEvent(eventType)) {
        graphElement.addEvent(new Event(eventType));
      }
      readActions(eventElement, graphElement, eventType);
    }
  }

  public void readActions(Element eventElement, GraphElement graphElement, String eventType) {
    // for all the elements in the event element
    Iterator nodeElementIter = eventElement.elementIterator();
    while (nodeElementIter.hasNext()) {
      Element actionElement = (Element) nodeElementIter.next();
      String actionName = actionElement.getName();
      if (ActionTypes.hasActionName(actionName)) {
        Action action = createAction(actionElement);
        if ( (graphElement!=null)
             && (eventType!=null)
           ) {
          // add the action to the event
          addAction(graphElement, eventType, action);
        }
      }
    }
  }

  private void addAction(GraphElement graphElement, String eventType, Action action) {
    Event event = graphElement.getEvent(eventType);
    if (event==null) {
      event = new Event(eventType); 
      graphElement.addEvent(event);
    }
    event.addAction(action);
  }
  
  public Action readSingleAction(Element nodeElement) {
    Action action = null;
    // search for the first action element in the node
    Iterator iter = nodeElement.elementIterator();
    while (iter.hasNext() && (action==null)) {
      Element candidate = (Element) iter.next();
      if (ActionTypes.hasActionName(candidate.getName())) {
        // parse the action and assign it to this node
        action = createAction(candidate);
      }
    }
    return action;
  }

  public Action createAction(Element actionElement) {
    // create a new instance of the action
    Action action = null;
    String actionName = actionElement.getName();
    Class actionType = ActionTypes.getActionType(actionName);
    try {
      action = (Action) actionType.newInstance();
    } catch (Exception e) {
      log.error("couldn't instantiate action '"+actionName+"', of type '"+actionType.getName()+"'", e);
    }

    // read the common node parts of the action
    readAction(actionElement, action);
    
    return action;
  }

  public void readAction(Element element, Action action) {
    // if a name is specified for this action
    String actionName = element.attributeValue("name");
    if (actionName!=null) {
      action.setName(actionName);
      // add the action to the named process action repository 
      processDefinition.addAction(action);
    }

    // if the action is parsable 
    // (meaning: if the action has special configuration to parse, other then the common node data)
    action.read(element, this);
  }

  private void readExceptionHandlers(Element graphElementElement, GraphElement graphElement) {
    Iterator iter = graphElementElement.elementIterator("exception-handler");
    while (iter.hasNext()) {
      Element exceptionHandlerElement = (Element) iter.next();
      readExceptionHandler(exceptionHandlerElement, graphElement);
    }
  }

  private void readExceptionHandler(Element exceptionHandlerElement, GraphElement graphElement) {
    // create the exception handler
    ExceptionHandler exceptionHandler = new ExceptionHandler();
    exceptionHandler.setExceptionClassName(exceptionHandlerElement.attributeValue("exception-class"));
    // add it to the graph element
    graphElement.addExceptionHandler(exceptionHandler);

    // read the actions in the body of the exception-handler element
    Iterator iter = exceptionHandlerElement.elementIterator();
    while (iter.hasNext()) {
      Element childElement = (Element) iter.next();
      if (ActionTypes.hasActionName(childElement.getName())) {
        Action action = createAction(childElement);
        exceptionHandler.addAction(action);
      }
    }
  }

  // transition destinations are parsed in a second pass //////////////////////
  
  public void addUnresolvedTransitionDestination(Element nodeElement, Node node) {
    unresolvedTransitionDestinations.add(new Object[]{nodeElement, node});
  }

  public void resolveTransitionDestinations() {
    Iterator iter = unresolvedTransitionDestinations.iterator();
    while (iter.hasNext()) {
      Object[] unresolvedTransition = (Object[]) iter.next();
      Element nodeElement = (Element) unresolvedTransition[0];
      Node node = (Node) unresolvedTransition[1];
      resolveTransitionDestinations(nodeElement.elements("transition"), node);
    }
  }

  public void resolveTransitionDestinations(List transitionElements, Node node) {
    Iterator iter = transitionElements.iterator();
    while (iter.hasNext()) {
      Element transitionElement = (Element) iter.next();
      resolveTransitionDestination(transitionElement, node);
    }
  }

  public void resolveTransitionDestination(Element transitionElement, Node node) {
    Transition transition = new Transition();
    transition.setProcessDefinition(processDefinition);
    
    // get the action name
    String name = transitionElement.attributeValue("name");
    if (name!=null) {
      transition.setName(name);
    }

    // add the transition to the node
    node.addLeavingTransition(transition);

    // set destinationNode of the transition
    String toName = transitionElement.attributeValue("to");
    if (toName==null) {
      addWarning("node '"+node.getFullyQualifiedName()+"' has a transition without a 'to'-attribute to specify its destinationNode");
    } else {
      Node to = ((NodeCollection)node.getParent()).findNode(toName);
      if (to==null) {
        addWarning("transition to='"+toName+"' on node '"+node.getFullyQualifiedName()+"' cannot be resolved");
      } else {
        to.addArrivingTransition(transition);
      }
    }
    
    // read the actions
    readActions(transitionElement, transition, Event.EVENTTYPE_TRANSITION);
    
    readExceptionHandlers(transitionElement, transition);
  }
  
  // action references are parsed in a second pass ////////////////////////////

  public void addUnresolvedActionReference(Element actionElement, Action action) {
    unresolvedActionReferences.add(new Object[]{actionElement, action});
  }

  public void resolveActionReferences() {
    Iterator iter = unresolvedActionReferences.iterator();
    while (iter.hasNext()) {
      Object[] unresolvedActionReference = (Object[]) iter.next();
      Element actionElement = (Element) unresolvedActionReference[0];
      Action action = (Action) unresolvedActionReference[1];
      String referencedActionName = actionElement.attributeValue("ref-name");
      Action referencedAction = processDefinition.getAction(referencedActionName);
      if (referencedAction==null) {
        addWarning("couldn't resolve action reference in "+actionElement.asXML());
      }
      action.setReferencedAction(referencedAction);
    }
  }

  // verify swimlane assignments in second pass ///////////////////////////////
  public void verifySwimlaneAssignments() {
    TaskMgmtDefinition taskMgmtDefinition = processDefinition.getTaskMgmtDefinition();
    if ( (taskMgmtDefinition!=null)
         && (taskMgmtDefinition.getSwimlanes()!=null)
       ) {
      Iterator iter = taskMgmtDefinition.getSwimlanes().values().iterator();
      while (iter.hasNext()) {
        Swimlane swimlane = (Swimlane) iter.next();
        
        Task startTask = taskMgmtDefinition.getStartTask();
        Swimlane startTaskSwimlane = (startTask!=null ? startTask.getSwimlane() : null);
        
        if ( (swimlane.getAssignmentDelegation()==null)
             && (swimlane!=startTaskSwimlane) 
           ) {
          addWarning("swimlane '"+swimlane.getName()+"' does not have an assignment");
        }
      }
    }
  }

  private static final Log log = LogFactory.getLog(JpdlXmlReader.class);
}
