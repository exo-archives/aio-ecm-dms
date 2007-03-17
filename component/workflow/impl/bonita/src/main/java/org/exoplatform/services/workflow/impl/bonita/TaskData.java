/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
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
