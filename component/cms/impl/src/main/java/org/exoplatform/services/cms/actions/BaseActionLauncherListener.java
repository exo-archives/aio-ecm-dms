package org.exoplatform.services.cms.actions;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
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
  
  public String getSrcWorkspace() {
    return srcWorkspace_;
  }
  
  public String getRepository() {
    return repository_;
  }
  
  public void onEvent(EventIterator events) {    
    RepositoryService repositoryService = 
      (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
    TemplateService templateService = 
      (TemplateService) PortalContainer.getComponent(TemplateService.class);
    ActionServiceContainer actionServiceContainer = 
      (ActionServiceContainer) PortalContainer.getComponent(ActionServiceContainer.class);
    SecurityService securityService = 
      (SecurityService) PortalContainer.getComponent(SecurityService.class);    
    if (events.hasNext()) {
      Event event = events.nextEvent();  
      Node node = null;      
      Session jcrSession = null;
      try {
        jcrSession = repositoryService.getRepository(repository_).getSystemSession(srcWorkspace_);
        node = (Node) jcrSession.getItem(srcPath_);
        AccessControlList acl = ((ExtendedNode) node).getACL();
        String userId = (acl != null) ? acl.getOwner() : "__unknown";
        Node actionNode = actionServiceContainer.getInitAction(node, actionName_);
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
        if (!hasPermission)
          return;

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
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public abstract void triggerAction(String userId, Map variables, String repository)
  throws Exception;
}
