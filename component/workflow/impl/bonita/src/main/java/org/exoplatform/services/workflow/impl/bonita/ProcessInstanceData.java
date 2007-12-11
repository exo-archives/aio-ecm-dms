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
