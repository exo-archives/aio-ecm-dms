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
package org.exoplatform.workflow.webui.component.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.Process;
import org.exoplatform.services.workflow.Task;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
import org.exoplatform.workflow.webui.component.BJARResourceResolver;
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
        @EventConfig(listeners = UITask.TransitionActionListener.class),
        @EventConfig(listeners = UITask.SelectUserActionListener.class, phase = Phase.DECODE)
        
    }
)
public class UITask extends UIForm implements UISelectable{

  public static final String MANAGE_TRANSITION = "manageTransition";
  private static final String TEXT = "text";
  private static final String TEXTAREA = "textarea";
  private static final String WYSIWYG = "wysiwyg";
  private static final String DATE = "date";
  private static final String DATE_TIME = "datetime";
  private static final String SELECT = "select";
  private static final String UPLOAD = "upload";
  private static final String CHECK_BOX = "checkbox";
  private static final String RADIO_BOX = "radiobox";
//  private static final String NODE_TYPE = "nodetype";
  private static final String NODE_VIEW = "nodeview";
  private static final String NODE_EDIT = "nodeedit";
  private static final String LABEL_ENCODING = ".label";
  private static final String NODE_PATH_VARIABLE = "nodePath";
  private static final String WORKSPACE_VARIABLE = "srcWorkspace";
  private static final String REPOSITORY_VARIABLE = "repository";

  private static final String DELEGATE_FIELD = "delegate";
  private Form form;
  private boolean isStart_;
  private String identification_;
  private WorkflowServiceContainer serviceContainer;
  private WorkflowFormsService formsService;
  private RepositoryService jcrService;
  private List<InputInfo> inputInfo_;

  
  public UITask() {
    serviceContainer = getApplicationComponent(WorkflowServiceContainer.class) ;
    formsService = getApplicationComponent(WorkflowFormsService.class) ;
    jcrService = getApplicationComponent(RepositoryService.class) ;
    inputInfo_ = new ArrayList<InputInfo>();
  }
  
