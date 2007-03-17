/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import java.util.Date;

import hero.interfaces.BnProjectLocal;
import hero.interfaces.BnProjectLocalHome;
import hero.interfaces.BnProjectUtil;
import hero.interfaces.BnProjectValue;

import org.exoplatform.services.workflow.ProcessInstance;

/**
 * Created by Bull R&D
 * @author Brice Revenant
 * Jan 1, 2006
 */
public class ProcessInstanceData implements ProcessInstance {
  private String processInstanceId = null;
  private String processId         = null;
  private String processName       = null;
  private Date   startDate         = null;
  private Date   endDate           = null;
  
  public ProcessInstanceData(BnProjectValue processInstance) {
    try {
      this.processInstanceId = processInstance.getId();
      this.processName       = processInstance.getName();
      this.startDate         = processInstance.getCreationDate();
      this.endDate           = processInstance.getEndDate();
      this.processId         = WorkflowServiceContainerHelper.
                                 getModelName(this.processName);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public String getProcessInstanceId() {
    return this.processInstanceId;
  }

  public String getProcessId() {
    return this.processId;
  }

  public String getProcessName() {
    return this.processName;
  }

  public Date getStartDate() {
    return this.startDate;
  }

  public Date getEndDate() {
    return this.endDate;
  }
}
