/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 25, 2007 9:10:53 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIEditModeConfiguration.SaveActionListener.class),
      @EventConfig(listeners = UIEditModeConfiguration.SelectPathActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIEditModeConfiguration.ChangeWorkspaceActionListener.class, phase=Phase.DECODE)
    }
)
public class UIEditModeConfiguration extends UIForm implements UISelector {

  final static public String FIELD_SELECT = "selectTemplate" ;
  final static public String FIELD_SAVEDPATH = "savedPath" ;
  final static public String ACTION_INPUT = "actionInput" ;
  final static public String WORKSPACE_NAME = "workspaceName" ;
  
  public UIEditModeConfiguration() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiWorkspaceList = 
      new UIFormSelectBox(UIEditModeConfiguration.WORKSPACE_NAME, UIEditModeConfiguration.WORKSPACE_NAME, options) ; 
    uiWorkspaceList.setOnChange("ChangeWorksapce") ;
    addUIFormInput(uiWorkspaceList) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction(ACTION_INPUT) ;
    uiInputAct.addUIFormInput(new UIFormStringInput(FIELD_SAVEDPATH, FIELD_SAVEDPATH, null)) ;
    uiInputAct.setActionInfo(FIELD_SAVEDPATH, new String[] {"SelectPath"}) ;
    addUIComponentInput(uiInputAct) ;
    addUIFormInput(new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, options)) ;
    setActions(new String[] {"Save"}) ;
  }
  
  public void initEditMode() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = context.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    boolean isDefaultWs = false ;
    ManageableRepository repository = getApplicationComponent(RepositoryService.class).getRepository();
    String[] wsNames = repository.getWorkspaceNames();
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
    String prefWs = preferences.getValue(Utils.WORKSPACE_NAME, "") ;
    setTemplateOptions(preferences.getValue("path", ""), prefWs) ;
    for(String wsName : wsNames) {
      if(wsName.equals(prefWs)) isDefaultWs = true ;
      workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
    }
    UIFormSelectBox uiWorkspaceList = getUIFormSelectBox(WORKSPACE_NAME) ; 
    uiWorkspaceList.setOptions(workspace) ;
    if(isDefaultWs) {
      uiWorkspaceList.setValue(prefWs);
    } else if(workspace.size() > 0) {
      uiWorkspaceList.setValue(workspace.get(0).getValue());
    }
    getUIStringInput(FIELD_SAVEDPATH).setValue(preferences.getValue("path", "")) ;
  }
  
  private void setTemplateOptions(String nodePath, String wsName) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    Session session = repositoryService.getRepository().getSystemSession(wsName) ;
    Node currentNode = null ;
    UIFormSelectBox uiSelectTemplate = getUIFormSelectBox(FIELD_SELECT) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    boolean hasDefaultDoc = false ;
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = context.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    String defaultValue = preferences.getValue("type", "") ;
    try {
      currentNode = (Node)session.getItem(nodePath) ;
    } catch(Exception ex) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.item-not-found", null, 
                                              ApplicationMessage.WARNING)) ;
      return ;
    }
    NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = currentNode.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates() ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ; 
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
        NodeType[] superTypes = nodeType.getSupertypes() ;
        boolean isCanCreateDocument = false ;
        for(NodeDefinition childDef : childDefs){
          NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes() ;
          for(NodeType requiredChild : requiredChilds) {          
            if(nodeTypeName.equals(requiredChild.getName())){            
              isCanCreateDocument = true ;
              break ;
            }            
          }
          if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
            if(!hasDefaultDoc && nodeTypeName.equals(defaultValue)) hasDefaultDoc = true ;
            String label = templateService.getTemplateLabel(nodeTypeName) ;
            options.add(new SelectItemOption<String>(label, nodeTypeName));          
            isCanCreateDocument = true ;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!hasDefaultDoc && nodeTypeName.equals(defaultValue)) {
                    hasDefaultDoc = true ;
                  }
                  String label = templateService.getTemplateLabel(nodeTypeName) ;
                  options.add(new SelectItemOption<String>(label, nodeTypeName));                
                  isCanCreateDocument = true ;
                  break;
                }
              }
              if(isCanCreateDocument) break ;
            }
            if(isCanCreateDocument) break ;
          }
        }            
      }
      uiSelectTemplate.setOptions(options) ;
      if(hasDefaultDoc) {
        uiSelectTemplate.setValue(defaultValue);
      } else if(options.size() > 0) {
        defaultValue = options.get(0).getValue() ;
        uiSelectTemplate.setValue(defaultValue);
      } 
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    String wsName = getUIFormSelectBox(WORKSPACE_NAME).getValue() ;
    try {
      setTemplateOptions(value, wsName) ;
    } catch(Exception ex) {
      ex.printStackTrace() ;
    }
    UIDialogPortlet uiDialog = getParent() ;
    UIPopupWindow uiPopup = uiDialog.getChild(UIPopupWindow.class) ;
    uiPopup.setRendered(false) ;
    uiPopup.setShow(false) ;
  }
  
  static public class SelectPathActionListener extends EventListener<UIEditModeConfiguration> {
    public void execute(Event<UIEditModeConfiguration> event) throws Exception {
      UIEditModeConfiguration uiTypeForm = event.getSource() ;
      UIDialogPortlet uiDialog = uiTypeForm.getParent() ;
      uiDialog.initPopupJCRBrowser(uiTypeForm.getUIFormSelectBox(WORKSPACE_NAME).getValue()) ;
    }
  }
  
  static public class ChangeWorkspaceActionListener extends EventListener<UIEditModeConfiguration> {
    public void execute(Event<UIEditModeConfiguration> event) throws Exception {
      UIEditModeConfiguration uiTypeForm = event.getSource() ;
      uiTypeForm.getUIStringInput(FIELD_SAVEDPATH).setValue("/") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTypeForm.getParent()) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIEditModeConfiguration> {
    public void execute(Event<UIEditModeConfiguration> event) throws Exception {
      UIEditModeConfiguration uiSelectForm = event.getSource() ;
      UIApplication uiApp = uiSelectForm.getAncestorOfType(UIApplication.class) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletRequest request = context.getRequest() ; 
      PortletPreferences preferences = request.getPreferences() ;
      String fileType = uiSelectForm.getUIFormSelectBox(FIELD_SELECT).getValue() ;
      String location = uiSelectForm.getUIStringInput(FIELD_SAVEDPATH).getValue() ;
      String wsName = uiSelectForm.getUIFormSelectBox(WORKSPACE_NAME).getValue() ;
      preferences.setValue("workspace", wsName) ;
      preferences.setValue("path", location) ;
      preferences.setValue("type", fileType) ;
      preferences.store() ;
      uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.save-successfully", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      uiSelectForm.reset() ;
    }
  }
}
