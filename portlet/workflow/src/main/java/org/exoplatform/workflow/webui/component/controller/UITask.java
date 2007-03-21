/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */
package org.exoplatform.workflow.webui.component.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormDateTimeInput;
import org.exoplatform.webui.component.UIFormInput;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.workflow.webui.component.InputInfo;
import org.exoplatform.workflow.webui.component.VariableMaps;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UITask.gtmpl",
    events = {
        @EventConfig(listeners = UITask.CancelActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UITask.StartProcessActionListener.class),
        @EventConfig(listeners = UITask.EndOfStateActionListener.class),
        @EventConfig(listeners = UITask.TransitionActionListener.class)
    }
)
public class UITask extends UIForm {

  public static final String MANAGE_TRANSITION = "manageTransition";
  private static final String TEXT = "text";
  private static final String TEXTAREA = "textarea";
//  private static final String WYSIWYG = "wysiwyg";
  private static final String DATE = "date";
  private static final String DATE_TIME = "datetime";
  private static final String SELECT = "select";
  private static final String UPLOAD = "upload";
  private static final String CHECK_BOX = "checkbox";
  private static final String RADIO_BOX = "radiobox";
  private static final String NODE_TYPE = "nodetype";
  private static final String NODE_VIEW = "nodeview";
  private static final String NODE_EDIT = "nodeedit";
  private static final String LABEL_ENCODING = ".label";
  private static final String NODE_PATH_VARIABLE = "nodePath";
  private static final String WORKSPACE_VARIABLE = "srcWorkspace";

  private Form form;
  private boolean isStart;
  private String identification;
  private WorkflowServiceContainer serviceContainer;
  private WorkflowFormsService formsService;
  private TemplateService dialogService;
  private RepositoryService jcrService;
  private List<InputInfo> inputInfo;
  private boolean isView;
  private boolean isCreatedOrUpdated;
  private String dialogPath_;

  public UITask() {
    serviceContainer = getApplicationComponent(WorkflowServiceContainer.class) ;
    formsService = getApplicationComponent(WorkflowFormsService.class) ;
    dialogService = getApplicationComponent(TemplateService.class) ;
    jcrService = getApplicationComponent(RepositoryService.class) ;
    inputInfo = new ArrayList<InputInfo>();
  }

  public String getManageTransition() { return MANAGE_TRANSITION ; }

  public String getStateImageURL() {
    try {
      Locale locale = getAncestorOfType(UIApplication.class).getLocale();
      if (isStart) {
        Process process = serviceContainer.getProcess(identification);
        form = formsService.getForm(identification, process.getStartStateName(), locale);
      } else {
        Task task = serviceContainer.getTask(identification);
        form = formsService.getForm(task.getProcessId(), task.getTaskName(), locale);
      }
      return form.getStateImageURL();
    } catch (Exception e) {
      return "";
    }
  }

