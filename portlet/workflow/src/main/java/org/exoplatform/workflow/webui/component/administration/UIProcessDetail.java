/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.administration ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfigs({
  @ComponentConfig(
      type = UIGrid.class, id = "UIProcessGrid",
      template = "app:groovy/webui/component/UIECMGrid.gtmpl"
  ),
  @ComponentConfig(
      lifecycle = UIContainerLifecycle.class,
      events = {
        @EventConfig(listeners = UIProcessDetail.ViewActionListener.class),
        @EventConfig(listeners = UIProcessDetail.DeleteActionListener.class, confirm = "UIProcessDetail.msg.confirm-delete-process")
      }
  )
})
public class UIProcessDetail extends UIContainer {
  private static String[] PROCESS_BEAN_FIELD = {"processInstanceId", "processId", "processName", "startDate", "endDate"} ;
  private static String[] TASK_BEAN_FIELD = {"id", "taskName", "actorId", "end"} ;
  
  private static String[] ACTION = {"View","Delete"} ;
  private String processInstanceId;
  
  public UIProcessDetail() throws Exception {
    UIGrid uiProcessDetail = addChild(UIGrid.class, "UIProcessGrid", "UIProcessDetailGrid") ;
    UIGrid uiTasksGrid = addChild(UIGrid.class, "UIProcessGrid", "UITasksGrid").setRendered(false) ;
    
    uiProcessDetail.setLabel("UIProcessDetailGrid") ;
    uiProcessDetail.getUIPageIterator().setId("UIProcessDetailGrid") ;
    uiProcessDetail.configure("processInstanceId", PROCESS_BEAN_FIELD, ACTION) ;
    
    uiTasksGrid.setLabel("UITasksGrid") ;
    uiTasksGrid.getUIPageIterator().setId("UITasksGrid") ;
    uiTasksGrid.configure("id", TASK_BEAN_FIELD, null) ;
  }
  
  public void updateProcessGrid(String id) throws Exception {
    WorkflowServiceContainer workflowServiceContainer = 
      getApplicationComponent(WorkflowServiceContainer.class);
    if(id != null) processInstanceId = id ;
    setRenderedChild("UIProcessDetailGrid") ;
    UIGrid uiMonitorGrid = getChildById("UIProcessDetailGrid") ;
    uiMonitorGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowServiceContainer.getProcessInstances(processInstanceId), 10)) ;
  }

  @SuppressWarnings("unchecked")
  public void updateTasksGrid(String id) throws Exception {
    WorkflowServiceContainer workflowServiceContainer = 
      getApplicationComponent(WorkflowServiceContainer.class);
    UIGrid uiGrid = getChildById("UITasksGrid") ;
    List<Task> haveEndDateList = new ArrayList<Task>() ;
    for(Task task : workflowServiceContainer.getTasks(id)) {
      if(task.getEnd() != null) haveEndDateList.add(task) ;
    }
    Collections.sort(haveEndDateList, new DateComparator()) ;
    uiGrid.getUIPageIterator().setPageList(new ObjectPageList(haveEndDateList, 10)) ;
  }

  static public class DateComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      Date date1 = ((Task) o1).getEnd() ;
      Date date2 = ((Task) o2).getEnd() ;
      return date1.compareTo(date2) ;
    }
  }
  
  static  public class ViewActionListener extends EventListener<UIProcessDetail> {
    public void execute(Event<UIProcessDetail> event) throws Exception {
      UIProcessDetail uicomp = event.getSource() ;
      String instance = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uicomp.setRenderedChild("UITasksGrid") ;
      uicomp.updateTasksGrid(instance) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIProcessDetail> {
    public void execute(Event<UIProcessDetail> event) throws Exception {
      UIProcessDetail uicomp = event.getSource() ;
      String instance = event.getRequestContext().getRequestParameter(OBJECTID) ;
      WorkflowServiceContainer workflowServiceContainer = 
        uicomp.getApplicationComponent(WorkflowServiceContainer.class);
      workflowServiceContainer.deleteProcessInstance(instance);
      uicomp.updateProcessGrid(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
    }
  }
}