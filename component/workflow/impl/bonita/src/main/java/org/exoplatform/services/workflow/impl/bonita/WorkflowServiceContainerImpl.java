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
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.BnNodeLocalHome;
import hero.interfaces.BnNodePK;
import hero.interfaces.BnNodePropertyLocal;
import hero.interfaces.BnNodeUtil;
import hero.interfaces.BnNodeValue;
import hero.interfaces.BnProjectLightValue;
import hero.interfaces.BnProjectLocal;
import hero.interfaces.BnProjectLocalHome;
import hero.interfaces.BnProjectPK;
import hero.interfaces.BnProjectPropertyLocal;
import hero.interfaces.BnProjectPropertyValue;
import hero.interfaces.BnProjectUtil;
import hero.interfaces.BnProjectValue;
import hero.interfaces.Constants;
import hero.interfaces.DeadlineEjbTimerSessionLocal;
import hero.interfaces.DeadlineEjbTimerSessionLocalHome;
import hero.interfaces.DeadlineEjbTimerSessionUtil;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.interfaces.UserSessionLocal;
import hero.interfaces.UserSessionLocalHome;
import hero.interfaces.UserSessionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginContext;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;
import org.exoplatform.services.workflow.FileDefinition;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.PredefinedProcessesPlugin;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.ProcessInstance;
import org.exoplatform.services.workflow.ProcessesConfig;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.Timer;
import org.exoplatform.services.workflow.WorkflowFileDefinitionService;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.picocontainer.Startable;

/**
 * Bonita implementation of the Workflow Service in eXo Platform
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Dec 25, 2005
 */
