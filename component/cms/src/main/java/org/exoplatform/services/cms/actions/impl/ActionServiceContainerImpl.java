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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.actions.ActionPlugin;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.picocontainer.Startable;

public class ActionServiceContainerImpl implements ActionServiceContainer, Startable {

  private static final String         ACTIONABLE           = "exo:actionable".intern();
  private static final String         ACTION               = "exo:action".intern();
  private static final String         JOB_NAME_PROP        = "exo:jobName".intern();
  private static final String         JOB_GROUP_PROP       = "exo:jobGroup".intern();
  private static final String         JOB_CLASS_PROP       = "exo:jobClass".intern();
  private static final String         LIFECYCLE_PHASE_PROP = "exo:lifecyclePhase".intern() ;
  private static final String         ACTION_QUERY         = "//element(*, exo:action)".intern() ;
  private static final String         SCHEDULABLE_MIXIN    = "exo:schedulableInfo".intern();
  private static final String         EXO_ACTIONS          = "exo:actions".intern();
  private static final String         ACTION_STORAGE       = "exo:actionStorage".intern();

  private RepositoryService           repositoryService_;  
  private CmsService                  cmsService_;
  private Collection<ComponentPlugin> actionPlugins        = new ArrayList<ComponentPlugin>();

  public ActionServiceContainerImpl(InitParams params, RepositoryService repositoryService,
      CmsService cmsService) throws Exception {
    repositoryService_ = repositoryService;
    cmsService_ = cmsService;    
  }

  public void start() {
    try {
      for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
        BaseActionPlugin plugin = (BaseActionPlugin) iter.next();
        plugin.importPredefinedActionsInJcr();
      }
      initiateActionConfiguration();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void stop() {
  }

  public void init(String repository) {
    try {
      for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
        BaseActionPlugin plugin = (BaseActionPlugin) iter.next();
        plugin.reImportPredefinedActionsInJcr(repository);
      }
      reInitiateActionConfiguration(repository);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public Collection<String> getActionPluginNames() {
    Collection<String> actionPluginNames = new ArrayList<String>();
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      actionPluginNames.add(plugin.getName());
    }
    return actionPluginNames;
  }

  public ActionPlugin getActionPlugin(String actionsServiceName) {
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      if (plugin.getName().equals(actionsServiceName))
        return (ActionPlugin) plugin;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void createActionType(String actionTypeName, String parentActionTypeName, String executable, 
      List<String> variableNames, boolean isMoveType, String repository) throws Exception {
    NodeTypeValue nodeTypeValue = new NodeTypeValue();
    nodeTypeValue.setName(actionTypeName);

    List<String> superTypes = new ArrayList<String>();
    superTypes.add(parentActionTypeName);
    if (isMoveType)
      superTypes.add("exo:move");
    nodeTypeValue.setDeclaredSupertypeNames(superTypes);

    List propDefs = new ArrayList();
    PropertyDefinitionValue propDef = null;
    for (Iterator iter = variableNames.iterator(); iter.hasNext();) {
      String variableName = (String) iter.next();
      propDef = createPropertyDef(variableName);
      propDefs.add(propDef);
    }
    propDef = createPropertyDef(getActionPluginForActionType(parentActionTypeName).getExecutableDefinitionName());
    List defaultValues = new ArrayList();
    defaultValues.add(executable);
    propDef.setDefaultValueStrings(defaultValues);
    propDef.setMandatory(false);
    propDefs.add(propDef);

    nodeTypeValue.setDeclaredPropertyDefinitionValues(propDefs);
    nodeTypeValue.setDeclaredChildNodeDefinitionValues(new ArrayList());
    ExtendedNodeTypeManager ntmanager = repositoryService_.getRepository(repository).getNodeTypeManager();
    ntmanager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
  }

  @SuppressWarnings("unchecked")
  private PropertyDefinitionValue createPropertyDef(String name) {
    PropertyDefinitionValue def = new PropertyDefinitionValue();
    def.setName(name);
    def.setRequiredType(PropertyType.STRING);
    def.setMandatory(false);
    def.setMultiple(false);
    def.setReadOnly(false);
    def.setAutoCreate(false);
    def.setOnVersion(OnParentVersionAction.COPY);
    def.setValueConstraints(new ArrayList());
    def.setDefaultValueStrings(new ArrayList());
    return def;
  }

  public Collection<NodeType> getCreatedActionTypes(String repository) throws Exception {
    Collection<NodeType> createsActions = new ArrayList<NodeType>();    
    NodeTypeManager ntmanager = repositoryService_.getRepository(repository).getNodeTypeManager();
    for(NodeTypeIterator iter = ntmanager.getAllNodeTypes();iter.hasNext();) {
      NodeType nt = (NodeType) iter.next();
      String name = nt.getName();
      if (nt.isNodeType(ACTION) && !isAbstractType(name)) {
        createsActions.add(nt);
      }
    }        
    return createsActions;
  }

  private boolean isAbstractType(String name) {
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      if (plugin.getName().equals(name))
        return true;
    }
    return false;
  }
  
  private Session getSystemSession(String repository, String workspace) throws RepositoryException,
  RepositoryConfigurationException {
    ManageableRepository jcrRepository = repositoryService_.getRepository(repository);
    return jcrRepository.getSystemSession(workspace);
  }

  public ActionPlugin getActionPluginForActionType(String actionTypeName) {
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      String actionServiceName = plugin.getName();
      ActionPlugin actionService = getActionPlugin(actionServiceName);
      if (actionService.isActionTypeSupported(actionTypeName)
          || actionServiceName.equals(actionTypeName))
        return actionService;
    }
    return null;
  }

