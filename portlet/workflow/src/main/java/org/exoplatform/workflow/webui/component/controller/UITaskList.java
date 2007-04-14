/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller ;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/UITaskList.gtmpl",
    events = {@EventConfig(listeners = UITaskList.ManageStateActionListener.class)}
)
public class UITaskList extends UIContainer {
  private WorkflowServiceContainer workflowServiceContainer;
  private WorkflowFormsService workflowFormsService;

  public UITaskList() throws Exception {
    workflowServiceContainer = getApplicationComponent(WorkflowServiceContainer.class);
    workflowFormsService = getApplicationComponent(WorkflowFormsService.class);
  }

  public String getProcessName(Task task) {
    return workflowServiceContainer.getProcess(task.getProcessId()).getName();
  }

  public Map getVariables(Task task) {
    return workflowServiceContainer.getVariables(task.getProcessInstanceId(), task.getId());
  }

  public Date getProcessInstanceStartDate(Task task) {
    return this.workflowServiceContainer.getProcessInstance(task.getProcessInstanceId()).getStartDate();
  }

  public String getIconURL(Task task) {
    try {
      Locale locale = getAncestorOfType(UIApplication.class).getLocale();    
      Form form = workflowFormsService.getForm(task.getProcessId(), task.getTaskName(), locale);
      return form.getIconURL(); 
    } catch(Exception e) {
      return "" ;
    }    
  }

  public List<Task> getTasks() throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    String remoteUser = pcontext.getRemoteUser();
    if (remoteUser == null) return selectVisibleTasks(new ArrayList<Task>()) ;      
    List<Task> unsortedTasks = workflowServiceContainer.getAllTasks(remoteUser);
    return selectVisibleTasks(unsortedTasks) ; 
  }

  private List<Task> selectVisibleTasks(List<Task> all) {
    List<Task> filtered = new ArrayList<Task>();
    Locale locale = getAncestorOfType(UIApplication.class).getLocale();    
    for (Iterator iter = all.iterator(); iter.hasNext();) {
      Task task = (Task) iter.next();
      Form form = workflowFormsService.getForm(task.getProcessId(), task.getTaskName(), locale);
      if(!form.isDelegatedView()) { filtered.add(task) ; }
    }
    return filtered;
  }

  static  public class ManageStateActionListener extends EventListener<UITaskList> {
    public void execute(Event<UITaskList> event) throws Exception {
      UITaskList taskList = event.getSource() ;
      String tokenId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      taskList.setRenderSibbling(UITaskList.class) ;
      UIWorkflowControllerPortlet portlet = taskList.getAncestorOfType(UIWorkflowControllerPortlet.class) ;
      UIPopupWindow popup = portlet.getChild(UIPopupWindow.class) ;
      popup.setShow(true) ;
      popup.setRendered(true) ;
      UITaskManager uiTaskManager = (UITaskManager)popup.getUIComponent() ;
      UITask uiTask = uiTaskManager.getChild(UITask.class) ;
      try {
        uiTask.setIdentification(tokenId) ;
        uiTask.setIsStart(false) ;
        uiTask.updateUITree() ;
      } catch (PathNotFoundException e){
        Task task = taskList.workflowServiceContainer.getTask(tokenId);
        String pid = task.getProcessInstanceId();       
        taskList.workflowServiceContainer.deleteProcessInstance(pid);
      }
    }
  }
}