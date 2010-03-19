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
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;

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
      @EventConfig(listeners = UIActionForm.SaveActionListener.class),
      @EventConfig(listeners = UIDialogForm.OnchangeActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIActionForm.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)
public class UIActionForm extends UIDialogForm implements UISelectable {
  
  private String parentPath_;
  private String nodeTypeName_ = null;
  private boolean isAddNew_;
  private String scriptPath_ = null;
  private boolean isEditInList_ = false;
  private String rootPath_ = null;
  
  private static final String EXO_ACTIONS = "exo:actions".intern();
  
  public UIActionForm() throws Exception {setActions(new String[]{"Save","Back"});}
  
  public void createNewAction(Node parentNode, String actionType, boolean isAddNew) throws Exception {
    reset();
    parentPath_ = parentNode.getPath();
    nodeTypeName_ = actionType;
    isAddNew_ = isAddNew;
    componentSelectors.clear();
    properties.clear();
    getChildren().clear();
  }
  
  private Node getParentNode() throws Exception{ return (Node) getSession().getItem(parentPath_); }
  
  public void doSelect(String selectField, Object value) throws Exception {
    isUpdateSelect = true;
    UIFormInput formInput = getUIInput(selectField);
    if (formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value.toString());
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput;            
      String valueSelected = String.valueOf(value).trim();
      List valueList = inputSet.getValue();
      if (!valueList.contains(valueSelected)) {
        valueList.add(valueSelected);
      }      
      inputSet.setValue(valueList);
    }
    if(isEditInList_) {
      UIActionManager uiManager = getAncestorOfType(UIActionManager.class);
      UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class);
      uiActionListContainer.removeChildById("PopupComponent");
    } else {
      UIActionContainer uiActionContainer = getParent();
      uiActionContainer.removeChildById("PopupComponent");
    }
  }
  
  public String getCurrentPath() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode().getPath();
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
  }

  public String getTemplate() { return getDialogPath(); }

  public String getDialogPath() {
    repositoryName = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    String dialogPath = null;
    if (nodeTypeName_ != null) {
      try {
        dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName_, userName, repositoryName);
      } catch (Exception e){
      }      
    }
    return dialogPath;    
  }
  
  public String getRepositoryName() { return repositoryName; }

  @Deprecated
  public String getTenmplateNodeType() { return getTemplateNodeType(); }

  public String getTemplateNodeType() { return nodeTypeName_; }

  private void setPath(String scriptPath) {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = uiExplorer.getCurrentWorkspace() + ":" + scriptPath;
    }
    scriptPath_ = scriptPath; 
  }
  public String getPath() { return scriptPath_; }  
  public void setRootPath(String rootPath){
   rootPath_ = rootPath;
  }
  public String getRootPath(){return rootPath_;}
  public void setIsEditInList(boolean isEditInList) { isEditInList_ = isEditInList; }
  
  public void onchange(Event<?> event) throws Exception {
    if(isEditInList_ || !isAddNew_) {
      event.getRequestContext().addUIComponentToUpdateByAjax(getParent());
      return;
    }
    UIActionManager uiManager = getAncestorOfType(UIActionManager.class);
    uiManager.setRenderedChild(UIActionContainer.class);
    event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
  }
  
  static public class SaveActionListener extends EventListener<UIActionForm> {
  	
  	private void addInputInfo(Map<String, JcrInputProperty> input, UIActionForm actionForm) throws Exception {
      UIJCRExplorer uiExplorer = actionForm.getAncestorOfType(UIJCRExplorer.class);
      UIJCRExplorerPortlet uiExplorerPortlet = actionForm.getAncestorOfType(UIJCRExplorerPortlet.class);
      PortalRequestContext pContext = Util.getPortalRequestContext();
      PortletRequestContext portletContext 
      	= (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      //portletname
      JcrInputProperty portletNameProperty = new JcrInputProperty();
      portletNameProperty.setValue(portletContext.getWindowId());
      portletNameProperty.setValueType(JcrInputProperty.SINGLE_VALUE);
      input.put("/node/exo:portletName", portletNameProperty);
      //drive name
      UITreeExplorer treeExplorer = uiExplorer.findFirstComponentOfType(UITreeExplorer.class);
      JcrInputProperty driveNameProperty = new JcrInputProperty();
      driveNameProperty.setValue(treeExplorer.getDriveName());
      driveNameProperty.setValueType(JcrInputProperty.SINGLE_VALUE);
      input.put("/node/exo:driveName", driveNameProperty);
      //portalUri
      JcrInputProperty portalUriProperty = new JcrInputProperty();
      portalUriProperty.setValue(pContext.getPortalURI());
      portalUriProperty.setValueType(JcrInputProperty.SINGLE_VALUE);
      input.put("/node/exo:portalUri", portalUriProperty);
  	}
    public void execute(Event<UIActionForm> event) throws Exception {      
      UIActionForm actionForm = event.getSource();
      UIApplication uiApp = actionForm.getAncestorOfType(UIApplication.class);
      ActionServiceContainer actionServiceContainer = actionForm.getApplicationComponent(ActionServiceContainer.class);
      UIJCRExplorer uiExplorer = actionForm.getAncestorOfType(UIJCRExplorer.class);   
      String repository = actionForm.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      Map<String, JcrInputProperty> sortedInputs = DialogFormUtil.prepareMap(actionForm.getChildren(), actionForm.getInputProperties());
      
      addInputInfo(sortedInputs, actionForm);
      
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canAddNode(currentNode) || !PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.no-permission-add", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiExplorer.addLockToken(currentNode);
      if (!actionForm.isAddNew_) {
        CmsService cmsService = actionForm.getApplicationComponent(CmsService.class);      
        Node storedHomeNode = actionForm.getNode().getParent();
        cmsService.storeNode(actionForm.nodeTypeName_, storedHomeNode, sortedInputs, false,repository);
        
        Node currentActionNode = storedHomeNode.getNode(sortedInputs.get("/node").getValue().toString());
        Session session = currentActionNode.getSession();
        session.move(currentActionNode.getPath(), storedHomeNode.getPath() + "/" + sortedInputs.get("/node/exo:name").getValue().toString());
        session.save();
        
        if (!uiExplorer.getPreference().isJcrEnable()) currentNode.getSession().save();
        if (actionForm.isEditInList_) {
          UIActionManager uiManager = actionForm.getAncestorOfType(UIActionManager.class);
          UIPopupWindow uiPopup = uiManager.findComponentById("editActionPopup");
          uiPopup.setShow(false);
          uiPopup.setRendered(false);
          uiManager.setDefaultConfig();
          actionForm.isEditInList_ = false;
          //actionForm.isAddNew_ = true;
          actionForm.setIsOnchange(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
          uiExplorer.setIsHidePopup(true);
          uiExplorer.updateAjax(event);
        } else {
          uiExplorer.setIsHidePopup(false);
          uiExplorer.updateAjax(event);
        }
        actionForm.setPath(storedHomeNode.getPath());
        actionServiceContainer.removeAction(currentNode, currentActionNode.getName(), repository);
        //return;
      }
      try{
        JcrInputProperty rootProp = sortedInputs.get("/node");
        if(rootProp == null) {
          rootProp = new JcrInputProperty();
          rootProp.setJcrPath("/node");
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
          sortedInputs.put("/node", rootProp);
        } else {
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
        }
        String actionName = (String)(sortedInputs.get("/node/exo:name")).getValue();        
        String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{", "/", "|", "\""};
        for(String filterChar : arrFilterChar) {
          if(actionName.indexOf(filterChar) > -1) {
            uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.name-not-allowed", null, 
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
        Node parentNode = actionForm.getParentNode();
        if (actionForm.isAddNew_) 
	        if(parentNode.hasNode(EXO_ACTIONS)) {
	          if(parentNode.getNode(EXO_ACTIONS).hasNode(actionName)) { 
	            Object[] args = {actionName};
	            uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.existed-action", args, 
	                ApplicationMessage.WARNING));
	            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
	            return;
	          }
	        }
        if(parentNode.isNew()) {
          String[] args = {parentNode.getPath()};
          uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add-action",args));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        actionServiceContainer.addAction(parentNode, repository, actionForm.nodeTypeName_, sortedInputs);
        actionForm.setIsOnchange(false);
        if (!uiExplorer.getPreference().isJcrEnable()) parentNode.getSession().save();
        UIActionManager uiActionManager = actionForm.getAncestorOfType(UIActionManager.class);
        actionForm.createNewAction(uiExplorer.getCurrentNode(), actionForm.nodeTypeName_, true);
        UIActionList uiActionList = uiActionManager.findFirstComponentOfType(UIActionList.class);  
        uiActionList.updateGrid(parentNode, uiActionList.getChild(UIPageIterator.class).getCurrentPage());
        uiActionManager.setRenderedChild(UIActionListContainer.class);
        actionForm.reset();
        //actionForm.isEditInList_ = false;
      } catch(RepositoryException repo) {      
        String key = "UIActionForm.msg.repository-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(NumberFormatException nume) {
        String key = "UIActionForm.msg.numberformat-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (NullPointerException nullPointerException) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {           
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } finally {
        if (actionForm.isEditInList_) {
          actionForm.isEditInList_ = false;          
        }
      }      
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource();
      UIContainer uiContainer = null;
      uiForm.isShowingComponent = true;
      if(uiForm.isEditInList_) {
        uiContainer = uiForm.getAncestorOfType(UIActionListContainer.class);
      } else {
        uiContainer = uiForm.getParent();
      }
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      Map fieldPropertiesMap = uiForm.componentSelectors.get(fieldName);
      String classPath = (String)fieldPropertiesMap.get("selectorClass");
      String rootPath = (String)fieldPropertiesMap.get("rootPath");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz = Class.forName(classPath, true, cl);
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      String selectorParams = (String)fieldPropertiesMap.get("selectorParams");
      if(uiComp instanceof UIOneNodePathSelector) {
        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
        String repositoryName = explorer.getRepositoryName();
        SessionProvider provider = explorer.getSessionProvider();        
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField");
        String wsName = "";
        if(wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue();          
          ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true);           
        }
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",");
          if(arrParams.length == 4) {
            ((UIOneNodePathSelector)uiComp).setAcceptedNodeTypesInPathPanel(new String[] {Utils.NT_FILE});
            wsName = arrParams[1];
            rootPath = arrParams[2];
            ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true);
            if(arrParams[3].indexOf(";") > -1) {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(arrParams[3].split(";"));
            } else {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(new String[] {arrParams[3]});
            }
          }
        }
        if(rootPath == null) rootPath = "/";
        ((UIOneNodePathSelector)uiComp).setRootNodeLocation(repositoryName, wsName, rootPath);
        ((UIOneNodePathSelector)uiComp).setShowRootPathSelect(true);
        ((UIOneNodePathSelector)uiComp).init(provider);
      }
      if(uiForm.isEditInList_) ((UIActionListContainer) uiContainer).initPopup(uiComp);
      else ((UIActionContainer)uiContainer).initPopup(uiComp);
      String param = "returnField=" + fieldName;
      String[] params = selectorParams == null ? new String[]{param} : new String[]{param, "selectorParams=" + selectorParams};
      ((ComponentSelector)uiComp).setSourceComponent(uiForm, params);
      if(uiForm.isAddNew_) {
        UIContainer uiParent = uiContainer.getParent();
        uiParent.setRenderedChild(uiContainer.getId());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

  static public class RemoveReferenceActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource();
      uiForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFormInput formInput = uiForm.getUIInput(fieldName);
      formInput.setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }
  
  static public class BackActionListener extends EventListener<UIActionForm> {
    public void execute(Event<UIActionForm> event) throws Exception {
      UIActionForm uiForm = event.getSource();
      UIActionManager uiManager = uiForm.getAncestorOfType(UIActionManager.class);
      if(uiForm.isAddNew_) {
        uiManager.setRenderedChild(UIActionListContainer.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
      } else {
        if(uiForm.isEditInList_) {
          uiManager.setRenderedChild(UIActionListContainer.class);
          uiManager.setDefaultConfig();
          UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class);
          UIPopupWindow uiPopup = uiActionListContainer.findComponentById("editActionPopup");
          uiPopup.setShow(false);
          uiPopup.setRendered(false);
          uiForm.isEditInList_ = false;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
        } else {
          UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
          uiExplorer.cancelAction();
        }
      }
    }
  }  
}
