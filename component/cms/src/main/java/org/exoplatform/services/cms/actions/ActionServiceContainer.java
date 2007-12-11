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
      String executable, List<String> variableNames, boolean isMoveType, String repository) throws Exception;

  public Collection<NodeType> getCreatedActionTypes(String repository) throws Exception;
    
  public Node getAction(Node node, String actionName) throws Exception;

  public boolean hasActions(Node node) throws Exception;

  public List<Node> getActions(Node node) throws Exception;

  public List<Node> getActions(Node node, String lifecyclePhase) throws Exception;
  
  public void removeAction(Node node, String actionName, String repository) throws Exception;

  public void addAction(Node node, String repository, String type, Map mappings) throws Exception; 
  
  public void executeAction(String userId, Node node, String actionName, Map variables, String repository) throws Exception;
  
  public void executeAction(String userId, Node node, String actionName, String repository) throws Exception;
  
  public void initiateObservation(Node node, String repository) throws Exception ;
  
  public void init(String repository) throws Exception ;
      
}
