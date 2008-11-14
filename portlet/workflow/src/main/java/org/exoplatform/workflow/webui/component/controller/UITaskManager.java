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
package org.exoplatform.workflow.webui.component.controller ;

import javax.jcr.PathNotFoundException;

import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(template = "app:/groovy/webui/component/UITabPane.gtmpl")
public class UITaskManager extends UIContainer implements UIPopupComponent {
  
  private String tokenId_ ;
  private boolean isStart_ = false;
  
  public UITaskManager() throws Exception {
    addChild(UITask.class, null, null) ;
  }

  public void setTokenId(String tokenId) { tokenId_ = tokenId ; }
  
  public void setIsStart(boolean isStart) { isStart_ = isStart ; }
  
  public void activate() throws Exception { }
  
  public boolean checkBeforeActive() throws Exception {
    WorkflowServiceContainer workflowServiceContainer = 
      getApplicationComponent(WorkflowServiceContainer.class);
    try {
      UITask uiTask = getChild(UITask.class);
      uiTask.setIdentification(tokenId_);
      uiTask.setIsStart(isStart_);
      uiTask.updateUITree();
      return true;
    } catch (PathNotFoundException e) {
      Task task = workflowServiceContainer.getTask(tokenId_);
      String pid = task.getProcessInstanceId();       
      workflowServiceContainer.deleteProcessInstance(pid);
      return false;
    } 
  }

  public void deActivate() throws Exception { }
}