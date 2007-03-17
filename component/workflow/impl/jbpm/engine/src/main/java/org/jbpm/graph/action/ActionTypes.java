package org.jbpm.graph.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.node.NodeTypes;
import org.jbpm.scheduler.def.CancelTimerAction;
import org.jbpm.scheduler.def.CreateTimerAction;


public class ActionTypes {

  public static Set getActionTypes() {
    return actionNames.keySet();
  }
  
  public static Set getActionNames() {
    return actionTypes.keySet();
  }
  
  public static Class getActionType(String name) {
    return (Class) actionTypes.get(name);
  }
  
  public static String getActionName(Class type) {
    return (String) actionNames.get(type);
  }

  public static boolean hasActionName(String name) {
    return actionTypes.containsKey(name);
  }

  private static final Log log = LogFactory.getLog(ActionTypes.class);
  private static Map actionTypes = initialiseActionTypes();
  private static Map actionNames = NodeTypes.createInverseMapping(actionTypes);
  
  private static Map initialiseActionTypes() {
    Map types = new HashMap();
    types.put("action", Action.class);
    types.put("create-timer", CreateTimerAction.class);
    types.put("cancel-timer", CancelTimerAction.class);
    try {
      types.put("script", Script.class);
    // if the beanshell lib is not in the class path
    // this throws a NoClassDefFoundException
    } catch (Throwable t) {
      log.debug("no beanshell lib present, disabling script actions");
    }
    return types; 
  }
}
