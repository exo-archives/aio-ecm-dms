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
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.templates.groovy.ResourceResolver;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

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
      @EventConfig(phase = Phase.DECODE, listeners = UIActionForm.BackActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIActionForm.ShowComponentActionListener.class)
    }
)
public class UIActionForm extends DialogFormFields implements UISelector {
  
  private Node parentNode_;
  private String nodeTypeName_ = null ;
  private boolean isAddNew_ ;
  private String scriptPath_ = null ;
  
  public UIActionForm() throws Exception {
  }
  
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
  public ResourceResolver getTemplateResourceResolver(RequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public String getTemplate() { return getDialogPath() ; }

  public String getDialogPath() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getUIPortal().getOwner() ;
    if (nodeTypeName_ != null) {
      try {
        return templateService.getTemplatePathByUser(true, nodeTypeName_, userName);
      } catch (Exception e){
        UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
        Object[] arg = { nodeTypeName_ } ;
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.not-support", arg)) ;
      }      
    }
    return null;    
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
  
  @SuppressWarnings("unchecked")
  public void storeValue(Event event) throws Exception {
    ActionServiceContainer actionServiceContainer = getApplicationComponent(ActionServiceContainer.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    Map sortedInputs = Utils.prepareMap(getChildren(), getInputProperties(), uiExplorer.getSession());
    String path = parentNode_.getPath() ;
    parentNode_.getSession().checkPermission(path, "add_node,set_property");
    if(!isAddNew_) {
      CmsService cmsService = getApplicationComponent(CmsService.class) ;
      Node storedHomeNode = getNode().getParent() ;
      cmsService.storeNode(nodeTypeName_, storedHomeNode, sortedInputs, false) ;
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      uiExplorer.setIsHidePopup(false) ;
      uiExplorer.updateAjax(event) ;
      setPath(storedHomeNode.getPath()) ;
      return ;
    }
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
      return ;
    }
    if(parentNode_.isNew()) {
      String[] args = {parentNode_.getPath()} ;
      uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add-action",args)) ;
      return ;
    }        
    actionServiceContainer.addAction(parentNode_, nodeTypeName_, sortedInputs);
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
    UIActionManager uiActionManager = getAncestorOfType(UIActionManager.class) ;
    UIActionList uiActionList = uiActionManager.getChild(UIActionList.class) ;  
    uiActionList.updateGrid(parentNode_) ;
    uiActionManager.setRenderedChild(UIActionList.class) ;
    reset() ;
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource() ;
      UIActionContainer uiContainer = uiForm.getParent() ;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.components.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
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
        uiActionContainer.setRenderSibbling(UIActionList.class) ;
      } else {
        UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        uiExplorer.cancelAction() ;
      }
    }
  }  
}
