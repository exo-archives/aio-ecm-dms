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
package org.exoplatform.services.cms.actions.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionPlugin;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.quartz.JobDataMap;

abstract public class BaseActionPlugin implements ActionPlugin {
    
  final static String JOB_NAME_PREFIX = "activate_".intern() ;
  final static String PERIOD_JOB = "period".intern() ;
  final static String CRON_JOB = "cron".intern() ;

  final static String SCHEDULABLE_INFO_MIXIN = "exo:schedulableInfo".intern() ;
  final static String SCHEDULED_INITIATOR = "exo:scheduledInitiator".intern() ;
  final static String JOB_NAME_PROP = "exo:jobName".intern() ;
  final static String JOB_GROUP_PROP = "exo:jobGroup".intern() ;
  final static String JOB_DESCRIPTION_PROP = "exo:jobDescription".intern() ;
  final static String JOB_CLASS_PROP = "exo:jobClass".intern() ;
  final static String SCHEDULE_TYPE_PROP = "exo:scheduleType".intern() ;
  final static String START_TIME_PROP = "exo:startTime".intern() ;
  final static String END_TIME_PROP = "exo:endTime".intern() ;
  final static String REPEAT_COUNT_PROP = "exo:repeatCount".intern() ;
  final static String TIME_INTERVAL_PROP = "exo:timeInterval".intern() ;
  final static String CRON_EXPRESSION_PROP = "exo:cronExpression".intern() ;

  final static String LIFECYCLE_PHASE_PROP = "exo:lifecyclePhase".intern() ;
  final static String NODE_NAME_PROP = "exo:name".intern() ;
  final static String COUNTER_PROP = "exo:counter".intern() ;
  final static String EXO_ACTIONS = "exo:actions".intern();
  final static String ACTION_STORAGE= "exo:actionStorage".intern();
  final static long BUFFER_TIME = 500*1000 ; 

  final static String actionNameVar = "actionName".intern() ;
  final static String srcRepository = "repository".intern() ;
  final static String srcWorkspaceVar = "srcWorkspace".intern() ;
  final static String initiatorVar = "initiator".intern() ;
  final static String srcPathVar = "srcPath".intern() ;
  final static String executableVar = "executable".intern() ;  

  protected Map<String, ECMEventListener> listeners_ = new HashMap<String, ECMEventListener>();
  private static final Log LOG  = ExoLogger.getLogger(BaseActionPlugin.class);

  abstract protected String getRepositoryName();  
  abstract protected List<RepositoryEntry> getRepositories();
  abstract protected String getWorkspaceName();
  abstract protected ManageableRepository getRepository(String repositoryName) throws Exception;
  abstract protected String getActionType();
  abstract protected List getActions();
  abstract protected ECMEventListener createEventListener(String actionName,
      String actionExecutable, String repository, String srcWorkspace, String srcPath,
      Map variables, String actiontype) throws Exception;

  abstract protected Class createActivationJob() throws Exception ;  
  
  public void addAction(String actionType, String repository, String srcWorkspace, String srcPath, Map mappings) throws Exception {
    addAction(actionType, repository, srcWorkspace, srcPath, true, null, null, mappings);
  }

  public void addAction(String actionType, String repository, String srcWorkspace, String srcPath, boolean isDeep, String[] uuid, String[] nodeTypeNames, Map mappings) throws Exception {
    String actionName = 
      (String) ((JcrInputProperty) mappings.get("/node/exo:name")).getValue();    
    mappings.remove("/node/exo:name");
    String type = 
      (String) ((JcrInputProperty) mappings.get("/node/exo:lifecyclePhase")).getValue();               
    String actionExecutable = getActionExecutable(actionType);
    if (ActionServiceContainer.READ_PHASE.equals(type)) return;    
    else if(ActionServiceContainer.SCHEDULE_PHASE.equals(type)) {
      scheduleActionActivationJob(repository, srcWorkspace, srcPath, actionName,
          actionType, actionExecutable, mappings) ;
    }else {
      Map<String,Object> variables = getExecutionVariables(mappings) ;
      ECMEventListener listener = createEventListener(actionName, actionExecutable, repository,
          srcWorkspace, srcPath, variables, actionType);
      Session session = getSystemSession(repository, srcWorkspace);
      ObservationManager obsManager = session.getWorkspace().getObservationManager();
    //TODO all actions are stored at srcNode/exo:actions node
      String listenerKey = repository + ":" + srcPath + "/exo:actions/" +actionName;
      if(listeners_.containsKey(listenerKey)){
        obsManager.removeEventListener(listeners_.get(listenerKey));
        listeners_.remove(listenerKey);      
      }
      
      if (ActionServiceContainer.ADD_PHASE.equals(type)) {
        obsManager.addEventListener(listener, Event.NODE_ADDED, srcPath, isDeep, uuid, nodeTypeNames, false);
      } else if (ActionServiceContainer.REMOVE_PHASE.equals(type)) {
        obsManager.addEventListener(listener, Event.NODE_REMOVED, srcPath, isDeep, uuid, nodeTypeNames, false);
      } else {
        obsManager.addEventListener(listener, Event.PROPERTY_CHANGED, srcPath, isDeep, uuid, nodeTypeNames, false);
      }
      
      session.logout();
      listeners_.put(listenerKey, listener);
    }        
  }
  
