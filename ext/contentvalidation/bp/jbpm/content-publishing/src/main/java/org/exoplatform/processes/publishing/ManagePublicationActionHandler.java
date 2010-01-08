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

import org.exoplatform.services.log.Log;
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
  private static final Log LOG  = ExoLogger.getLogger(ManagePublicationActionHandler.class);

  public void execute(ExecutionContext context) {
    try {      
      if(executed) {
        return; 
      }             
      executed = true;
      publishContent(context);
    } catch (Exception e) {
      LOG.error("Unexpected error", e);
      ExoLogger.getLogger(this.getClass()).error(e);
    } finally {               
      context.getSchedulerInstance().cancel("publicationTimer", context.getToken());      
      context.getToken().signal("publication-done");            
    }
  }
  
  protected void publishContent(ExecutionContext context) throws Exception{    
    String[] currentLocation = ProcessUtil.getCurrentLocation(context);
    String repository = currentLocation[0];
    String currentWorkspace = currentLocation[1];
    String currentPath = currentLocation[2];                    
    String publishWorkspace=(String)context.getVariable("exo:publishWorkspace");
    String publishPath = (String)context.getVariable("exo:publishPath");
    String realPublishPath = ProcessUtil.computeDestinationPath(currentPath,publishPath);
    CmsService cmsService = ProcessUtil.getService(CmsService.class);            
    cmsService.moveNode(currentPath, currentWorkspace, publishWorkspace, realPublishPath, repository);    
    context.setVariable(ProcessUtil.CURRENT_STATE,ProcessUtil.LIVE);    
    ProcessUtil.setCurrentLocation(context,publishWorkspace,realPublishPath);
    ProcessUtil.publish(context);
  }

} 