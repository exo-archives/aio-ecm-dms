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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *        phamtuanchip@yahoo.de
 * Nov 08, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
      @EventConfig(listeners = UIDocumentForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveDataActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
      
    }
)

public class UIDocumentForm extends DialogFormFields implements UIPopupComponent, UISelector {

  private String documentType_;
  private boolean isAddNew_ = false; 
  
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"});    
  }

  public void setTemplateNode(String type) { documentType_ = type;}

  public boolean isAddNew() {return isAddNew_;}

  public void addNew(boolean b) {isAddNew_ = b;}

  public void setRepositoryName(String repositoryName) { repositoryName_ = repositoryName; }

  public void updateSelect(String selectField, String value) {
    isUpdateSelect_ = true;    
    UIFormInput formInput = getUIInput(selectField);
    if(formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value);
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput;
      UIFormInputBase input = inputSet.getChild(inputSet.getChildren().size()-1);
      input.setValue(value);
    }
    UIDocumentFormController uiContainer = getParent();
    uiContainer.removeChildById("PopupComponent");
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try {
      resetScriptInterceptor();
      return templateService.getTemplatePathByUser(true, documentType_, userName, repositoryName_);
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = { documentType_ };
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
          ApplicationMessage.ERROR));
      return null;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode(); 
  }

  public boolean isEditing() { return !isAddNew_; }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    List inputs = getChildren();
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties());
    Node newNode = null;
    String nodeType;
    Node homeNode;
    Node currentNode = uiExplorer.getCurrentNode();
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    if(isAddNew()) {
      UIDocumentFormController uiDFController = getParent();
      homeNode = currentNode;
      nodeType = uiDFController.getChild(UISelectDocumentForm.class).getSelectValue();
      
      if(homeNode.isLocked()) {
        homeNode.getSession().addLockToken(Utils.getLockToken(homeNode));
      }
    } else { 
      homeNode = getNode().getParent();
      nodeType = getNode().getPrimaryNodeType().getName();
      if(getNode().isLocked()) {
        getNode().getSession().addLockToken(Utils.getLockToken(getNode()));
      }
    }       
    try {
      CmsService cmsService = getApplicationComponent(CmsService.class);
      String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, isAddNew(),repositoryName_);
      try {
        homeNode.save();
        newNode = (Node)homeNode.getSession().getItem(addedPath);
      } catch(Exception e) {
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
        uiExplorer.updateAjax(event);
        return null;
      }
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
      uiExplorer.updateAjax(event);        
    } catch (AccessControlException ace) {
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;
    } catch(ItemNotFoundException item) {
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;      
    } catch(LockException lock) {
      Object[] arg = { currentNode.getPath() };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;
    } catch(RepositoryException repo) {
      repo.printStackTrace();
      String key = "UIDocumentForm.msg.repository-exception";
      if(ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;
    } catch(NumberFormatException nume) {
      String key = "UIDocumentForm.msg.numberformat-exception";
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;
    } catch(Exception e) {
      String key = "UIDocumentForm.msg.cannot-save";
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return null;
    }
    return newNode;
  }

  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource();
      UIDocumentFormController uiContainer = uiForm.getParent();
      uiForm.isShowingComponent_ = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      Map fieldPropertiesMap = uiForm.components.get(fieldName);
      String classPath = (String)fieldPropertiesMap.get("selectorClass");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz = Class.forName(classPath, true, cl);
      String rootPath = (String)fieldPropertiesMap.get("rootPath");
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIJCRBrowser) {
        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
        String repositoryName = explorer.getRepositoryName();
        SessionProvider provider = explorer.getSessionProvider();                
        ((UIJCRBrowser)uiComp).setRepository(repositoryName);
        ((UIJCRBrowser)uiComp).setSessionProvider(provider);
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField");
        if(wsFieldName != null && wsFieldName.length() > 0) {
          String wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue();
          ((UIJCRBrowser)uiComp).setIsDisable(wsName, true);      
        }
        if(wsFieldName != null && rootPath != null) ((UIJCRBrowser)uiComp).setRootPath(rootPath);
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams");
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",");
          if(arrParams.length == 4) {
            ((UIJCRBrowser)uiComp).setFilterType(new String[] {Utils.NT_FILE});
            ((UIJCRBrowser)uiComp).setRootPath(arrParams[2]);
            if(arrParams[3].indexOf(";") > -1) {
              ((UIJCRBrowser)uiComp).setMimeTypes(arrParams[3].split(";"));
            } else {
              ((UIJCRBrowser)uiComp).setMimeTypes(new String[] {arrParams[3]});
            }
          }
        }
      }
      uiContainer.initPopup(uiComp);
      String param = "returnField=" + fieldName;
      ((ComponentSelector)uiComp).setComponent(uiForm, new String[]{param});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }  
  
  static public class RemoveReferenceActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource();
      uiForm.isRemovePreference_ = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      uiForm.getUIStringInput(fieldName).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }
  
  static public class RemoveDataActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource();
      uiForm.isRemovePreference_ = true;
      String referenceNodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      Node referenceNode = (Node)uiForm.getSesssion().getItem(uiForm.getNodePath() + referenceNodePath);
      if(referenceNode.hasProperty(Utils.JCR_DATA)) {
        referenceNode.setProperty(Utils.JCR_DATA, "");
        uiForm.setDataRemoved(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }  

  static  public class CancelActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

  static public class AddActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent());
    }
  }

  static public class RemoveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent());
    }
  }  
}