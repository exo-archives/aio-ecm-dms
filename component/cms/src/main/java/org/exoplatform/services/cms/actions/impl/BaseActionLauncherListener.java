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

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.security.SecurityService;

public abstract class BaseActionLauncherListener implements ECMEventListener {
  
  protected String actionName_;
  protected String repository_ ;
  protected String srcWorkspace_;
  protected String srcPath_;
  protected String executable_;
  protected Map actionVariables_;
  
  public BaseActionLauncherListener(String actionName, String executable,
      String repository, String srcWorkspace, String srcPath, Map actionVariables)
  throws Exception {
    actionName_ = actionName;
    executable_ = executable;
    repository_ = repository ;
    srcWorkspace_ = srcWorkspace;
    srcPath_ = srcPath;
    actionVariables_ = actionVariables;
  }
  
  public String getSrcWorkspace() { return srcWorkspace_; }  
  public String getRepository() { return repository_; }
  
  @SuppressWarnings("unchecked")
  public void onEvent(EventIterator events) {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer() ;
    RepositoryService repositoryService = 
      (RepositoryService) exoContainer.getComponentInstanceOfType(RepositoryService.class);    
    ActionServiceContainer actionServiceContainer = 
      (ActionServiceContainer) exoContainer.getComponentInstanceOfType(ActionServiceContainer.class);
    SecurityService securityService = 
      (SecurityService) exoContainer.getComponentInstanceOfType(SecurityService.class);        
    TemplateService templateService = 
      (TemplateService) exoContainer.getComponentInstanceOfType(TemplateService.class);       
    if (events.hasNext()) {
      Event event = events.nextEvent();  
      Node node = null;      
      Session jcrSession = null;
      try {
        jcrSession = repositoryService.getRepository(repository_).getSystemSession(srcWorkspace_);
        node = (Node) jcrSession.getItem(srcPath_);
        String userId = event.getUserID();
        Node actionNode = actionServiceContainer.getAction(node, actionName_);
        Property rolesProp = actionNode.getProperty("exo:roles");
        boolean hasPermission = false;
        Value[] roles = rolesProp.getValues();
        for (int i = 0; i < roles.length; i++) {
          String role = roles[i].getString();
          if (securityService.hasMembershipInGroup(userId, role)
              || SystemIdentity.SYSTEM.equals(userId)) {
            hasPermission = true;
            break;
          }
        }
        if (!hasPermission) {
          jcrSession.logout();
          return;
        }          
        String path = event.getPath();
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("initiator", userId);
        variables.put("actionName", actionName_);
        variables.put("nodePath", path);
        variables.put("repository", repository_);
        variables.put("srcWorkspace", srcWorkspace_);
        variables.put("srcPath", srcPath_);
        variables.putAll(actionVariables_);
        if(event.getType() == Event.NODE_ADDED) {          
          node = (Node) jcrSession.getItem(path);        
          String nodeType = node.getPrimaryNodeType().getName();
          if (templateService.getDocumentTemplates(repository_).contains(nodeType)) {                    
            variables.put("document-type", nodeType);
            triggerAction(userId, variables, repository_);
          }          
        } else {
          triggerAction(userId, variables, repository_);
        }
        jcrSession.logout();
      } catch (Exception e) {
        jcrSession.logout();
        e.printStackTrace();
      }
    }
  }
  
  public abstract void triggerAction(String userId, Map variables, String repository)
  throws Exception;
}