  public String getTemplate() {
    if(isCustomizedView()) {
      return getIdentification() + ":" + getCustomizedView() ;
    }
    return getComponentConfig().getTemplate() ;
  }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(isCustomizedView()) return new BJARResourceResolver(serviceContainer) ;
    return super.getTemplateResourceResolver(context, getComponentConfig().getTemplate()) ;
  }
  
  public String getManageTransition() { return MANAGE_TRANSITION ; }

  public String getStateImageURL() {
    try {
      Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale() ;
      if (isStart()) {
        Process process = serviceContainer.getProcess(identification_);
        form = formsService.getForm(identification_, process.getStartStateName(), locale);
      } else {
        Task task = serviceContainer.getTask(identification_);
        form = formsService.getForm(task.getProcessId(), task.getTaskName(), locale);
      }
      return form.getStateImageURL();
    } catch (Exception e) {
      return "";
    }
  }

  @SuppressWarnings("unchecked")
  public void updateUITree() throws Exception {
    clean() ;
    UITaskManager uiTaskManager = getParent() ;
    Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale() ;
    Map variablesForService = new HashMap();
    if (isStart_) {
      Process process = serviceContainer.getProcess(identification_);
      form = formsService.getForm(identification_, process.getStartStateName(), locale);
    } else {
      Task task = serviceContainer.getTask(identification_);
      String processInstanceId = task.getProcessInstanceId();
      variablesForService = serviceContainer.getVariables(processInstanceId, identification_);
      form = formsService.getForm(task.getProcessId(), task.getTaskName(), locale);
    }
    String workspaceName = (String) variablesForService.get(WORKSPACE_VARIABLE);
    String repository = (String) variablesForService.get(REPOSITORY_VARIABLE);
    if(repository == null) {
      repository = jcrService.getDefaultRepository().getConfiguration().getName() ;
    }
    ManageableRepository mRepository = jcrService.getRepository(repository) ;
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;
    List variables = form.getVariables();
    UIFormInput input = null;
    int i = 0;
    for (Iterator iter = variables.iterator(); iter.hasNext(); i++) {
      Map attributes = (Map) iter.next();
      String name = (String) attributes.get("name");
      String component = (String) attributes.get("component");
      String editableString = (String) attributes.get("editable");
      boolean editable = true ;
      if (editableString != null && !"".equals(editableString)) {
        editable = new Boolean(editableString).booleanValue();
      }
      boolean mandatory = false;
      String mandatoryString = (String) attributes.get("mandatory");
      if (mandatoryString != null && !"".equals(mandatoryString)) {
        mandatory = new Boolean(mandatoryString).booleanValue();
      }
      boolean visiable = true;
      String visiableString = (String) attributes.get("visiable");
      if (visiableString != null && !"".equals(visiableString)) {
        visiable = new Boolean(visiableString).booleanValue();
      }
      Object value = variablesForService.get(name);
      
      if (NODE_EDIT.equals(component)) {
        UIDocumentForm uiDocForm = createUIComponent(UIDocumentForm.class, null, null) ;
        String nodePath = (String) variablesForService.get(NODE_PATH_VARIABLE);          
        Node dialogNode = (Node) sessionProvider.getSession(workspaceName,mRepository).getItem(nodePath);
        String nodetype = dialogNode.getPrimaryNodeType().getName();
        uiDocForm.setNodePath(nodePath);
        uiDocForm.setTemplateNode(nodetype) ;
        uiDocForm.setRepositoryName(repository) ;
        Task task = serviceContainer.getTask(identification_);
        form = formsService.getForm(task.getProcessId(), task.getTaskName(), locale);
        uiTaskManager.addChild(uiDocForm) ;
        uiDocForm.setRendered(false) ;
      } else if (NODE_VIEW.equals(component)) {
        String nodePath = (String) variablesForService.get(NODE_PATH_VARIABLE);
        Node viewNode = (Node) sessionProvider.getSession(workspaceName,mRepository).getItem(nodePath);
        UIDocumentContent uiDocContent = createUIComponent(UIDocumentContent.class, null, null) ;
        uiDocContent.setNode(viewNode);
        uiTaskManager.addChild(uiDocContent) ;
        uiDocContent.setRendered(false) ;
      } else {
        if (component == null || TEXT.equals(component)) {
          input = new UIFormStringInput(name, (String) value);
          ((UIFormStringInput)input).setEditable(editable);
        } else if (TEXTAREA.equals(component)) {
          input = new UIFormTextAreaInput(name, null, (String) value);
          ((UIFormTextAreaInput)input).setEditable(editable);
        } else if (WYSIWYG.equals(component)) {
          input = new UIFormWYSIWYGInput(name, name, (String) value);
          ((UIFormWYSIWYGInput)input).setToolBarName(UIFormWYSIWYGInput.DEFAULT_TOOLBAR);
          ((UIFormWYSIWYGInput)input).setEditable(editable);
        } else if (DATE.equals(component) || DATE_TIME.equals(component)) {
          input = (value == null ? new UIFormDateTimeInput(name, null, new Date(), DATE_TIME.equals(component)) : 
                                   new UIFormDateTimeInput(name, null, (Date)value, DATE_TIME.equals(component)));
          if (!visiable && (value == null)) {
            input.setValue("");
          }
          input.addValidator(DateTimeValidator.class);
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
          ((UIFormSelectBox)input).setEditable(editable);
        } else if (CHECK_BOX.equals(component)) {
          ResourceBundle bundle = form.getResourceBundle();
          String key = name + ".checkbox";
          input = new UIFormCheckBoxInput<Boolean>(name, bundle.getString(key), Boolean.valueOf(
              (String) value).booleanValue());
          ((UIFormCheckBoxInput)input).setEditable(editable);
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
        ResourceBundle res = form.getResourceBundle();
        inputInfo_.add(new InputInfo("", "", res.getString(name + LABEL_ENCODING), input, mandatory));
        addUIFormInput(input);
      }
    }
  }

  public void setIsStart(boolean b) { isStart_ = b ; }
  public boolean isStart() { return isStart_ ; }

  public ResourceBundle getWorkflowBundle() { return form.getResourceBundle() ; }

  public List getInputInfo() { return inputInfo_ ; }

  public List getSubmitButtons() { return form.getSubmitButtons() ; }

  public boolean isCustomizedView() { return form.isCustomizedView() ; }
  public String getCustomizedView() { return form.getCustomizedView() ; }

  public void setIdentification(String identification) { this.identification_ = identification ; }
  public String getIdentification() { return identification_ ; }

  public VariableMaps prepareVariables() throws Exception {
    VariableMaps maps = prepareWorkflowVariables(getChildren());
    return maps;
  }
  
  @SuppressWarnings("unchecked")
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
        /*if (calendar == null) calendar = new GregorianCalendar();
        value = calendar.getTime();*/
        value = ((calendar == null) ? (name.equals("startDate") ? (new GregorianCalendar().getTime()) : ("")) : (calendar.getTime()));
      } else if (input instanceof UIFormWYSIWYGInput) {
        value = ((UIFormWYSIWYGInput)input).getValue();
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
    }
    String repository = jcrService.getDefaultRepository().getConfiguration().getName() ;
    workflowVariables.put(Utils.REPOSITORY, repository) ;
    return new VariableMaps(workflowVariables, jcrVariables);
  }

  public void clean() { 
    UITaskManager uiTaskManager = getParent() ;
    uiTaskManager.removeChild(UIDocumentContent.class) ;
    uiTaskManager.removeChild(UIDocumentForm.class) ;
    inputInfo_.clear(); 
  }

  public static class StartProcessActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource() ;
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      String remoteUser = pcontext.getRemoteUser();
      if (remoteUser == null) remoteUser = "anonymous";
      VariableMaps maps = uiTask.prepareVariables();
      Map variables = maps.getWorkflowVariables();
      uiTask.serviceContainer.startProcess(remoteUser, uiTask.identification_, variables);
      uiTask.getAncestorOfType(UIPopupContainer.class).deActivate() ;
    }
  }

  public static class EndOfStateActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource();
      VariableMaps maps = uiTask.prepareVariables();
      try {
        Map variables = maps.getWorkflowVariables();
        uiTask.serviceContainer.endTask(uiTask.identification_, variables);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      uiTask.getAncestorOfType(UIPopupContainer.class).deActivate() ;
    }
  }

  static  public class CancelActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UIPopupContainer uiPopup = event.getSource().getAncestorOfType(UIPopupContainer.class) ;
      uiPopup.deActivate() ;
    }
  }

  public static class TransitionActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource() ;
      VariableMaps maps = uiTask.prepareVariables();
      List submitButtons = uiTask.form.getSubmitButtons();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Task task = uiTask.serviceContainer.getTask(uiTask.identification_);
      String processInstanceId = task.getProcessInstanceId();
      Map variablesForService = uiTask.serviceContainer.getVariables(processInstanceId, uiTask.identification_);
      String nodePath = (String)variablesForService.get("nodePath");
      String srcPath = (String)variablesForService.get("srcPath");
      String srcWorkspace = (String)variablesForService.get("srcWorkspace");
      String repository = (String)variablesForService.get("repository");
      RepositoryService repositoryService = uiTask.getApplicationComponent(RepositoryService.class);
      if (objectId.equals("delegate")) {
        String delegate = (String)maps.getWorkflowVariables().get(UITask.DELEGATE_FIELD);
        if (delegate.length() == 0) {
            UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
            uiApp.addMessage(new ApplicationMessage("UITask.msg.has-not-got-delegate", null, 
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
        }
      }
      for (Iterator iterator = submitButtons.iterator(); iterator.hasNext();) {
        Map attributes = (Map) iterator.next();
        String name = (String) attributes.get("name");
        if (objectId.equals(name)) {
          String transition = (String) attributes.get("transition");
          try {
            if(nodePath != null) {
              Session session = 
                SessionProviderFactory.createSessionProvider().getSession(srcWorkspace, repositoryService.getRepository(repository));
              Node node = (Node)session.getItem(nodePath);
              if(node.isLocked()) {
                String actionName = (String)variablesForService.get("actionName");
                ActionServiceContainer actionServiceContainer = 
                  uiTask.getApplicationComponent(ActionServiceContainer.class);
                Node actionNode = actionServiceContainer.getAction((Node)session.getItem(srcPath), actionName);
                String destPath = actionNode.getProperty("exo:destPath").getString() + nodePath.substring(nodePath.lastIndexOf("/"));
                String destWorkspace = actionNode.getProperty("exo:destWorkspace").getString();
                Session desSession = 
                  SessionProviderFactory.createSessionProvider().getSession(destWorkspace, repositoryService.getRepository(repository));
                Node destNode = (Node)desSession.getItem(destPath);
                LockUtil.changeLockToken(nodePath, destNode);
              }
              session.logout();
            }
            Map variables = maps.getWorkflowVariables();
            uiTask.serviceContainer.endTask(uiTask.identification_, variables, transition);
            uiTask.getAncestorOfType(UIPopupContainer.class).deActivate() ;
            return;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
  
  static  public class SelectUserActionListener extends EventListener<UITask> {
    public void execute(Event<UITask> event) throws Exception {
      UITask uiTask = event.getSource();
      UITaskManager uiTaskManager = uiTask.getParent();
      uiTaskManager.initPopupSelectUser(UITask.DELEGATE_FIELD);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskManager);
    }
  }

  public void doSelect(String selectField, Object value) throws Exception {
    for (UIComponent uiInput : getChildren()) {
      if (((UIFormInput) uiInput).getName().equals(selectField)) {
        ((UIFormStringInput) uiInput).setValue(value.toString());
      }
    }
  }

}