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
package org.exoplatform.processes.publishing;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.log.ExoLogger;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Dec 13, 2007  
 */
public class ManagePublicationActionHandler implements ActionHandler {

  private static final long serialVersionUID = 1L;    
  private boolean executed = false;  

  public void execute(ExecutionContext context) {
    try {      
      if(executed) {
        return; 
      }             
      executed = true;
      publishContent(context);
    } catch (Exception e) {
      e.printStackTrace();
      ExoLogger.getLogger(this.getClass()).error(e);
    } finally {         
      context.getToken().signal("publication-done");
      context.getSchedulerInstance().cancel("publicationTimer", context.getToken());
    }
  }
  
  protected void publishContent(ExecutionContext context) throws Exception{    
    String[] currentLocation = ProcessUtil.getCurrentLocation(context);
    String repository = currentLocation[0];
    String currentWorkspace = currentLocation[1];
    String currentPath = currentLocation[2];                    
    String publishWorkspace=(String)context.getVariable("exo:publishWorkspace");
    String publishPath = (String)context.getVariable("exo:publishPath");        
    CmsService cmsService = ProcessUtil.getService(CmsService.class);    
    if(publishPath.endsWith("/")) {
      publishPath = publishPath + currentPath.substring(currentPath.lastIndexOf("/") + 1) ;
    } else {
      publishPath = publishPath + currentPath.substring(currentPath.lastIndexOf("/")) ;
    }    
    cmsService.moveNode(currentPath, currentWorkspace, publishWorkspace, publishPath, repository);    
    context.setVariable(ProcessUtil.CURRENT_STATE,ProcessUtil.LIVE);    
    ProcessUtil.setCurrentLocation(context,publishWorkspace,publishPath);
    ProcessUtil.publish(context);
  }

} 