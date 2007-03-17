/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.monitoring ;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
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
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UIProcessDetail.ViewActionListener.class),
      @EventConfig(listeners = UIProcessDetail.DeleteActionListener.class)
    }
)
public class UIProcessDetail extends UIContainer {
  private static String[] PROCESS_BEAN_FIELD = {"processInstanceId", "processId", "processName", "startDate", "endDate"} ;
  private static String[] TASK_BEAN_FIELD = {"id", "taskName", "actorId", "end"} ;
  
  private static String[] ACTION = {"View","Delete"} ;
  private WorkflowServiceContainer workflowService;
  private String processInstanceId;
  
  public UIProcessDetail() throws Exception {
    UIGrid uiProcessDetail = addChild(UIGrid.class, null, "UIProcessDetailGrid") ;
    UIGrid uiTasksGrid = addChild(UIGrid.class, null, "UITasksGrid").setRendered(false) ;
    workflowService = getApplicationComponent(WorkflowServiceContainer.class);
    
    uiProcessDetail.setLabel("UIProcessDetailGrid") ;
    uiProcessDetail.getUIPageIterator().setId("UIProcessDetailGrid") ;
    uiProcessDetail.configure("processInstanceId", PROCESS_BEAN_FIELD, ACTION) ;
    
    uiTasksGrid.setLabel("UITasksGrid") ;
    uiTasksGrid.getUIPageIterator().setId("UITasksGrid") ;
    uiTasksGrid.configure("id", TASK_BEAN_FIELD, null) ;
  }
  
  public void updateProcessGrid(String id) throws Exception {
    if(id != null) processInstanceId = id ;
    setRenderedChild("UIProcessDetailGrid") ;
    UIGrid uiMonitorGrid = getChildById("UIProcessDetailGrid") ;
    uiMonitorGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowService.getProcessInstances(processInstanceId), 10)) ;
  }

  public void updateTasksGrid(String id) throws Exception {
    UIGrid uiGrid = getChildById("UITasksGrid") ;
    uiGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowService.getTasks(id), 10)) ;
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
      uicomp.workflowService.deleteProcessInstance(instance);
      uicomp.updateProcessGrid(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
    }
  }
}