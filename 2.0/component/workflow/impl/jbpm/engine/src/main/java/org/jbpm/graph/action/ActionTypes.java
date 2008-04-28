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
