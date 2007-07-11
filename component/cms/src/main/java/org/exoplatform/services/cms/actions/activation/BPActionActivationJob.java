/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.actions.activation;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.actions.ActionPlugin;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.actions.impl.BPActionPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.security.SecurityService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Dec 21, 2006  
 */
public class BPActionActivationJob implements Job {
  final private static String COUNTER_PROP = "exo:counter".intern() ;

  public void execute(JobExecutionContext context) throws JobExecutionException {    
    RepositoryService repositoryService = 
      (RepositoryService) PortalContainer.getComponent(RepositoryService.class);    
    ActionServiceContainer actionServiceContainer = 
      (ActionServiceContainer) PortalContainer.getComponent(ActionServiceContainer.class);
    SecurityService securityService = 
      (SecurityService) PortalContainer.getComponent(SecurityService.class);
    ActionPlugin bpActionService = actionServiceContainer.getActionPlugin(BPActionPlugin.ACTION_TYPE) ;

    Session jcrSession = null;
    Node actionNode = null ;

    JobDataMap jdatamap = context.getJobDetail().getJobDataMap() ;
    String userId = jdatamap.getString("initiator") ;
    String srcWorkspace = jdatamap.getString("srcWorkspace") ;
    String srcPath = jdatamap.getString("srcPath") ;
    String actionName = jdatamap.getString("actionName") ;
    String executable = jdatamap.getString("executable") ;
    String repository = jdatamap.getString("repository") ;
    Map variables = jdatamap.getWrappedMap() ;
    try {
      jcrSession = repositoryService.getRepository(repository).getSystemSession(srcWorkspace);
      Node node = (Node) jcrSession.getItem(srcPath);
      actionNode = actionServiceContainer.getInitAction(node, actionName);
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
      if (!hasPermission) return;
      bpActionService.activateAction(userId,executable,variables,repository) ;
      int currentCounter = (int)actionNode.getProperty(COUNTER_PROP).getValue().getLong() ;
      actionNode.setProperty(COUNTER_PROP,currentCounter +1) ;
      actionNode.save() ;
      jcrSession.save() ;
    } catch (Exception e) {
      e.printStackTrace();     
    }    
  }

}