  public void initiateActionObservation(Node storedActionNode, String repository) throws Exception {
    String actionName = storedActionNode.getProperty("exo:name").getString() ;
    String lifecyclePhase = storedActionNode.getProperty("exo:lifecyclePhase").getString();
    String[] uuid = storedActionNode.hasProperty("exo:uuid") ? 
                     parseValuesToArray(storedActionNode.getProperty("exo:uuid").getValues())
                     : null;
    boolean isDeep = storedActionNode.hasProperty("exo:isDeep") ?
                      storedActionNode.getProperty("exo:isDeep").getBoolean()
                      : true;
    String[] nodeTypeName = storedActionNode.hasProperty("exo:nodeTypeName") ?
                             parseValuesToArray(storedActionNode.getProperty("exo:nodeTypeName").getValues())
                             : null;
    String actionType = storedActionNode.getPrimaryNodeType().getName() ;
    String srcWorkspace = storedActionNode.getSession().getWorkspace().getName() ;
    //TODO all actions are stored in srcNode/exo:actions
    String srcPath = storedActionNode.getParent().getParent().getPath() ;    
    if (ActionServiceContainer.READ_PHASE.equals(lifecyclePhase)) {
      return;
    }    
    Map<String,Object> variables = new HashMap<String,Object>() ;
    NodeType nodeType = storedActionNode.getPrimaryNodeType() ;
    PropertyDefinition[] defs = nodeType.getPropertyDefinitions() ;
    for(PropertyDefinition propDef:defs) {
      if(!propDef.isMultiple()) {
        String key = propDef.getName() ;
        try{
          Object value = getPropertyValue(storedActionNode.getProperty(key)) ;
          variables.put(key,value) ;
        }catch(Exception e) {
          variables.put(key,null) ;
        }        
      }
    }  
    String actionExecutable = getActionExecutable(actionType); 
    ECMEventListener listener = 
      createEventListener(actionName, actionExecutable, repository, srcWorkspace, srcPath, variables, actionType);
    Session session = getSystemSession(repository, srcWorkspace);
    //TODO all actions are stored at srcNode/exo:actions node
    String listenerKey = repository + ":" + srcPath + "/exo:actions/" +actionName;
    ObservationManager obsManager = session.getWorkspace().getObservationManager(); 
    if(listeners_.containsKey(listenerKey)){
      obsManager.removeEventListener(listeners_.get(listenerKey));
      listeners_.remove(listenerKey) ;      
    }
    if (ActionServiceContainer.ADD_PHASE.equals(lifecyclePhase)) {
      obsManager.addEventListener(listener, Event.NODE_ADDED, srcPath, isDeep, uuid, nodeTypeName, false);
    } else if (ActionServiceContainer.REMOVE_PHASE.equals(lifecyclePhase)) {
      obsManager.addEventListener(listener, Event.NODE_REMOVED, srcPath, isDeep, uuid, nodeTypeName, false);
    } else {
      obsManager.addEventListener(listener, Event.PROPERTY_CHANGED, srcPath, isDeep, uuid, nodeTypeName, false);
    }
    session.logout();
    listeners_.put(listenerKey, listener);
  }

