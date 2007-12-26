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
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Dec 13, 2007  
 */
public class BackupContentActionHandler implements ActionHandler {

  private static final long serialVersionUID = 1L;

  private boolean executed = false;

  public void execute(ExecutionContext context) {    
    try {
      if (executed)
        return;
      executed = true;
      backupContent(context);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      context.getToken().signal("backup-done");
      context.getSchedulerInstance().cancel("backupTimer", context.getToken());
    }
  }

  protected void backupContent(ExecutionContext context) throws Exception {                  
    String[] currentLocation = ProcessUtil.getCurrentLocation(context);
    String repository =currentLocation[0];
    String currentWorkspace = currentLocation[1];
    String currentPath = currentLocation[2];
    String backupWorkspace = (String)context.getVariable("exo:backupWorkspace");
    String backupPath = (String)context.getVariable("exo:backupPath");    
    if(backupPath.endsWith("/")) {
      backupPath = backupPath + currentPath.substring(currentPath.lastIndexOf("/") + 1) ;
    } else {
      backupPath = backupPath + currentPath.substring(currentPath.lastIndexOf("/")) ;
    }            
    CmsService cmsService = ProcessUtil.getService(CmsService.class);
    cmsService.moveNode(currentPath, currentWorkspace, backupWorkspace, backupPath, repository);
    ProcessUtil.setCurrentLocation(context,backupWorkspace,backupPath);    
    ProcessUtil.backup(context);
  }

}