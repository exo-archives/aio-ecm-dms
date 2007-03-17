/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.monitoring ;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIGrid;
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
    template = "system:groovy/webui/component/UITabPane.gtmpl",
    events = {
        @EventConfig(listeners = UIMonitorManager.ViewActionListener.class),
        @EventConfig(listeners = UIMonitorManager.DeleteActionListener.class)
    }
)
public class UIMonitorManager extends UIContainer {
  private static String[] MONITOR_BEAN_FIELD = {"id", "name", "version"} ;
  private static String[] TIMERS_BEAN_FIELD = {"id", "name", "dueDate"} ;
  
  private static String[] ACTION = {"View","Delete"} ;
  private WorkflowServiceContainer workflowService;
  
  public UIMonitorManager() throws Exception {
    UIGrid uiMonitorGrid = addChild(UIGrid.class, null, "UIMonitor") ;
    UIGrid uiTimersGrid = addChild(UIGrid.class, null, "UITimers").setRendered(false) ;
    workflowService = getApplicationComponent(WorkflowServiceContainer.class);
    
    uiMonitorGrid.setLabel("UIMonitor") ;
    uiMonitorGrid.getUIPageIterator().setId("UIMonitorGrid") ;
    uiMonitorGrid.configure("id", MONITOR_BEAN_FIELD, ACTION) ;
    updateMonitorGrid() ;
    
    uiTimersGrid.setLabel("UITimers") ;
    uiTimersGrid.getUIPageIterator().setId("UITimersGrid") ;
    uiTimersGrid.configure("id", TIMERS_BEAN_FIELD, null) ;
    updateTimersGrid() ;
  }
  
  public void updateMonitorGrid() throws Exception {
    UIGrid uiMonitorGrid = getChildById("UIMonitor") ;
    uiMonitorGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowService.getProcesses(), 10)) ;
  }

  public void updateTimersGrid() throws Exception {
    UIGrid uiGrid = getChildById("UITimers") ;
    uiGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowService.getTimers(), 10)) ;
  }

  static  public class ViewActionListener extends EventListener<UIMonitorManager> {
    public void execute(Event<UIMonitorManager> event) throws Exception {
      UIWorkflowMonitoringPortlet portlet = event.getSource().getParent() ;
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupWindow popup = portlet.getChild(UIPopupWindow.class);
      ((UIProcessDetail)popup.getUIComponent()).updateProcessGrid(id) ;
      popup.setShow(true) ;
      popup.setWindowSize(700, 0) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIMonitorManager> {
    public void execute(Event<UIMonitorManager> event) throws Exception {
      UIMonitorManager uicomp = event.getSource() ;
      String processDef = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uicomp.workflowService.deleteProcess(processDef);
      uicomp.updateMonitorGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
    }
  }
}