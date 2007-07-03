/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.administration ;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
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
      type = UIGrid.class, id = "UIECMGrid",
      template = "app:groovy/webui/component/UIECMGrid.gtmpl"
  ),
  @ComponentConfig(
      template = "app:groovy/webui/component/UITabPaneWithAction.gtmpl",
      events = {
          @EventConfig(listeners = UIAdministrationManager.ViewActionListener.class),
          @EventConfig(listeners = UIAdministrationManager.DeleteActionListener.class, confirm = "UIAdministrationManager.msg.confirm-delete-process"),
          @EventConfig(listeners = UIAdministrationManager.UploadProcessActionListener.class)
      }
  )}
)
public class UIAdministrationManager extends UIContainer {
  private static String[] MONITOR_BEAN_FIELD = {"id", "name", "version"} ;
  private static String[] TIMERS_BEAN_FIELD = {"id", "name", "dueDate"} ;
  
  private static String[] ACTION = {"View","Delete"} ;
  private static String[] ACTIONS = {"UploadProcess"} ;
  
  public UIAdministrationManager() throws Exception {
    UIGrid uiMonitorGrid = addChild(UIGrid.class, "UIECMGrid", "UIMonitor") ;
    UIGrid uiTimersGrid = addChild(UIGrid.class, "UIECMGrid", "UITimers").setRendered(false) ;
    uiMonitorGrid.setLabel("UIMonitor") ;
    uiMonitorGrid.getUIPageIterator().setId("UIMonitorGrid") ;
    uiMonitorGrid.configure("id", MONITOR_BEAN_FIELD, ACTION) ;
    updateMonitorGrid() ;
    
    uiTimersGrid.setLabel("UITimers") ;
    uiTimersGrid.getUIPageIterator().setId("UITimersGrid") ;
    uiTimersGrid.configure("id", TIMERS_BEAN_FIELD, null) ;
    updateTimersGrid() ;
  }
  
  public String[] getActions() { return ACTIONS ; }
  
  public void updateMonitorGrid() throws Exception {
    UIGrid uiMonitorGrid = getChildById("UIMonitor") ;
    WorkflowServiceContainer workflowServiceContainer = getApplicationComponent(WorkflowServiceContainer.class);
    uiMonitorGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowServiceContainer.getProcesses(), 10)) ;
  }

  public void updateTimersGrid() throws Exception {
    WorkflowServiceContainer workflowServiceContainer = getApplicationComponent(WorkflowServiceContainer.class);
    UIGrid uiGrid = getChildById("UITimers") ;
    uiGrid.getUIPageIterator().setPageList(new ObjectPageList(workflowServiceContainer.getTimers(), 10)) ;
  }

  static  public class ViewActionListener extends EventListener<UIAdministrationManager> {
    public void execute(Event<UIAdministrationManager> event) throws Exception {
      UIWorkflowAdministrationPortlet uiAdministrationPortlet = event.getSource().getParent() ;
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupWindow popup = uiAdministrationPortlet.getChild(UIPopupWindow.class);
      ((UIProcessDetail)popup.getUIComponent()).updateProcessGrid(id) ;
      popup.setShow(true) ;
      popup.setWindowSize(700, 0) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIAdministrationManager> {
    public void execute(Event<UIAdministrationManager> event) throws Exception {
      UIAdministrationManager uiAdminManager = event.getSource() ;
      String processDef = event.getRequestContext().getRequestParameter(OBJECTID) ;
      WorkflowServiceContainer workflowServiceContainer = 
        uiAdminManager.getApplicationComponent(WorkflowServiceContainer.class) ;
      workflowServiceContainer.deleteProcess(processDef);
      uiAdminManager.updateMonitorGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAdminManager) ;
    }
  }
  
  static  public class UploadProcessActionListener extends EventListener<UIAdministrationManager> {
    public void execute(Event<UIAdministrationManager> event) throws Exception {
      UIWorkflowAdministrationPortlet uiAdministrationPortlet = event.getSource().getParent() ;
      uiAdministrationPortlet.initUploadPopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAdministrationPortlet) ;
    }
  }
}