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
package org.jbpm.graph.node;

import java.util.*;
import org.jbpm.graph.def.*;

public class NodeTypes {

  public static Set getNodeTypes() {
    return nodeNames.keySet();
  }
  
  public static Set getNodeNames() {
    return nodeTypes.keySet();
  }
  
  public static Class getNodeType(String name) {
    return (Class) nodeTypes.get(name);
  }
  
  public static String getNodeName(Class type) {
    return (String) nodeNames.get(type);
  }
  
  private static Map nodeTypes = initialiseNodeTypes();
  private static Map nodeNames = createInverseMapping(nodeTypes);
  
  private static Map initialiseNodeTypes() {
    Map types = new HashMap();
    types.put("start-state", StartState.class);
    types.put("end-state", EndState.class);
    types.put("node", Node.class);
    types.put("state", State.class);
    types.put("task-node", TaskNode.class);
    types.put("fork", Fork.class);
    types.put("join", Join.class);
    types.put("decision", Decision.class);
    types.put("process-state", ProcessState.class);
    types.put("super-state", SuperState.class);
    types.put("merge", Merge.class);
    types.put("milestone-node", MilestoneNode.class);
    types.put("interleave-start", InterleaveStart.class);
    types.put("interleave-end", InterleaveEnd.class);
    return types; 
  }

  public static Map createInverseMapping(Map map) {
    Map names = new HashMap();
    Iterator iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      names.put(entry.getValue(), entry.getKey());
    }
    return names;
  }

  //private static final Log log = LogFactory.getLog(NodeTypes.class);
}