  public void reScheduleActivations(Node storedActionNode, String repository) throws Exception {    
    String jobClassName = storedActionNode.getProperty(JOB_CLASS_PROP).getString() ;
    Class activationJobClass  = null ;
    try {
      activationJobClass = Class.forName(jobClassName) ;
    }catch (Exception e) {
      LOG.error("Unexpected error", e);
      return ;
    }
    String actionName = storedActionNode.getProperty(NODE_NAME_PROP).getString() ;    
    String actionType = storedActionNode.getPrimaryNodeType().getName() ;
    String srcWorkspace = storedActionNode.getSession().getWorkspace().getName() ;
    String scheduleType = storedActionNode.getProperty(SCHEDULE_TYPE_PROP).getString() ;
    String initiator = storedActionNode.getProperty(SCHEDULED_INITIATOR).getString() ;
    //TODO all action node is stored in /exo:actions
    String srcPath = storedActionNode.getParent().getParent().getPath() ;       
    String jobName = storedActionNode.getProperty(JOB_NAME_PROP).getString() ;
    String jobGroup = storedActionNode.getProperty(JOB_GROUP_PROP).getString() ;
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    JobSchedulerService schedulerService = 
      (JobSchedulerService)container.getComponentInstanceOfType(JobSchedulerService.class) ;    
    Map<String,Object> variables = new HashMap<String,Object>() ;
    NodeType nodeType = storedActionNode.getPrimaryNodeType() ;
    PropertyDefinition[] defs = nodeType.getPropertyDefinitions() ;
    for(PropertyDefinition propDef:defs) {
      if(!propDef.isMultiple()) {
        String key = propDef.getName() ;
        try{
          Object value = getPropertyValue(storedActionNode.getProperty(key)) ;
          variables.put(key,value) ;
        }catch(Exception e) {
          variables.put(key,null) ;
        }        
      }
    }    
    String actionExecutable = getActionExecutable(actionType);
    variables.put(initiatorVar,initiator) ;
    variables.put(actionNameVar, actionName);
    variables.put(srcRepository, repository) ;
    variables.put(executableVar,actionExecutable) ;
    //variables.put("nodePath", path);
    variables.put(srcWorkspaceVar, srcWorkspace);
    variables.put(srcPathVar, srcPath);
    JobDataMap jdatamap = new JobDataMap() ;
    JobInfo jinfo = new JobInfo(jobName,jobGroup,activationJobClass) ;
    jdatamap.putAll(variables) ;    
    if(CRON_JOB.equals(scheduleType)) {
      String cronExpression = storedActionNode.getProperty(CRON_EXPRESSION_PROP).getString() ;      
      schedulerService.addCronJob(jinfo,cronExpression,jdatamap) ;
    }else {      
      Calendar endTime = null ;
      Date endDate = null ;
      if(storedActionNode.hasProperty(END_TIME_PROP)) {
        endTime = storedActionNode.getProperty(END_TIME_PROP).getDate() ; 
      }            
      if(endTime != null) endDate = endTime.getTime() ;
      long timeInterval = storedActionNode.getProperty(TIME_INTERVAL_PROP).getLong() ;
      Date startDate = new Date(System.currentTimeMillis()+BUFFER_TIME) ;
      int repeatCount = (int)storedActionNode.getProperty(REPEAT_COUNT_PROP).getLong() ;
      int counter = (int)storedActionNode.getProperty(COUNTER_PROP).getLong() ;
      PeriodInfo pinfo = new PeriodInfo(startDate,endDate,repeatCount-counter,timeInterval) ;
      schedulerService.addPeriodJob(jinfo,pinfo,jdatamap) ;
    }       
  } 

  protected Session getSystemSession(String repository, String workspace) throws Exception {   
    ManageableRepository jcrRepository = getRepository(repository);      
    return  jcrRepository.getSystemSession(workspace);            
  }

  public String getActionExecutable(String actionTypeName) throws Exception {    
    NodeTypeManager ntManager = getRepository(getRepositoryName()).getNodeTypeManager();
    NodeType nt = ntManager.getNodeType(actionTypeName);
    PropertyDefinition[] propDefs = nt.getDeclaredPropertyDefinitions();
    for (int i = 0; i < propDefs.length; i++) {
      PropertyDefinition definition = propDefs[i];
      if (definition.getName().equals(getExecutableDefinitionName()) && 
          definition.getDefaultValues() != null) {
        return definition.getDefaultValues()[0].getString();
      }
    }
    return null;
  }

  public boolean isActionTypeSupported(String actionType) {    
    try {      
      NodeTypeManager ntmanager = getRepository(getRepositoryName()).getNodeTypeManager();      
      for (NodeType type:ntmanager.getNodeType(actionType).getSupertypes()) {        
        if (getActionType().equals(type.getName())) {
          return true; 
        }         
      }      
    } catch (Exception re) {      
    }
    return false ;
  }

