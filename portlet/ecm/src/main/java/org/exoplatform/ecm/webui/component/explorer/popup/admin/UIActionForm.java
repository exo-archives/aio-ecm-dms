/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 8, 2006
 * 11:23:50 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
      @EventConfig(listeners = DialogFormFields.OnchangeActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIActionForm.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionForm.ShowComponentActionListener.class, phase = Phase.DECODE)
    }
)
public class UIActionForm extends DialogFormFields implements UISelector {
  
  private Node parentNode_;
  private String nodeTypeName_ = null ;
  private boolean isAddNew_ ;
  private String scriptPath_ = null ;
  private boolean isEditInList_ = false ;
  
  public UIActionForm() throws Exception {setActions(new String[]{"Save","Back"}) ;}
  
  public void createNewAction(Node parentNode, String actionType, boolean isAddNew) throws Exception {
    reset() ;
    parentNode_ = parentNode;
    nodeTypeName_ = actionType;
    isAddNew_ = isAddNew ;
    components.clear() ;
    properties.clear() ;
    getChildren().clear() ;
  }
  
  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    UIContainer uiContainer = getParent() ;
    UIPopupWindow uiPopup = uiContainer.getChild(UIPopupWindow.class) ;
    uiPopup.setShow(false) ;
  }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public String getTemplate() { return getDialogPath() ; }

  public String getDialogPath() {
    repository_ = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    String dialogPath = null ;
    if (nodeTypeName_ != null) {
      try {
        dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName_, userName, repository_);
      } catch (Exception e){}      
    }
    return dialogPath ;    
  }
  
  public String getTenmplateNodeType() { return nodeTypeName_ ; }
  
  private void setPath(String scriptPath) {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = uiExplorer.getSession().getWorkspace().getName() + ":" + scriptPath ;
    }
    scriptPath_ = scriptPath ; 
  }
  public String getPath() { return scriptPath_ ; }
  
  public void setIsEditInList(boolean isEditInList) { isEditInList_ = isEditInList; }
  
  public void onchange(Event event) throws Exception {
    UIActionContainer uiActionContainer = getAncestorOfType(UIActionContainer.class) ;
    uiActionContainer.setRenderSibbling(UIActionContainer.class) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
  }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    ActionServiceContainer actionServiceContainer = getApplicationComponent(ActionServiceContainer.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;   
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    Map sortedInputs = Utils.prepareMap(getChildren(), getInputProperties(), uiExplorer.getSession());
    String path = parentNode_.getPath() ;
    String pers = PermissionType.ADD_NODE + "," + PermissionType.SET_PROPERTY ;
    parentNode_.getSession().checkPermission(path, pers);
    if(!isAddNew_) {
      CmsService cmsService = getApplicationComponent(CmsService.class) ;
      Node storedHomeNode = getNode().getParent() ;
      cmsService.storeNode(nodeTypeName_, storedHomeNode, sortedInputs, 
                           false, Util.getPortalRequestContext().getRemoteUser(), repository) ;
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      if(isEditInList_) {
        UIActionManager uiManager = getAncestorOfType(UIActionManager.class) ;
        UIPopupWindow uiPopup = uiManager.findComponentById("editActionPopup") ;
        uiPopup.setShow(false) ;
        uiPopup.setRendered(false) ;
        uiManager.setDefaultConfig() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      } else {
        uiExplorer.setIsHidePopup(false) ;
        uiExplorer.updateAjax(event) ;
      }
      setPath(storedHomeNode.getPath()) ;
      return getNode();
    }
    try{
      JcrInputProperty rootProp = (JcrInputProperty) sortedInputs.get("/node");
      if(rootProp == null) {
        rootProp = new JcrInputProperty();
        rootProp.setJcrPath("/node");
        rootProp.setValue(((JcrInputProperty)sortedInputs.get("/node/exo:name")).getValue()) ;
        sortedInputs.put("/node", rootProp) ;
      } else {
        rootProp.setValue(((JcrInputProperty)sortedInputs.get("/node/exo:name")).getValue());
      }
      String actionName = (String)((JcrInputProperty)sortedInputs.get("/node/exo:name")).getValue() ;
      if(parentNode_.hasNode(actionName)) { 
        Object[] args = {actionName} ;
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.existed-action", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return null;
      }
      if(parentNode_.isNew()) {
        String[] args = {parentNode_.getPath()} ;
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add-action",args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return null;
      }
      actionServiceContainer.addAction(parentNode_, repository, nodeTypeName_, sortedInputs);
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      UIActionManager uiActionManager = getAncestorOfType(UIActionManager.class) ;
      createNewAction(uiExplorer.getCurrentNode(), nodeTypeName_, true) ;
      UIActionList uiActionList = uiActionManager.findFirstComponentOfType(UIActionList.class) ;  
      uiActionList.updateGrid(parentNode_) ;
      uiActionManager.setRenderedChild(UIActionListContainer.class) ;
      reset() ;
    } catch (Exception e) {
      e.printStackTrace() ;
      uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null)) ;
      return null;
    }
    return null ;
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource() ;
      UIActionContainer uiContainer = null;
      if(uiForm.isEditInList_) {
        uiContainer = uiForm.getAncestorOfType(UIActionManager.class).getChild(UIActionContainer.class) ;
      } else {
        uiContainer = uiForm.getParent() ;
      }
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.components.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIJCRBrowser) {
        String repository = uiForm.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
        ((UIJCRBrowser)uiComp).setRepository(repository) ;
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        if(wsFieldName != null && wsFieldName.length() > 0) {
          String wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;
          ((UIJCRBrowser)uiComp).setWorkspace(wsName) ;          
        }
      }
      uiContainer.initPopup(uiComp) ;
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)uiComp).setComponent(uiForm, new String[]{param}) ;
      if(uiForm.isAddNew_) {
        UIContainer uiParent = uiContainer.getParent() ;
        uiParent.setRenderedChild(uiContainer.getId()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static public class BackActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource() ;
      if(uiForm.isAddNew_) {
        UIActionContainer uiActionContainer = event.getSource().getParent() ;
        uiActionContainer.setRenderSibbling(UIActionListContainer.class) ;
      } else {
        if(uiForm.isEditInList_) {
          UIActionManager uiManager = uiForm.getAncestorOfType(UIActionManager.class) ;
          uiManager.setRenderedChild(UIActionListContainer.class) ;
          uiManager.setDefaultConfig() ;
          UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class) ;
          UIPopupWindow uiPopup = uiActionListContainer.findComponentById("editActionPopup") ;
          uiPopup.setShow(false) ;
          uiPopup.setRendered(false) ;
        } else {
          UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
          uiExplorer.cancelAction() ;
        }
      }
    }
  }  
}
