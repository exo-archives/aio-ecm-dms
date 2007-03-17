package org.exoplatform.services.cms.actions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

public interface ActionServiceContainer {  
  
  public static final String READ_PHASE = "read";
  public static final String ADD_PHASE = "add";
  public static final String MODIFY_PHASE = "modify";
  public static final String REMOVE_PHASE = "remove";
  public static final String SCHEDULE_PHASE = "schedule" ;

  public Collection<String> getActionPluginNames();
  
  public ActionPlugin getActionPlugin(String actionServiceName);
  
  public ActionPlugin getActionPluginForActionType(String actionTypeName);
  
  public void createActionType(String actionTypeName, String parentActionTypeName, 
      String executable, List<String> variableNames, boolean isMoveType) throws Exception;

  public Collection<NodeType> getCreatedActionTypes() throws Exception;
  
  public Node getAction(Node node, String actionName) throws Exception;
  
  public Node getInitAction(Node node, String actionName) throws Exception;

  public boolean hasActions(Node node) throws Exception;

  public List<Node> getActions(Node node) throws Exception;

  public List<Node> getActions(Node node, String lifecyclePhase) throws Exception;
  
  public void removeAction(Node node, String actionName) throws Exception;

  public void addAction(Node node, String type, Map mappings) throws Exception; 
  
  public void executeAction(String userId, Node node, String actionName, Map variables) throws Exception;
  
  public void executeAction(String userId, Node node, String actionName) throws Exception;
  
  public void initiateObservation(Node node) throws Exception ;
      
}
