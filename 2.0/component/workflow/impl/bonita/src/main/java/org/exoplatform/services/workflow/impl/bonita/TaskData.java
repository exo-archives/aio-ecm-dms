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
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.BnNodeValue;
import hero.interfaces.BnProject;
import hero.interfaces.BnProjectHome;
import hero.interfaces.BnProjectPK;
import hero.interfaces.BnProjectUtil;

import java.util.Date;

import org.exoplatform.services.workflow.Task;

/**
 * Created by Bull R&D
 * @author Brice Revenant
 * Jan 1, 2006
 */
public class TaskData implements Task {
  private String actorId           = null;
  private String description       = null;
  private String id                = null;
  private String processId         = null;
  private String processInstanceId = null;
  private String taskName          = null;
  private Date   end               = null;  

  public TaskData(BnNodeValue node) {
    this.actorId = node.getBnRole().getName();
    this.description = node.getDescription();
    this.id = node.getId();
    this.processInstanceId = node.getBnProject().getId();
    this.taskName = node.getName();
    this.end = node.getEndDate();
    
    try
    {
      BnProjectHome projectHome = BnProjectUtil.getHome();
      String modelName = WorkflowServiceContainerHelper.getModelName(
        node.getBnProject().getName());
      BnProject project = projectHome.findByName(modelName);
      this.processId = project.getId();
    }
    catch(Exception e) {
      // TODO Use logging facilities instead
      e.printStackTrace();
      this.processId = "";
    }
  }
  
  public String getActorId() {
    return this.actorId;
  }

  public String getDescription() {
    return this.description;
  }

  public String getId() {
    return this.id;
  }

  public String getProcessId() {
    return this.processId;
  }

  public String getProcessInstanceId() {
    return this.processInstanceId;
  }

  public String getTaskName() {
    return this.taskName;
  }

  public Date getEnd() {
    return this.end;
  }
}