  public void updateUITree() throws Exception {
    clean() ;
    UITaskManager taskManager = getAncestorOfType(UITaskManager.class) ;
    UIDocumentContent docContent = taskManager.getUIDocContent() ;
    
    Locale locale = getAncestorOfType(UIApplication.class).getLocale();
    Map variablesForService = new HashMap();
    if (isStart) {
      Process process = serviceContainer.getProcess(identification);
      form = formsService.getForm(identification, process.getStartStateName(), locale);
    } else {
      Task task = serviceContainer.getTask(identification);
      String processInstanceId = task.getProcessInstanceId();
      variablesForService = serviceContainer.getVariables(processInstanceId, identification);
      form = formsService.getForm(task.getProcessId(), task.getTaskName(), locale);
    }
    String workspaceName = (String) variablesForService.get(WORKSPACE_VARIABLE);
    List variables = form.getVariables();
    UIFormInput input = null;
    int i = 0;
    for (Iterator iter = variables.iterator(); iter.hasNext(); i++) {
      Map attributes = (Map) iter.next();
      String name = (String) attributes.get("name");
      String component = (String) attributes.get("component");
      String editableString = (String) attributes.get("editable");
      if (editableString != null && !"".equals(editableString)) {
//        editable = new Boolean(editableString).booleanValue();
      }
      boolean mandatory = false;
      String mandatoryString = (String) attributes.get("mandatory");
      if (mandatoryString != null && !"".equals(mandatoryString)) {
        mandatory = new Boolean(mandatoryString).booleanValue();
      }
      Object value = variablesForService.get(name);
      String userName = Util.getUIPortal().getOwner() ;
      if (NODE_TYPE.equals(component)) {
        dialogPath_ = dialogService.getTemplatePathByUser(true, (String) value, userName);
        isCreatedOrUpdated = true;
      } else if (NODE_EDIT.equals(component)) {
        String nodePath = (String) variablesForService.get(NODE_PATH_VARIABLE);
        Node viewNode = (Node) jcrService.getRepository().getSystemSession(workspaceName).getItem(nodePath);
        isView = false ;
        docContent.setNode(viewNode);
        String nodetype = viewNode.getPrimaryNodeType().getName();
        dialogPath_ = dialogService.getTemplatePathByUser(true, nodetype, userName);
        isCreatedOrUpdated = true;
      } else if (NODE_VIEW.equals(component)) {
        String nodePath = (String) variablesForService.get(NODE_PATH_VARIABLE);
        Node viewNode = (Node) jcrService.getRepository().getSystemSession(workspaceName).getItem(nodePath);
        isView = true ;
        docContent.setNode(viewNode);
      } else {
        if (component == null || TEXT.equals(component)) {
          input = new UIFormStringInput(name, (String) value);
        } else if (TEXTAREA.equals(component)) {
          input = new UIFormTextAreaInput(name, null, (String) value);
//        } else if (WYSIWYG.equals(component)) {
////          input = new UIWYSIWYG(name, (String) value);
        } else if (DATE.equals(component) || DATE_TIME.equals(component)) {
          input = (value == null ? new UIFormDateTimeInput(name, null, new Date()) : 
                                   new UIFormDateTimeInput(name, null, (Date)value)) ;
        } else if (SELECT.equals(component)) {
          String baseKey = name + ".select-";
          List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
          int j = 0;
          String select0 = (String) variablesForService.get(baseKey + j);
          if (select0 == null) {
            ResourceBundle bundle = form.getResourceBundle();
            try {
              while (true) {
                String property = bundle.getString(baseKey + j);
                options.add(new SelectItemOption<String>(property, property));
                j++;
              }
            } catch (MissingResourceException e) {}
          } else {
            while (true) {
              String property = (String) variablesForService.get(baseKey + j);
              if (property == null) break;
              options.add(new SelectItemOption<String>(property, property));
              j++;
            }
          }
          input = new UIFormSelectBox(name, (String) value, options);
        } else if (CHECK_BOX.equals(component)) {
          ResourceBundle bundle = form.getResourceBundle();
          String key = name + ".checkbox";
          input = new UIFormCheckBoxInput<Boolean>(name, bundle.getString(key), Boolean.valueOf(
              (String) value).booleanValue());
        } else if (UPLOAD.equals(component)) {
          input = new UIFormUploadInput(name, name);
        } else if (RADIO_BOX.equals(component)) {
          String baseKey = name + ".radiobox-";
          List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
          int j = 0;
          String select0 = (String) variablesForService.get(baseKey + j);
          if (select0 == null) {
            ResourceBundle bundle = form.getResourceBundle();
            try {
              while (true) {
                String property = bundle.getString(baseKey + j);
                options.add(new SelectItemOption<String>(property, property));
                j++;
              }
            } catch (MissingResourceException e) {}
          } else {
            while (true) {
              String property = (String) variablesForService.get(baseKey + j);
              if (property == null) break;
              options.add(new SelectItemOption<String>(property, property));
              j++;
            }
          }
          input = new UIFormRadioBoxInput(name, (String) value, options);
        }
//        input.setEditable(editable);
        ResourceBundle res = form.getResourceBundle();
        inputInfo.add(new InputInfo("", "", res.getString(name + LABEL_ENCODING), input, mandatory));
        addUIFormInput(input);
      }
    }
    if(isView || isCreatedOrUpdated) taskManager.addChild(docContent) ;
  }

  public void setIsStart(boolean b) { isStart = b ; }
  public boolean isStart() { return isStart ; }

  public boolean isView() { return isView ; }

  public boolean isCreatedOrUpdated() { return isCreatedOrUpdated ; }

  public String getDialogPath() { return dialogPath_ ; }

  public ResourceBundle getWorkflowBundle() { return form.getResourceBundle() ; }

  public List getInputInfo() { return inputInfo ; }