public class WorkflowServiceContainerImpl implements WorkflowServiceContainer,
                                                     Startable {

  /**
   * Holds variables to be set while instantiating a Process. Indeed Bonita
   * currently executes some mappers and hooks prior having the opportunity to
   * set variables in the instance. By accessing this Thread Local, mappers and
   * hooks can retrieve them.
   */ 
  public static ThreadLocal<Map<String,Object>> InitialVariables =
    new ThreadLocal<Map<String,Object>>();
  
  /** Configuration of the Service */
  private ArrayList<ProcessesConfig> configurations;
  
  /** Reference to the Configuration Manager Service */
  private ConfigurationManager configurationManager = null;
  
  /** Reference to the File Definition Service */
  private WorkflowFileDefinitionService fileDefinitionService = null;

  /** Reference to the Workflow Forms Service */
  private WorkflowFormsService formsService = null;
  
  /** Reference to the Organization Service */
  private OrganizationService organizationService = null;
  
  /** Reference to the Portal Container */
  private PortalContainer portalContainer = null;
  
  private String superUser_ = "root";
  private String jaasLoginContext_ = "tomcat";

  /**
   * Instantiates a new Bonita service instance.
   * This constructor requires the injection of the Forms Service.
   * 
   * @param fileDefinitionService reference to the File Definition Service
   * @param formsService          reference to the Forms Service
   * @param organizationService   reference to the Organization Service
   * @param configuration         reference to the Configuration Manager
   * @param params                initialization parameters of the service
   */
  public WorkflowServiceContainerImpl(
      WorkflowFileDefinitionService fileDefinitionService,
      WorkflowFormsService          formsService,
      OrganizationService           organizationService,
      ConfigurationManager          configurationManager,
      PortalContainer               portalContainer,
      InitParams                    params) {
    
    // Store references to dependent services
    this.fileDefinitionService = fileDefinitionService;
    this.formsService          = formsService;
    this.organizationService   = organizationService;
    this.configurationManager  = configurationManager;
    this.portalContainer       = portalContainer;
    
    // Initialize some fields
    this.configurations        = new ArrayList<ProcessesConfig>();
    if(params != null) {
      ValueParam superUserParam = params.getValueParam("super.user") ;
      if(superUserParam != null && superUserParam.getValue().length()>0) {
        this.superUser_ = superUserParam.getValue();
      }
      ValueParam jaasLoginContextParam = params.getValueParam("jaas.login.context") ;
      if(jaasLoginContextParam !=null && jaasLoginContextParam.getValue().length()>0) {
        this.jaasLoginContext_ = jaasLoginContextParam.getValue();
      } 
    }
  }
  
  /**
   * Add a plugin to the Workflow service.
   * This method currently only supports plugins to deploy predefined processes.
   * 
   * @param plugin the plugin to add
   * @throws Exception if the plugin type is unknown.
   */
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    if(plugin instanceof PredefinedProcessesPlugin) {
      this.configurations.add(((PredefinedProcessesPlugin) plugin).
        getProcessesConfig());
    }
    else {
      throw new RuntimeException(
        plugin.getClass().getName()
        + " is an unknown plugin type.") ;
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#deleteProcess(java.lang.String)
   */
  public void deleteProcess(String processId) {
    UserSessionLocal userSession = null;
    
    try {
      // Retrieve textual information
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project         = projectHome.findByPrimaryKey(
        new BnProjectPK(processId));
      String processName             = project.getName();
      
      // Initialize User Session
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      
      // Terminate all Project instances
      Collection<BnProjectValue> processInstances =
        userSession.getProjectInstances(processName);
      for(BnProjectValue projectValue : processInstances) {
        userSession.removeProject(projectValue.getName());
      }
      
      // Remove the Model
      userSession.removeProject(processName);
      
      // Remove the Process definition
      this.formsService.removeForms(processId);
      this.fileDefinitionService.remove(processId);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#deleteProcessInstance(java.lang.String)
   */
  public void deleteProcessInstance(String processInstanceId) {
    UserSessionLocal userSession = null;
    
    try {
      // Retrieve textual information
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project         = projectHome.findByPrimaryKey(
        new BnProjectPK(processInstanceId));
      String processInstanceName     = project.getName();
      
      // Initialize User Session
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      
      // Remove the Project. Currently only a Project admin is allowed to do so.
      userSession.removeProject(processInstanceName);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#deployProcess(java.io.InputStream)
   */
  public void deployProcess(InputStream jarInputStream) throws IOException {
    // TODO Replace the IOException by an Exception in the eXo interface
    // That way it will be possible to catch any kind of exception, not only IO
    FileDefinition fileDefinition = new XPDLFileDefinition(jarInputStream);
    
    try {
      // Deploy the Process
      fileDefinition.deploy();
      
      // Get the id of the deployed process
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByName(
        fileDefinition.getProcessModelName());
      String processId = project.getId();
      
      // Store the File Definition so that it can be retrieved if eXo restarts
      this.fileDefinitionService.store(fileDefinition, processId);
    }
    catch(Exception e) {
      // TODO Remove that and let an Exception bubble up as suggested above
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#endTask(java.lang.String, java.util.Map)
   */
  public void endTask(String taskId, Map variables) {
    UserSessionLocal userSession = null;
    
    try {
      // Retrieve textual information
      BnNodeLocalHome nodeHome = BnNodeUtil.getLocalHome();
      BnNodeLocal node = nodeHome.findByPrimaryKey(new BnNodePK(taskId));
      String projectName = node.getBnProject().getName();
      String processInstanceId = node.getBnProject().getId();
      String nodeName = node.getName();
      
      // Initialize User Session
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      
      // Start the activity
      userSession.startActivity(projectName, nodeName);
      
      // Set variables
      this.setVariables(processInstanceId, taskId, variables);
      
      // Terminate the Activity
      userSession.terminateActivity(projectName, nodeName);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#endTask(java.lang.String, java.util.Map, java.lang.String)
   */
  public void endTask(String taskId, Map variables, String transition) {
    /*
     * In Bonita, we consider this is the Workflow duty to determine which
     * activity comes next hence transition name is unused.
     */
    endTask(taskId, variables);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getAllTasks(java.lang.String)
   */
  public List<Task> getAllTasks(String user) throws Exception {
    List allTasks = new ArrayList();
    allTasks.addAll(getUserTaskList(user));
    allTasks.addAll(getGroupTaskList(user));
    return allTasks;
  }
  
  public WorkflowFileDefinitionService getFileDefinitionService() {
    return this.fileDefinitionService ;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getGroupTaskList(java.lang.String)
   */
  public List<Task> getGroupTaskList(String user) throws Exception {
    // TODO Determine if something can be implemented
    return new ArrayList<Task>();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getProcess(java.lang.String)
   */
  public Process getProcess(String processId) {
    Process process = null;
    
    try {
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processId));
      process = new ProcessData(project.getBnProjectLightValue());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
    return process;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getProcesses()
   */
  public List<Process> getProcesses() {
    UserSessionLocal userSession = null;
    List<Process> processes = new ArrayList<Process>();
    
    try {
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      /*
       * By calling this method, we voluntarily return the
       * models which can be instantiated by the user.
       */
      Collection<BnProjectLightValue> projects = userSession.getModels();
        
      for(BnProjectLightValue project : projects) {
        processes.add(new ProcessData(project));
      }
    } 
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
    
    return processes;
  } 

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getProcessInstance(java.lang.String)
   */
  public ProcessInstance getProcessInstance(String processInstanceId) {
    ProcessInstance processInstance = null;
    
    try {
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processInstanceId));
      processInstance = new ProcessInstanceData(project.getBnProjectValue());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
    return processInstance;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getProcessInstances(java.lang.String)
   */
  public List<ProcessInstance> getProcessInstances(String processId) {
    // TODO Also take into account terminated processes by parsing the XML
    UserSessionLocal userSession = null;
    List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
    
    try {
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processId));
      String processName = project.getName();
      
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      Collection<BnProjectValue> instances = userSession.getProjectInstances(
        processName);
        
      for(BnProjectValue instance : instances) {
        processInstances.add(new ProcessInstanceData(instance));
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
    
    return processInstances;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getTask(java.lang.String)
   */
  public Task getTask(String taskId) {
    Task task = null;
    
    try {
      BnNodeLocalHome nodeHome = BnNodeUtil.getLocalHome();
      BnNodeLocal node = nodeHome.findByPrimaryKey(new BnNodePK(taskId));
      task = new TaskData(node.getBnNodeValue());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
    return task;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getTasks(java.lang.String)
   */
  public List<Task> getTasks(String processInstanceId) {
    List<Task> tasks = new ArrayList<Task>();
    
    try {
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processInstanceId));
      Collection<BnNodeLocal> nodes = project.getBnNodes();
      
      for(BnNodeLocal node : nodes) {
        // Filters out nodes which have not been accessed yet
        if(node.getState() != Constants.Nd.INITIAL) {
          tasks.add(new TaskData(node.getBnNodeValue()));
        }
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    return tasks;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getTimers()
   */
  public List<Timer> getTimers() {
    DeadlineEjbTimerSessionLocal deadlineSession = null;
    List<Timer> timers = new ArrayList<Timer>();

    try {
      DeadlineEjbTimerSessionLocalHome deadlineHome =
        DeadlineEjbTimerSessionUtil.getLocalHome();
      deadlineSession = deadlineHome.create();
      Collection<hero.util.TimerData> deadlines = deadlineSession.getTimers();
      for(hero.util.TimerData timerData : deadlines) {
        timers.add(new TimerData(timerData));
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        deadlineSession.remove();
      }
      catch(Exception ignore) {
      }
    }
    
    return timers;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getUserTaskList(java.lang.String)
   */
  public List<Task> getUserTaskList(String user) {
    UserSessionLocal userSession = null;
    List<Task> tasks = new ArrayList<Task>();
    
    try
    {
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      Collection<BnNodeValue> nodes = userSession.getToDoListAllInstances();
        
      for(BnNodeValue node : nodes) {
        tasks.add(new TaskData(node));
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
    
    return tasks;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#getVariables(java.lang.String)
   */
  public Map getVariables(String processInstanceId, String taskId) {
    Map<Object, Object> variables = new HashMap<Object, Object>();

    /*
     * ProjectSessionBean is not used here as it is faster to proceed directly
     * with Entity Beans when ids are available. Indeed, if ProjectSessionBean
     * was used, it would have been needed to retrieve names from ids first.
     */
    try {
      // Retrieve Node properties
      BnNodeLocalHome nodeHome = BnNodeUtil.getLocalHome();
      BnNodeLocal node = nodeHome.findByPrimaryKey(new BnNodePK(taskId));
      Collection<BnNodePropertyLocal> nodeProperties =
        node.getBnProperties();
      for(BnNodePropertyLocal property : nodeProperties) {
        variables.put(property.getTheKey(),
                      property.getTheValue());
      }
      
      // Retrieve Project properties last, which means they have a high priority
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processInstanceId));
      Collection<BnProjectPropertyLocal> projectProperties =
        project.getBnProperties();
      for(BnProjectPropertyLocal property : projectProperties) {
        variables.put(property.getTheKey(),
                      property.getTheValue());
      }
      
      // Retrieve the Process Model id from the Process Instance id
      String processModelName = WorkflowServiceContainerHelper.
        getModelName(project.getName());
      String processModelId = projectHome.findByName(processModelName).getId();
      
      // Retrieve the Form corresponding to the specified Task and its variables
      Form form = this.formsService.getForm(processModelId,
                                            node.getName(),
                                            Locale.getDefault());
      List<Map<String, Object>> formVariables = form.getVariables();
      
      // Convert String to Objects based on Form information
      for(Map<String, Object> formVariable : formVariables) {
        String key       = (String) formVariable.get("name");
        String component = (String) formVariable.get("component");
        variables.put(key, WorkflowServiceContainerHelper.stringToObject(
          (String) variables.get(key),
          component));
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
    return variables;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#hasStartTask(java.lang.String)
   */
  public boolean hasStartTask(String processId) {
    // Retrieve the Start Form and determine if it contains variables
    Form form = this.formsService.getForm(processId,
                                          ProcessData.START_STATE_NAME,
                                          Locale.getDefault());
    
    return ! form.getVariables().isEmpty();
  }
  
  /**
   * Sets attributes of a Process instance.
   * Bonita provides Process instance and Activity attributes. This method sets
   * both types of attributes. The Process instance attributes have a higher
   * priority in case of name collision.
   * 
   * @param processInstanceId identifies the Process instance in which
   *                          in which attributes should be set
   * @param taskId            identifies the current Activity in which
   *                          attributes should be set. A <tt>null</tt> value
   *                          is allowed in case the process should be started
   * @param variables         contains the variables to be set
   */
  private void setVariables(String processInstanceId,
                            String taskId,
                            Map variables) {
    
    ProjectSessionLocal projectSession = null;
    
    // TODO As we already have retrieved the Project and Node entity beans,
    // try to see if it is possible to directly deal with them when setting
    // properties instead of creating a Project Session Bean. That way it
    // should increase performance a bit
    try {
      // Retrieve Project name and Process Model identifier
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project         = projectHome.findByPrimaryKey(
                                         new BnProjectPK(processInstanceId));
      String processInstanceName     = project.getName();
      String processModelName        = WorkflowServiceContainerHelper.
                                         getModelName(processInstanceName);
      project                        = projectHome.findByName(processModelName);
      String processModelId          = project.getId();

      // Initialize Project Session
      ProjectSessionLocalHome projectSessionHome =
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      projectSession.initProject(processInstanceName);
      
      // Retrieve Task name
      String taskName = null;
      if(taskId != null) {
        // An Activity is being processed
        BnNodeLocalHome nodeHome = BnNodeUtil.getLocalHome();
        BnNodeLocal node = nodeHome.findByPrimaryKey(new BnNodePK(taskId));
        taskName = node.getName();
      }
      else {
        // The Process is being started
        taskName = ProcessData.START_STATE_NAME;
      }
            
      // Retrieve Form variables and put them in a HashMap to access them easily
      Form form = this.formsService.getForm(processModelId,
                                            taskName,
                                            Locale.getDefault());
      List<Map<String, Object>> formVariableList = form.getVariables();
      HashMap<String, Map<String, Object>> formVariables =
        new HashMap<String, Map<String, Object>>();
      for(Map<String, Object> formVariable : formVariableList) {
        formVariables.put((String) formVariable.get("name"), formVariable);
      }
      
      // Retrieve Project properties keys
      Collection<BnProjectPropertyValue> projectPropertyValues =
        projectSession.getProperties();
      Collection<String> projectPropertyKeys = new ArrayList<String>();
      for(BnProjectPropertyValue projectPropertyValue : projectPropertyValues) {
        projectPropertyKeys.add(projectPropertyValue.getTheKey());
      }

      // Process each variable to set
      for(Object keyObject : variables.keySet()) {
        // Retrieve the variable from the Form. It may not exist.
        String key = (String) keyObject;
        Map<String, Object> formVariable = formVariables.get(key);
        
        // Determine the Component name
        String componentName = null;
        if(formVariable != null ) {
          // The variable exists in the Form
          componentName = (String) formVariable.get("component");
        }
        if(componentName == null) {
          /*
           * The variable does not exist in the Form or the component is unset.
           * The default in that case is "text".
           */
          componentName = "text";
        }
        
        // The value can be any Object. Convert it to a String
        String value = WorkflowServiceContainerHelper.objectToString(
          variables.get(keyObject),
          componentName);
        
        if(projectPropertyKeys.contains(key) ||
           ProcessData.START_STATE_NAME.equals(taskName)) {
          // The variable corresponds to a Project property
          projectSession.setProperty(key, value);
        } else {
          // The variable corresponds to a Node property
          projectSession.setNodeProperty(taskName, key, value);
        }
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        projectSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }
  
  /*
   * Deploy the predefined processes. This is done in the <tt>start()</tt>
   * method and not the <tt>initComponent()</tt> method as the predefined users
   * are not created yet in that case, which makes the login fail. We expect the
   * <tt>start()</tt> method of the Organization service to be called prior the
   * <tt>start()</tt> method of the Workflow service because of the
   * dependencies.
   *
   * (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    LoginContext lc = null;

    try {
      // Retrieve the already deployed Processes. No need to be logged in yet.
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      Collection<BnProjectLocal> projects = projectHome.findAll();

      // If the predefined Processes need to be deployed
      if(projects.isEmpty()) {                
        
        /*
         * Retrieve the corresponding password from the Organization Service.
         * Currently the password needs the "@portalname" suffix so that the
         * target portal is specified to the Login Module. 
         */
        UserHandler userHandler = this.organizationService.getUserHandler();
        User user = userHandler.findUserByName(superUser_);
        char[] password = user.getPassword().toCharArray();          
        /*
         * As the Application Server is being started, there is no logged in
         * user. It is therefore required to login programmatically.
         */ 
        BasicCallbackHandler handler = new BasicCallbackHandler(superUser_, password);
        lc = new LoginContext(jaasLoginContext_, handler);
        lc.login();
      
        // Deploy each predefined Process
        for(ProcessesConfig processConfig : configurations) {
          HashSet predefinedProcesses = processConfig.getPredefinedProcess();
          String processLoc           = processConfig.getProcessLocation();          
          for (Iterator iter = predefinedProcesses.iterator(); iter.hasNext();) {
            String parFile = (String) iter.next();
            InputStream iS;
            try {
              iS = this.configurationManager.getInputStream(processLoc + parFile);
              this.deployProcess(iS);
            }
            catch (Exception e) {
              // Process does not exist
              e.printStackTrace();
            }
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        /*
         * Logout. This does not hurt if it fails as Exceptions are ignored.
         */ 
        lc.logout();
      }
      catch(Exception ignore) {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#startProcess(java.lang.String)
   */
  public void startProcess(String processId) {
    
    // Delegate the call
    this.startProcess(null, processId, new HashMap());
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#startProcess(java.lang.String, java.lang.String, java.util.Map)
   */
  public void startProcess(String remoteUser, String processId, Map variables) {
    ProjectSessionLocal projectSession = null;
    
    try {
      ProjectSessionLocalHome projectSessionHome =
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      
      // Retrieve the process name from the id as ProjectSessionBean.
      // instantiateProject() only allows a name as parameter.
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByPrimaryKey(
        new BnProjectPK(processId));
      String modelName = project.getName();

      // The first activity should be automatic so that it can be started
      String instanceName = projectSession.instantiateProject(modelName);

      // Retrieve the Instance identifier from the Process Instance name
      project = projectHome.findByName(instanceName);
      String instanceId = project.getId();
      
      // Set the initial variables in Project attributes
      this.setVariables(instanceId, null, variables);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        projectSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.WorkflowServiceContainer#startProcessFromName(java.lang.String, java.lang.String, java.util.Map)
   */
  public void startProcessFromName(String remoteUser,
                                   String processName,
                                   Map    variables) {
    try {
      // Retrieve the Model by name and get its identifier
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByName(processName);
      String processId = project.getId();
      
      /*
       * Store initial variables in the Thread Local to enable initial mappers
       * and hooks to retrieve them
       */
      WorkflowServiceContainerImpl.InitialVariables.set(variables);
      
      // Delegate the call
      this.startProcess(remoteUser, processId, variables);
      
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        // Free up the Thread Local
        WorkflowServiceContainerImpl.InitialVariables.remove();
      } 
      catch(Exception ignore) {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
  }
}
