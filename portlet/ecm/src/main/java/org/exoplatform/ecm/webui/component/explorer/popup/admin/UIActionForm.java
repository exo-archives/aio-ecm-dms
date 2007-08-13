/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
    isUpdateSelect_ = true ;
    getUIStringInput(selectField).setValue(value) ;
    if(isEditInList_) {
      UIActionManager uiManager = getAncestorOfType(UIActionManager.class) ;
      UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class) ;
      uiActionListContainer.removeChildById("PopupComponent") ;
    } else {
      UIActionContainer uiActionContainer = getParent() ;
      uiActionContainer.removeChildById("PopupComponent") ;
    }
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
      } catch (Exception e){
        e.printStackTrace() ;
      }      
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
    if(isEditInList_ || !isAddNew_) {
      event.getRequestContext().addUIComponentToUpdateByAjax(getParent()) ;
      return ;
    }
    UIActionManager uiManager = getAncestorOfType(UIActionManager.class) ;
    uiManager.setRenderedChild(UIActionContainer.class) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
  }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    ActionServiceContainer actionServiceContainer = getApplicationComponent(ActionServiceContainer.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;   
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    Map sortedInputs = Utils.prepareMap(getChildren(), getInputProperties(), uiExplorer.getSession());
    if(!Utils.isAddNodeAuthorized(uiExplorer.getCurrentNode()) || !Utils.isSetPropertyNodeAuthorized(uiExplorer.getCurrentNode())) {
      uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.no-permission-add", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    if(!isAddNew_) {
      CmsService cmsService = getApplicationComponent(CmsService.class) ;
      Node storedHomeNode = getNode().getParent() ;
      cmsService.storeNode(nodeTypeName_, storedHomeNode, sortedInputs, false,repository) ;
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      if(isEditInList_) {
        UIActionManager uiManager = getAncestorOfType(UIActionManager.class) ;
        UIPopupWindow uiPopup = uiManager.findComponentById("editActionPopup") ;
        uiPopup.setShow(false) ;
        uiPopup.setRendered(false) ;
        uiManager.setDefaultConfig() ;
        isEditInList_ = false ;
        isAddNew_ = true ;
        setIsOnchange(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
        uiExplorer.setIsHidePopup(true) ;
        uiExplorer.updateAjax(event) ;
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
      setIsOnchange(false) ;
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      UIActionManager uiActionManager = getAncestorOfType(UIActionManager.class) ;
      createNewAction(uiExplorer.getCurrentNode(), nodeTypeName_, true) ;
      UIActionList uiActionList = uiActionManager.findFirstComponentOfType(UIActionList.class) ;  
      uiActionList.updateGrid(parentNode_) ;
      uiActionManager.setRenderedChild(UIActionListContainer.class) ;
      reset() ;
      isEditInList_ = false ;
    } catch(RepositoryException repo) {
      String key = "UIActionForm.msg.repository-exception" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(NumberFormatException nume) {
      String key = "UIActionForm.msg.numberformat-exception" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
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
      UIContainer uiContainer = null;
      if(uiForm.isEditInList_) {
        uiContainer = uiForm.getAncestorOfType(UIActionListContainer.class) ;
      } else {
        uiContainer = uiForm.getParent() ;
      }
      //TO DO why need load class here?
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.components.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIJCRBrowser) {
        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        String repositoryName = explorer.getRepositoryName() ;
        SessionProvider provider = explorer.getSessionProvider() ;                
        ((UIJCRBrowser)uiComp).setRepository(repositoryName) ;
        ((UIJCRBrowser)uiComp).setSessionProvider(provider) ;
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        if(wsFieldName != null && wsFieldName.length() > 0) {
          String wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;
          ((UIJCRBrowser)uiComp).setIsDisable(wsName, true) ;          
        }
      }
      if(uiForm.isEditInList_) ((UIActionListContainer) uiContainer).initPopup(uiComp) ;
      else ((UIActionContainer)uiContainer).initPopup(uiComp) ;
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
      UIActionManager uiManager = uiForm.getAncestorOfType(UIActionManager.class) ;
      if(uiForm.isAddNew_) {
        uiManager.setRenderedChild(UIActionListContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      } else {
        if(uiForm.isEditInList_) {
          uiManager.setRenderedChild(UIActionListContainer.class) ;
          uiManager.setDefaultConfig() ;
          UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class) ;
          UIPopupWindow uiPopup = uiActionListContainer.findComponentById("editActionPopup") ;
          uiPopup.setShow(false) ;
          uiPopup.setRendered(false) ;
          uiForm.isEditInList_ = false ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
        } else {
          UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
          uiExplorer.cancelAction() ;
        }
      }
    }
  }  
}