  public List getSubmitButtons() { return form.getSubmitButtons() ; }

  public boolean isCustomizedView() { return form.isCustomizedView() ; }
  public String getCustomizedView() { return form.getCustomizedView() ; }

  public void setIdentification(String identification) { this.identification = identification ; }
  public String getIdentification() { return identification ; }

  public VariableMaps prepareVariables() throws Exception {
    VariableMaps maps = prepareWorkflowVariables(getChildren());
    return maps;
  }

  public VariableMaps prepareWorkflowVariables(Collection inputs) throws Exception {
    Map<String, Object> workflowVariables = new HashMap<String, Object>();
    Map jcrVariables = new HashMap();
    for (Iterator iter = inputs.iterator(); iter.hasNext();) {
      UIFormInput input = (UIFormInput) iter.next();
      String name = input.getName();

      Object value = "";
      if (input instanceof UIFormStringInput) {
        value = ((UIFormStringInput) input).getValue();
      } else if (input instanceof UIFormDateTimeInput) {
        Calendar calendar = ((UIFormDateTimeInput) input).getCalendar();
        value = calendar.getTime();
      } else if (input instanceof UIFormDateTimeInput) {
        Calendar calendar = ((UIFormDateTimeInput) input).getCalendar();
        value = calendar.getTime();
//      } else if (input instanceof UIWYSIWYG) {
//        value = ((UIWYSIWYG) input).getValue();
      } else if (input instanceof UIFormTextAreaInput) {
        value = ((UIFormTextAreaInput) input).getValue();
      } else if (input instanceof UIFormCheckBoxInput) {
        value = new Boolean(((UIFormCheckBoxInput) input).isChecked()).toString();
      } else if (input instanceof UIFormSelectBox) {
        value = ((UIFormSelectBox) input).getValue();
      } else if (input instanceof UIFormRadioBoxInput) {
        value = ((UIFormRadioBoxInput) input).getValue();
      } else if (input instanceof UIFormUploadInput) {
        value = ((UIFormUploadInput) input).getUploadData() ;
      }

      if (value == null) value = "";
      workflowVariables.put(name, value);

//      String jcrPath = (String) input.getProperty("jcrPath");
//      if (jcrPath != null) {
//        jcrVariables.put(jcrPath, value);
//      } else {
//        workflowVariables.put(name, value);
//      }
    }
    return new VariableMaps(workflowVariables, jcrVariables);
  }

  public void clean() {
    isView = false;
    isCreatedOrUpdated = false;
    dialogPath_ = null;
    inputInfo.clear();
    getAncestorOfType(UITaskManager.class).removeChild(UIDocumentContent.class) ;
  }

  public static class StartProcessActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource() ;
      PortletRequestContext pcontext = (PortletRequestContext)RequestContext.getCurrentInstance() ;
      String remoteUser = pcontext.getRemoteUser();
      if (remoteUser == null) remoteUser = "anonymous";
      VariableMaps maps = uiTask.prepareVariables();
      Map variables = maps.getWorkflowVariables();
      uiTask.serviceContainer.startProcess(remoteUser, uiTask.identification, variables);
      uiTask.getAncestorOfType(UIPopupWindow.class).setShow(false) ;
    }
  }

  public static class EndOfStateActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource();
      VariableMaps maps = uiTask.prepareVariables();
      try {
        Map variables = maps.getWorkflowVariables();
        uiTask.serviceContainer.endTask(uiTask.identification, variables);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      uiTask.getAncestorOfType(UIPopupWindow.class).setShow(false) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UIPopupWindow popup = event.getSource().getAncestorOfType(UIPopupWindow.class) ;
      popup.setShow(false) ;
    }
  }

  public static class TransitionActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource() ;
      VariableMaps maps = uiTask.prepareVariables();
      List submitButtons = uiTask.form.getSubmitButtons();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      for (Iterator iterator = submitButtons.iterator(); iterator.hasNext();) {
        Map attributes = (Map) iterator.next();
        String name = (String) attributes.get("name");
        if (objectId.equals(name)) {
          String transition = (String) attributes.get("transition");
          try {
            Map variables = maps.getWorkflowVariables();
            uiTask.serviceContainer.endTask(uiTask.identification, variables, transition);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      uiTask.getAncestorOfType(UIPopupWindow.class).setShow(false) ;
    }
  }
}