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
import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
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
      type = UIGrid.class, id = "UIControllerGrid",
      template = "app:/groovy/webui/component/UIECMGrid.gtmpl"
  ),
  @ComponentConfig(
      template = "system:/groovy/webui/core/UITabPane.gtmpl",
      events = {@EventConfig(listeners = UIControllerManager.ManageStartActionListener.class)}
  )
})
public class UIControllerManager extends UIContainer {
  private static String[] BPDEFINITION_BEAN_FIELD = {"id", "name", "version"} ;
  private static String[] ACTION = {"ManageStart"} ;
  private WorkflowServiceContainer service_;
  
  public UIControllerManager() throws Exception {
    service_ = getApplicationComponent(WorkflowServiceContainer.class) ;
    addChild(UITaskList.class, null, null) ;
    UIGrid uiBPDefinitionGrid = addChild(UIGrid.class, "UIControllerGrid", "UIBPDefinition").setRendered(false) ;
    uiBPDefinitionGrid.setLabel("UIBPDefinition") ;
    uiBPDefinitionGrid.getUIPageIterator().setId("UIBPDefinitionGrid") ;
    uiBPDefinitionGrid.configure("id", BPDEFINITION_BEAN_FIELD, ACTION) ;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale() ;
    WorkflowFormsService workflowFormsService = getApplicationComponent(WorkflowFormsService.class) ;
    List<Process> processes = service_.getProcesses() ;
    
    List<Process> visibleDefinitions = new ArrayList<Process>() ;
    for(Process process : processes) {
      workflowFormsService.removeForms(process.getId()) ;
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
        UIPopupContainer uiPopup = portlet.getChild(UIPopupContainer.class) ;
        UITaskManager uiTaskManager = portlet.createUIComponent(UITaskManager.class, null, null) ;
        uiTaskManager.setTokenId(processId) ;
        uiTaskManager.setIsStart(true);
        uiTaskManager.checkBeforeActive();
        uiPopup.activate(uiTaskManager, 600, 500) ;
      } else {
        uiControllerManager.service_.startProcess(processId);
      }
    }
  }
}