  public Node getAction(Node node, String actionName) throws Exception {
    return node.getNode( EXO_ACTIONS + "/"+actionName);
  }

  public boolean hasActions(Node node) throws Exception {
    return node.isNodeType(ACTIONABLE);
  }

  public List<Node> getActions(Node node) throws Exception {
    return getActions(node, null);
  }

  public List<Node> getCustomActionsNode(Node node, String lifecyclePhase) throws Exception {
    try {
      return getActions(node.getParent(), lifecyclePhase) ;
    } catch(Exception item) {
      return null ;
    }
  }
  
  public List<Node> getActions(Node node, String lifecyclePhase) throws Exception {
    List<Node> actions = new ArrayList<Node>();
    Node actionStorage = null;
    try{
      actionStorage = node.getNode(EXO_ACTIONS) ;
    }catch (Exception e) {
      return actions;
    }        
    for (NodeIterator iter = actionStorage.getNodes(); iter.hasNext();) {
      Node tmpNode = iter.nextNode();
      if (tmpNode.isNodeType(ACTION)
          && (lifecyclePhase == null || lifecyclePhase.equals(tmpNode.getProperty(
              LIFECYCLE_PHASE_PROP).getString()))) {
        actions.add(tmpNode);
      }
    }
    return actions;
  }

