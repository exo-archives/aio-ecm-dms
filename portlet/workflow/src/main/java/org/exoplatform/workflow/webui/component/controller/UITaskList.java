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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
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
  
  private WorkflowServiceContainer workflowServiceContainer_;
  
  private WorkflowFormsService workflowFormsService_;
  
  private static final String NODE_VIEW = "nodeview";
  
  private static final String NODE_EDIT = "nodeedit";
  
  private static final String NODE_PATH_VARIABLE = "nodePath";
  
  private static final String WORKSPACE_VARIABLE = "srcWorkspace";
  
  private static final String REPOSITORY_VARIABLE = "repository";
  
  public UITaskList() throws Exception {
    workflowServiceContainer_ = getApplicationComponent(WorkflowServiceContainer.class);
    workflowFormsService_ = getApplicationComponent(WorkflowFormsService.class);
  }

  public String getProcessName(Task task) {
    return workflowServiceContainer_.getProcess(task.getProcessId()).getName();
  }

  public Date getProcessInstanceStartDate(Task task) {
    return this.workflowServiceContainer_.getProcessInstance(task.getProcessInstanceId()).getStartDate();
  }

  public String getIconURL(Task task) {
    try {
      Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale() ;    
      Form form = workflowFormsService_.getForm(task.getProcessId(), task.getTaskName(), locale);
      return form.getIconURL(); 
    } catch(Exception e) {
      return "" ;
    }    
  }

  @SuppressWarnings("unchecked")
  public List<Task> getTasks() throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    String remoteUser = pcontext.getRemoteUser();
    if (remoteUser == null) return selectVisibleTasks(new ArrayList<Task>()) ;      
    List<Task> unsortedTasks = workflowServiceContainer_.getAllTasks(remoteUser);
    Collections.sort(unsortedTasks, new DateComparator()) ;
    return selectVisibleTasks(unsortedTasks) ; 
  }
  
  public class DateComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      Date date1 = getProcessInstanceStartDate((Task) o1) ;
      Date date2 = getProcessInstanceStartDate((Task) o2) ;
      return date1.compareTo(date2) ;
    }
  }

  private List<Task> selectVisibleTasks(List<Task> all) {
    List<Task> filtered = new ArrayList<Task>();
    Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale() ;    
    for (Iterator iter = all.iterator(); iter.hasNext();) {
      Task task = (Task) iter.next();
      Form form = workflowFormsService_.getForm(task.getProcessId(), task.getTaskName(), locale);
      if(!form.isDelegatedView()) {
        if (checkTaskWithNodeExist(task, form)) {
          filtered.add(task) ; 
        }
      }
    }
    return filtered;
  }
  
  private boolean checkTaskWithNodeExist(Task task, Form form) {
      String processInstanceId = task.getProcessInstanceId();
      String identification_ = task.getId();
      Map variablesForService = new HashMap();
      variablesForService = workflowServiceContainer_.getVariables(processInstanceId, identification_);
      RepositoryService jcrService = getApplicationComponent(RepositoryService.class) ;
      String workspaceName = (String) variablesForService.get(WORKSPACE_VARIABLE);
      String repository = (String) variablesForService.get(REPOSITORY_VARIABLE);
      try {
        if(repository == null) {
          repository = jcrService.getDefaultRepository().getConfiguration().getName() ;
        }
        ManageableRepository mRepository = jcrService.getRepository(repository) ;
        SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;
        List variables = form.getVariables();
        int i = 0;
        for (Iterator iter = variables.iterator(); iter.hasNext(); i++) {
          Map attributes = (Map) iter.next();
          String component = (String) attributes.get("component");
          if (NODE_EDIT.equals(component) || NODE_VIEW.equals(component)) {
            String nodePath = (String) variablesForService.get(NODE_PATH_VARIABLE);          
            sessionProvider.getSession(workspaceName,mRepository).getItem(nodePath);
          }
        }
      } catch (Exception e) {
        return false;
      }
      return true;
  }
  /**
   * Indicates whether a Task has been processed or not.
   * This method was introduced to avoid bug described in ECM-2374, when two
   * users try to manage the same Task.
   * 
   * @param taskId identifies the Task
   * @return true if the Task has been processed
   */
  public boolean isTaskActivated(String taskId) {
    try {
      Task task = this.workflowServiceContainer_.getTask(taskId);
      return task.getEnd() != null;
    } catch(Exception e) {
      return true ;
    }
  }

  static public class ManageStateActionListener extends EventListener<UITaskList> {
    public void execute(Event<UITaskList> event) throws Exception {
      UITaskList uiTaskList = event.getSource() ;
      String tokenId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      if (uiTaskList.isTaskActivated(tokenId)) {
        uiApp.addMessage(new ApplicationMessage("UITaskList.msg.task-not-found", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } else {
        uiTaskList.setRenderSibbling(UITaskList.class) ;
        UIWorkflowControllerPortlet uiControllerPortlet = uiTaskList.getAncestorOfType(UIWorkflowControllerPortlet.class);
        UITaskManager uiTaskManager = uiControllerPortlet.createUIComponent(UITaskManager.class, null, null);
        uiTaskManager.setTokenId(tokenId);
        uiTaskManager.setIsStart(false);
        if (!uiTaskManager.checkBeforeActive()) {
          uiApp.addMessage(new ApplicationMessage("UITaskList.msg.task-change", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskList);
          return;
        }
        UIPopupContainer uiPopupAction = uiControllerPortlet.getChild(UIPopupContainer.class) ;
        uiPopupAction.activate(uiTaskManager, 600) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
}