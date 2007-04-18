/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller ;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIApplication;
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
    events = {@EventConfig(listeners = UIControllerManager.ManageStartActionListener.class)}
)
public class UIControllerManager extends UIContainer {
  private static String[] BPDEFINITION_BEAN_FIELD = {"id", "name", "version"} ;
  private static String[] ACTION = {"ManageStart"} ;
  private WorkflowServiceContainer service_;
  
  public UIControllerManager() throws Exception {
    service_ = getApplicationComponent(WorkflowServiceContainer.class) ;
    addChild(UITaskList.class, null, null) ;
    UIGrid uiBPDefinitionGrid = addChild(UIGrid.class, null, "UIBPDefinition").setRendered(false) ;
    uiBPDefinitionGrid.setLabel("UIBPDefinition") ;
    uiBPDefinitionGrid.getUIPageIterator().setId("UIBPDefinitionGrid") ;
    uiBPDefinitionGrid.configure("id", BPDEFINITION_BEAN_FIELD, ACTION) ;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    Locale locale = getAncestorOfType(UIApplication.class).getLocale() ;
    WorkflowFormsService workflowFormsService = getApplicationComponent(WorkflowFormsService.class) ;
    List<Process> processes = service_.getProcesses() ;
    
    List<Process> visibleDefinitions = new ArrayList<Process>() ;
    for(Process process : processes) {
      Form form = workflowFormsService.getForm(process.getId(), process.getStartStateName(), locale) ;
      if (form != null && !form.isDelegatedView()) visibleDefinitions.add(process) ;
    }
    UIGrid uiBPDefinitionGrid = getChild(UIGrid.class) ;
    uiBPDefinitionGrid.getUIPageIterator().setPageList(new ObjectPageList(visibleDefinitions, 10)) ;
    super.processRender(context);
  }
  
  static  public class ManageStartActionListener extends EventListener<UIControllerManager> {
    public void execute(Event<UIControllerManager> event) throws Exception {
      UIControllerManager uiControllerManager = event.getSource() ;
      uiControllerManager.setRenderedChild(UIGrid.class) ;
      String processId = event.getRequestContext().getRequestParameter(OBJECTID);
      if(uiControllerManager.service_.hasStartTask(processId)) {      
        UIWorkflowControllerPortlet portlet = uiControllerManager.getParent() ;
        UIPopupWindow popup = portlet.getChild(UIPopupWindow.class) ;
        popup.setResizable(true) ;
        popup.setShow(true) ;
        popup.setRendered(true) ;
        UITaskManager uiTaskManager = (UITaskManager)popup.getUIComponent() ;
        uiTaskManager.setRendered(true) ;
        UITask uiTask = uiTaskManager.getChild(UITask.class) ;
        uiTask.setRenderSibbling(UITask.class) ;
        uiTask.setIdentification(processId);
        uiTask.setIsStart(true);
        uiTask.updateUITree();
      } else {
        uiControllerManager.service_.startProcess(processId);
      }
    }
  }
}