  public void removeObservation(String repository, String actionPath) throws Exception {

    ECMEventListener eventListener = listeners_.get(repository + ":" + actionPath);
    if(eventListener != null){
      String srcWorkspace = eventListener.getSrcWorkspace();
      Session session = getSystemSession(repository, srcWorkspace);
      ObservationManager obsManager = session.getWorkspace().getObservationManager();
      obsManager.removeEventListener(eventListener);
      session.logout();
    }
    listeners_.remove(repository + ":" + actionPath);
  }

  public void removeActivationJob(String jobName,String jobGroup,String jobClass) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    JobSchedulerService schedulerService = 
      (JobSchedulerService)container.getComponentInstanceOfType(JobSchedulerService.class) ;
    Class activationJob = null ;
    try {
      activationJob = Class.forName(jobClass) ;
    }catch (Exception e) {
      LOG.error("Unexpected error", e);      
    }    
    if(activationJob == null) return  ;
    JobInfo jinfo = new JobInfo(jobName,jobGroup,activationJob) ; 
    schedulerService.removeJob(jinfo) ;
  }

  public boolean isVariable(String variable) throws Exception {    
    NodeTypeManager ntManager = getRepository(getRepositoryName()).getNodeTypeManager();
    NodeType nt = ntManager.getNodeType(getActionType());
    PropertyDefinition[] propDefs = nt.getDeclaredPropertyDefinitions();
    for (int i = 0; i < propDefs.length; i++) {
      PropertyDefinition definition = propDefs[i];
      if (definition.getName().equals(variable)) {
        return false;
      }
    }
    return true;
  }

  public Collection<String> getVariableNames(String actionTypeName) throws Exception {
    Collection<String> variableNames = new ArrayList<String>();    
    NodeTypeManager ntManager = getRepository(getRepositoryName()).getNodeTypeManager() ;
    NodeType nt = ntManager.getNodeType(actionTypeName);
    PropertyDefinition[] propDefs = nt.getDeclaredPropertyDefinitions();
    for (int i = 0; i < propDefs.length; i++) {
      PropertyDefinition definition = propDefs[i];
      if (isVariable(definition.getName())) {
        variableNames.add(definition.getName());
      }
    }
    return variableNames;
  }

  protected void importPredefinedActionsInJcr() throws Exception {
    List actions = getActions();
    if (actions.isEmpty()) return;
    Session  session = null;
    for (Iterator iter = actions.iterator(); iter.hasNext();) {
      ActionConfig.Action action = (ActionConfig.Action) iter.next();      
      try {
        session = getSystemSession(getRepositoryName(),action.getSrcWorkspace());
        importAction(action, session) ;
        session.logout();
      } catch (Exception e) {
        if(session != null) session.logout();
        System.out.println("[WARNING] ==> Can not init action '" + action.getName() 
            + "' in repository '"+getRepositoryName()+"' and workspace '"+action.getSrcWorkspace()+"'") ;
      }
    }
  }

  protected void reImportPredefinedActionsInJcr(String repository) throws Exception {
    List actions = getActions();
    if (actions.isEmpty()) return;
    Session session = null ;
    for (Iterator iter = actions.iterator(); iter.hasNext();) {
      ActionConfig.Action action = (ActionConfig.Action) iter.next();
      if(repository.equals(getRepositoryName())) {
        try {
          session = getSystemSession(repository, action.getSrcWorkspace());
          importAction(action,session) ;
        } catch (Exception e) {
          if(session != null) session.logout();
          System.out.println("[WARNING] ==> Can not init action '" + action.getName() 
              + "' in repository '"+repository+"' and workspace '"+action.getSrcWorkspace()+"'") ;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void importAction(ActionConfig.Action action, Session session) throws Exception{
    Node srcNode = (Node) session.getItem(action.getSrcPath());
    Node actionNode = null;
    boolean firstImport = false;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActionServiceContainer actionContainer = (ActionServiceContainer) container
        .getComponentInstanceOfType(ActionServiceContainer.class);
    RepositoryService repositoryService = (RepositoryService) container
    .getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository manageRepo = repositoryService.getRepository(getRepositoryName());
    Node actionNodeName = null;
    try {
      actionNodeName = actionContainer.getAction(srcNode, action.getName()) ;
    } catch (Exception e) {}
    
    Node actionsNode = null;
    if (actionNodeName == null) {
      firstImport = true;
      if (!srcNode.isNodeType("exo:actionable")) {
        srcNode.addMixin("exo:actionable");                  
      }
      //TODO now, each node will have actions storage. It's better to store all actions in one storage like version storage
      try {
        actionsNode = srcNode.getNodes(EXO_ACTIONS).nextNode();
      } catch (Exception e) {
        actionsNode = srcNode.addNode(EXO_ACTIONS,ACTION_STORAGE);
        srcNode.save();
      }
      actionNode = actionsNode.addNode(action.getName(), action.getType());
      actionNode.setProperty("exo:name", action.getName());
      actionNode.setProperty("exo:description", action.getDescription());
      actionNode.setProperty("exo:isDeep", action.isDeep());
      if (action.getUuid() != null)
        actionNode.setProperty("exo:uuid", StringUtils.split(action.getUuid(), ";"));
      if (action.getNodeTypeName() != null)
        actionNode.setProperty("exo:nodeTypeName", StringUtils.split(action.getNodeTypeName(), ";"));
      if (action.getLifecyclePhase() != null)
        actionNode.setProperty("exo:lifecyclePhase", action.getLifecyclePhase());
      if (action.getRoles() != null) {
        String[] roles = StringUtils.split(action.getRoles(), ";");
        actionNode.setProperty("exo:roles", roles);
      }
      Iterator mixins = action.getMixins().iterator();
      NodeType nodeType;
      String value;
      while (mixins.hasNext()) {
        ActionConfig.Mixin mixin = (ActionConfig.Mixin) mixins.next();
        actionNode.addMixin(mixin.getName());
        Map<String, String> props = mixin.getParsedProperties();
        Set keys = props.keySet();
        nodeType = manageRepo.getNodeTypeManager().getNodeType(mixin.getName());
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
          String key = (String) iterator.next();
          if (((ExtendedNodeType) nodeType).getPropertyDefinitions(key).getAnyDefinition()
              .isMultiple()) {
            value = props.get(key);
            if (value != null)
              actionNode.setProperty(key, value.split(","));
          } else
            actionNode.setProperty(key, props.get(key));
        }
      }
    } else {
      actionNodeName = actionContainer.getAction(srcNode, action.getName());
    }

    String unparsedVariables = action.getVariables();
    Map variablesMap = new HashMap();
    if (unparsedVariables != null && !"".equals(unparsedVariables)) {
      String[] variables = StringUtils.split(unparsedVariables, ";");
      for (int i = 0; i < variables.length; i++) {
        String variable = variables[i];
        String[] keyValue = StringUtils.split(variable, "=");
        String variableKey = keyValue[0];
        String variableValue = keyValue[1];
        variablesMap.put(variableKey, variableValue);
        if (firstImport)
          actionNode.setProperty(variableKey, variableValue);
      }
    }
    if (firstImport)
      srcNode.save();
  }
  
  private void scheduleActionActivationJob(String repository, String srcWorkspace,String srcPath,
      String actionName,String actionType,String actionExecutable, Map mappings) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    JobSchedulerService schedulerService =
      (JobSchedulerService)container.getComponentInstanceOfType(JobSchedulerService.class) ;
    ActionServiceContainer actionContainer = 
      (ActionServiceContainer) container.getComponentInstanceOfType(ActionServiceContainer.class) ;

    Session session = getSystemSession(repository, srcWorkspace) ;
    Node srcNode = (Node)session.getItem(srcPath) ;
    Node actionNode = actionContainer.getAction(srcNode,actionName) ;
    if(!actionNode.isNodeType(SCHEDULABLE_INFO_MIXIN)) {
      actionNode.addMixin(SCHEDULABLE_INFO_MIXIN) ;
      actionNode.save() ;        
    }
    Class activationJob = createActivationJob() ;
    String jobName = JOB_NAME_PREFIX.concat(actionName) ;
    String jobGroup = actionType ;
    String userId = session.getUserID() ;
    String scheduleType = null, repeatCount = null, timeInterval = null, cronExpress = null ;
    GregorianCalendar startTime = new GregorianCalendar() ;
    GregorianCalendar endTime = null ;
    if(mappings.containsKey("/node/exo:scheduleType")) {
      scheduleType = (String) ((JcrInputProperty) mappings.get("/node/exo:scheduleType")).getValue();
      mappings.remove("/node/exo:scheduleType") ;
    }
    if(mappings.containsKey("/node/exo:startTime")) {
      startTime = (GregorianCalendar) ((JcrInputProperty) mappings.get("/node/exo:startTime")).getValue();
      mappings.remove("/node/exo:startTime") ;
    }     
    if(mappings.containsKey("/node/exo:endTime")) {
      endTime = (GregorianCalendar) ((JcrInputProperty) mappings.get("/node/exo:endTime")).getValue();
      mappings.remove("/node/exo:endTime") ;
    }   
    if(mappings.containsKey("/node/exo:repeatCount")) {
      repeatCount = (String) ((JcrInputProperty) mappings.get("/node/exo:repeatCount")).getValue();
      mappings.remove("/node/exo:repeatCount") ;
    }
    if(mappings.containsKey("/node/exo:timeInterval")) {
      timeInterval = (String) ((JcrInputProperty) mappings.get("/node/exo:timeInterval")).getValue();
      mappings.remove("/node/exo:timeInterval") ;
    } 
    if(mappings.containsKey("/node/exo:cronExpression")) {
      cronExpress = (String) ((JcrInputProperty) mappings.get("/node/exo:cronExpression")).getValue();                   
      mappings.remove("/node/exo:cronExpression") ; 
    }    
    actionNode.setProperty(JOB_NAME_PROP,jobName) ;
    actionNode.setProperty(JOB_GROUP_PROP,jobGroup) ;

    actionNode.setProperty(JOB_CLASS_PROP,activationJob.getName()) ;
    actionNode.setProperty(SCHEDULED_INITIATOR,userId) ;
    actionNode.setProperty(SCHEDULE_TYPE_PROP,scheduleType) ;
    actionNode.save() ;
    Map<String,Object> variables = new HashMap<String,Object>(); 
    variables.put(initiatorVar, userId);
    variables.put(actionNameVar, actionName);
    variables.put(executableVar,actionExecutable) ;
    variables.put(srcWorkspaceVar, srcWorkspace);
    variables.put(srcRepository, repository);
    variables.put(srcPathVar, srcPath);
    Map<String,Object> executionVariables = getExecutionVariables(mappings) ; 
    JobDataMap jdatamap = new JobDataMap() ;
    jdatamap.putAll(variables) ; 
    jdatamap.putAll(executionVariables) ;
    JobInfo jinfo = new JobInfo(jobName,jobGroup,activationJob) ;
    if(scheduleType.equals(CRON_JOB)) {    
      actionNode.setProperty(CRON_EXPRESSION_PROP,cronExpress) ;
      actionNode.save() ;        
      schedulerService.addCronJob(jinfo,cronExpress,jdatamap) ;
    } else {      
      int repeatNum = Integer.parseInt(repeatCount) ;
      long period = Long.parseLong(timeInterval) ;      
      actionNode.setProperty(START_TIME_PROP, startTime) ;
      if(endTime != null ) {
        actionNode.setProperty(END_TIME_PROP, endTime) ;        
      }
      actionNode.setProperty(TIME_INTERVAL_PROP,period) ;
      actionNode.setProperty(REPEAT_COUNT_PROP,repeatNum) ;
      actionNode.save() ;     
      PeriodInfo pinfo ;
      if(endTime != null) {
        pinfo = new PeriodInfo(startTime.getTime(),endTime.getTime(),repeatNum,period) ;
      } else {
        pinfo = new PeriodInfo(repeatNum,period) ;
      }
      schedulerService.addPeriodJob(jinfo,pinfo,jdatamap) ;
    }                    
    session.save() ;
    session.logout();
  }
  private Map<String,Object> getExecutionVariables(Map mappings) {
    Map<String,Object> variables = new HashMap<String,Object>();
    Set keys = mappings.keySet();
    for (Iterator iter = keys.iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      Object value = ((JcrInputProperty) mappings.get(key)).getValue();
      key = key.substring(key.lastIndexOf("/") + 1);
      variables.put(key, value);      
    }
    return variables ;
  }  
  
  private Object getPropertyValue(Property property) throws Exception {
    int propertyType = property.getType() ;
    switch(propertyType) {
    case PropertyType.STRING : return property.getValue().getString() ;
    case PropertyType.BOOLEAN : return property.getValue().getBoolean() ;
    case PropertyType.DATE : return property.getValue().getDate() ;
    case PropertyType.DOUBLE : return property.getValue().getDouble() ;
    case PropertyType.LONG : return property.getValue().getLong() ;
    case PropertyType.NAME : return property.getValue().getString() ;
    case PropertyType.UNDEFINED : return property.getValue() ;
    }
    return null ;
  }
  
  private String[] parseValuesToArray(Value[] values) throws Exception {
    String[] valueToString = new String[values.length];
    int i = 0;
    for(Value value : values) {
      valueToString[i++] = value.toString();
    }
    return valueToString;
  }
}