  public void removeAction(Node node, String actionName, String repository) throws Exception {
    if(!node.isNodeType(ACTIONABLE)) return  ;    
    Node action2Remove = node.getNode(EXO_ACTIONS+ "/" + actionName);
    String lifecyclePhase = action2Remove.getProperty(LIFECYCLE_PHASE_PROP).getString();
    String jobName = null, jobGroup = null, jobClassName = null;
    if (action2Remove.isNodeType(SCHEDULABLE_MIXIN)) {
      jobName = action2Remove.getProperty(JOB_NAME_PROP).getString();
      jobGroup = action2Remove.getProperty(JOB_GROUP_PROP).getString();
      jobClassName = action2Remove.getProperty(JOB_CLASS_PROP).getString();
    }
    String actionTypeName = action2Remove.getPrimaryNodeType().getName();
    String actionPath = action2Remove.getPath();
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      String actionServiceName = plugin.getName();
      ActionPlugin actionService = getActionPlugin(actionServiceName);
      if (actionService.isActionTypeSupported(actionTypeName)) {
        if (lifecyclePhase.equals(ActionServiceContainer.SCHEDULE_PHASE)) {
          actionService.removeActivationJob(jobName, jobGroup, jobClassName);
        } else {
          actionService.removeObservation(repository, actionPath);
        }
      }
    }
    action2Remove.remove();
    node.save();    
  }

  public void addAction(Node storeActionNode, String repository, String actionType, Map mappings) throws Exception {
    if (!storeActionNode.isNodeType(ACTIONABLE)) {
      storeActionNode.addMixin(ACTIONABLE);
      storeActionNode.save();
    }
    Node actionsNode = null;
    if(storeActionNode.hasNode(EXO_ACTIONS)) {
      actionsNode = storeActionNode.getNode(EXO_ACTIONS) ;
    }else {
      actionsNode = storeActionNode.addNode(EXO_ACTIONS,ACTION_STORAGE) ;
      storeActionNode.save();
    }
    String newActionPath = cmsService_.storeNode(actionType, actionsNode, mappings,true,repository);
    storeActionNode.save();
    String srcWorkspace = storeActionNode.getSession().getWorkspace().getName();

    String srcPath = storeActionNode.getPath();         
    ActionPlugin actionService = getActionPluginForActionType(actionType);
    if (actionService == null) 
      throw new ClassNotFoundException("Not found any action's service compatible with action type "+actionType) ;      
    try {
      actionService.addAction(actionType, repository, srcWorkspace, srcPath, mappings);
    } catch (Exception e) {
      Session session = getSystemSession(repository, storeActionNode.getSession().getWorkspace().getName());
      Node actionNode = (Node) session.getItem(newActionPath);
      actionNode.remove();
      session.save();
      session.logout(); 
    }
  }

  public void executeAction(String userId, Node node, String actionName, String repository) throws Exception {
    Map<String, String> variables = new HashMap<String, String>();
    variables.put("initiator", userId);
    variables.put("actionName", actionName);
    variables.put("nodePath", node.getPath());
    variables.put("srcWorkspace", node.getSession().getWorkspace().getName());    
    variables.put("srcPath", node.getParent().getPath());

    NodeType nodeType = node.getPrimaryNodeType();
    String nodeTypeName = nodeType.getName();
    variables.put("document-type", nodeTypeName);
    Node parentNode = node.getParent() ;
    Node actionNode = parentNode.getNode(EXO_ACTIONS + "/" + actionName);
    NodeType actionNodeType = actionNode.getPrimaryNodeType();
    fillVariables(actionNode, actionNodeType, variables);

    NodeType[] actionMixinTypes = actionNode.getMixinNodeTypes();

    for (int i = 0; i < actionMixinTypes.length; i++) {
      NodeType mixinType = actionMixinTypes[i];
      fillVariables(actionNode, mixinType, variables);
    }

    executeAction(userId, node, actionName, variables, repository);
  }

  private void fillVariables(Node actionNode, NodeType nodeType, Map variables) throws Exception {    
    for(PropertyDefinition def:nodeType.getDeclaredPropertyDefinitions()) {
      String propName = def.getName();
      if (actionNode.hasProperty(propName)) {
        String propValue = actionNode.getProperty(propName).getString();
        variables.put(propName, propValue);
      }
    }    
  }

  public void executeAction(String userId, Node node, String actionName, Map variables, String repository) throws Exception {
    Node parentNode = node.getParent() ;
    if (!parentNode.isNodeType(ACTIONABLE)) return ;
    Node actionNode = parentNode.getNode(EXO_ACTIONS + "/" +actionName);
    String actionTypeName = actionNode.getPrimaryNodeType().getName();
    for (Iterator iter = actionPlugins.iterator(); iter.hasNext();) {
      ComponentPlugin plugin = (ComponentPlugin) iter.next();
      String actionServiceName = plugin.getName();
      ActionPlugin actionPlugin = getActionPlugin(actionServiceName);
      if (actionPlugin.isActionTypeSupported(actionTypeName)) {
        actionPlugin.executeAction(userId, actionNode, variables, repository);
      }
    }    
  }

  public void addPlugin(ComponentPlugin plugin) { actionPlugins.add(plugin); }

  public ComponentPlugin removePlugin(String pluginName) {
    return null;
  }

  public Collection getPlugins() { return actionPlugins; }

  private void initiateActionConfiguration() throws Exception {
    List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
    ManageableRepository jcrRepository = null ;
    for(RepositoryEntry repository : repositories) { 
      jcrRepository = repositoryService_.getRepository(repository.getName());
      String[] workspaces = jcrRepository.getWorkspaceNames();
      for (String workspace : workspaces) {
        Session session = jcrRepository.getSystemSession(workspace);        
        QueryManager queryManager = null;
        try {
          queryManager = session.getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
          System.out.println("[WARN] ActionServiceContainer - Query Manager Factory of workspace "
              + workspace + " not found. Check configuration.");
        }
        if (queryManager == null) {
          session.logout();
          continue; 
        }          
        initAction(queryManager, repository.getName(), workspace) ;
        session.logout();
      }
    }    
  }

  private void reInitiateActionConfiguration(String repository) throws Exception {
    ManageableRepository jcrRepository = repositoryService_.getRepository(repository);    
    for (String workspace : jcrRepository.getWorkspaceNames()) {
      Session session = jcrRepository.getSystemSession(workspace);
      QueryManager queryManager = null;
      try {
        queryManager = session.getWorkspace().getQueryManager();
      } catch (RepositoryException e) {
        System.out.println("[WARN] ActionServiceContainer - Query Manager Factory of workspace "
            + workspace + " not found. Check configuration.");
      }
      if (queryManager == null)  {
        session.logout();
        continue;
      }
      initAction(queryManager, repository, workspace) ;
      session.logout();
    }
  }

  private void initAction(QueryManager queryManager, String repository, String workspace) throws Exception {
    try {
      Query query = queryManager.createQuery(ACTION_QUERY, Query.XPATH);
      QueryResult queryResult = query.execute();
      for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
        Node actionNode = iter.nextNode();
        String lifecyclePhase = actionNode.getProperty(LIFECYCLE_PHASE_PROP).getString();
        String actionType = actionNode.getPrimaryNodeType().getName();
        for (Iterator pluginIter = actionPlugins.iterator(); pluginIter.hasNext();) {
          ComponentPlugin plugin = (ComponentPlugin) pluginIter.next();
          String actionServiceName = plugin.getName();
          ActionPlugin actionService = getActionPlugin(actionServiceName);
          if (actionService.isActionTypeSupported(actionType)) {
            if (lifecyclePhase.equals(ActionServiceContainer.SCHEDULE_PHASE)) {
              actionService.reScheduleActivations(actionNode, repository);
            } else {
              actionService.initiateActionObservation(actionNode, repository);
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println(">>>> Can not launch action listeners for wokrpsace: " 
          + workspace + " in '" + repository + "' repository");
      // e.printStackTrace() ;
    }
  }

  public void initiateObservation(Node node, String repository) throws Exception {
    try {
      Session session = node.getSession();
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      String queryStr;
      if (node.getPath() != "/") {
        queryStr = "/jcr:root" + node.getPath() + ACTION_QUERY;
      } else {
        queryStr = ACTION_QUERY;
      }
      Query query = queryManager.createQuery(queryStr, Query.XPATH);
      QueryResult queryResult = query.execute();
      for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
        Node actionNode = iter.nextNode();
        try {
          String actionType = actionNode.getPrimaryNodeType().getName();
          for (Iterator pluginIter = actionPlugins.iterator(); pluginIter.hasNext();) {
            ComponentPlugin plugin = (ComponentPlugin) pluginIter.next();
            String actionServiceName = plugin.getName();
            ActionPlugin actionService = getActionPlugin(actionServiceName);
            if (actionService.isActionTypeSupported(actionType)) {
              actionService.initiateActionObservation(actionNode, repository);
            }
          }
        } catch (Exception e) {
          System.out.println("Can not launch action listeners named is " + actionNode.getPath());
          // e.printStackTrace();
        }
      }
    } catch (Exception ex) {
      System.out.println("Can not launch action listeners inside " + node.getPath() + " node.");
      // ex.printStackTrace() ;
    }
  }
}
